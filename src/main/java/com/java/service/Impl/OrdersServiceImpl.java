package com.java.service.Impl;

import com.java.mapper.ItemMapper;
import com.java.mapper.OrdersMapper;
import com.java.mapper.UsersMapper;
import com.java.pojo.Item;
import com.java.pojo.Orders;
import com.java.service.ItemService;
import com.java.service.OrdersService;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@CacheConfig(cacheNames="orders")
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    ItemService itemService;
    @Autowired
    UsersMapper usersMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersServiceImpl.class);

    @Override
    public void add(Orders c) {
        ordersMapper.save(c);
    }

    @Override
    @CacheEvict(key="'orders-one-'+ #p0")
    public void delete(int id) {
        ordersMapper.delete(id);
    }

    @Override
    public void update(Orders c) {
        ordersMapper.update(c);
    }

    @Override
    @Cacheable(key="'orders-one-'+ #p0")
    public Orders get(int id) {
        return ordersMapper.get(id);
    }

    @Override
//    @Cacheable(key="'orders-all'")
    public List<Orders> list() {
        return ordersMapper.findAll();
    }

    @Override
    @Cacheable(key="'orders-uid-'+ #p0")
    public List<Orders> listByUid(int uid) {
        return ordersMapper.getByUid(uid);
    }

    @Override
    @CacheEvict(key="'orders-uid-'+ #p0")
    public void delOrderCache(int uid){
        String hashKey= "orders-uid-"+uid;
        LOGGER.info("删除用户订单缓存:[{}]",hashKey);
    }

    @Override
    public int createWrongOrder(int item_id,Integer number,Integer user_id) {
        //校验库存
        Item item = checkStock(item_id,number);
        //扣库存
        saleStock(item,number);
        //创建订单
        Orders orders=initOrder(item_id,number,user_id);
        int id = createOrder(orders);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public int createPessimisticOrder(int item_id, Integer number, Integer user_id) {
        //校验库存(悲观锁for update)
        Item item = checkStockForUpdate(item_id,number);
        //更新库存
        saleStock(item,number);
        //创建订单
        Orders orders=initOrder(item_id,number,user_id);
        int id = createOrder(orders);
        return item.getitem_stock() - (item.getItem_sale());
    }

    @Override
    public int createOptimisticOrder(int item_id, Integer number, Integer user_id) {
        //校验库存
        Item item = checkStock(item_id,number);
        //乐观锁更新库存
        boolean success = saleStockOptimistic(item,number);
        if (!success){
            throw new RuntimeException("过期库存值，更新失败");
        }
        //创建订单
        Orders orders=initOrder(item_id,number,user_id);
        int id = createOrder(orders);
        return item.getitem_stock() - (item.getItem_sale());
    }

    @Override
    public int createOrderWithRedis(int item_id, Integer number, Integer user_id) {
        //校验库存
        Item item = checkStockWithRedis(item_id,number);
        int leftStock = item.getitem_stock() - (item.getItem_sale());
        //乐观锁更新DB
        boolean success = saleStockOptimistic(item,number);
        if (!success){
            throw new RuntimeException("过期库存值，更新失败");
        }
        //删除商品缓存
        itemService.delItemCache(item_id);
        //删除库存缓存
        itemService.delStockCountCache(item_id);
        //创建订单到DB
        LOGGER.info("创建订单到DB");
        Orders orders=initOrder(item_id,number,user_id);
        createOrder(orders);
        //删除用户订单缓存
        delOrderCache(user_id);
        return leftStock;
    }

    @Override
    public void createOrderByMq(Orders orders,int item_id) throws Exception {
        int number=orders.getOrders_number();
        int user_id=orders.getUser_id();
        //校验数据库中库存
        Item item = checkStockNoThrow(item_id,number);
        if(item == null){
            LOGGER.info("mq：库存不足！");
            return;
        }
        int leftStock = item.getitem_stock() - (item.getItem_sale());
        LOGGER.info("mq：库存足够，预计剩余[{}]",leftStock);
        //乐观锁更新DB
        boolean success = saleStockOptimistic(item,number);
        if (!success){
            LOGGER.info("mq：过期库存值，更新失败");
            return;
        }
        //删除商品缓存
        itemService.delItemCache(item_id);
        //删除库存缓存
        itemService.delStockCountCache(item_id);
        //创建订单到DB
        LOGGER.info("mq：创建订单到DB");
        createOrder(orders);
        //删除用户订单缓存
        delOrderCache(user_id);
    }

    @Override
    public int createVerifiedOrder(int item_id, Integer number, Integer user_id, String verifyHash) throws Exception {
        return 0;
    }

    @Override
    public Boolean checkUserOrderInfoInCache(int item_id, Integer number, Integer user_id) throws Exception {
        return null;
    }

    /**
     * 检查库存
     * @param item_id
     * @return
     */
    private Item checkStock(int item_id,int number) {
        Item item = itemService.getItemById(item_id);
        int oldStock = item.getitem_stock()-item.getItem_sale();
        int leftStock = oldStock-number;
        if (leftStock<0) {
            throw new RuntimeException("库存不足");
        }
        item.setItem_sale(item.getItem_sale() + number);
        return item;
    }
    /**
     * 检查库存
     * @param item_id
     * @return
     */
    private Item checkStockNoThrow(int item_id,int number) {
        Item item = itemService.getItemById(item_id);
        int oldStock = item.getitem_stock()-item.getItem_sale();
        int leftStock = oldStock-number;
        if (leftStock<0) {
            return null;
        }
        item.setItem_sale(item.getItem_sale() + number);
        return item;
    }

    /**
     * 检查库存 ForUpdate
     * @param item_id
     * @return
     */
    private Item checkStockForUpdate(int item_id,int number) {
        Item item = itemService.getItemByIdForUpdate(item_id);
        int oldStock = item.getitem_stock()-item.getItem_sale();
        int leftStock = oldStock-number;
        if (leftStock<0) {
            throw new RuntimeException("库存不足");
        }
        item.setItem_sale(item.getItem_sale() + number);
        return item;
    }
    /**
     * 检查缓存库存,并获取缓存中的item(若缓存不命中则从mapper拿)，若库存不足抛异常
     * @param item_id
     * @return
     */
    private Item checkStockWithRedis(int item_id,int number) {
        LOGGER.info("检查缓存中商品是否足够库存");
        Item item = itemService.get(item_id);
        int oldStock = item.getitem_stock()-item.getItem_sale();
        int leftStock = oldStock-number;
        if (leftStock<0) {
            throw new RuntimeException("库存不足");
        }
        item.setItem_sale(item.getItem_sale() + number);
        LOGGER.info("库存足够，预计剩余：[{}]",leftStock);
        return item;
    }

    @Override
    public Integer checkStockWithRedisNoThrow(int item_id,int number) {
        LOGGER.info("server：获取缓存中商品是否足够库存");
        Item item = itemService.get(item_id);
        int oldStock = item.getitem_stock()-item.getItem_sale();
        int leftStock = oldStock-number;
        return leftStock;
    }
    /**
     * 更新库存
     * @param item
     * @param number
     */
    private void saleStock(Item item,Integer number) {
        itemService.updateItemById(item);
    }
    /**
     * 更新库存 乐观锁
     * @param item
     * @param number
     */
    private boolean saleStockOptimistic(Item item,Integer number) {
        LOGGER.info("乐观锁尝试更新库存");
        int count = itemService.updateItemByOptimistic(item);
        return count != 0;
    }
    /**
     * 创建订单
     * @param order
     * @return
     */
    private int createOrder(Orders order) {
        //创建时间数据库自动生成
        return ordersMapper.insertSelective(order);
    }
    @Override
    public Orders initOrder(int item_id,Integer number,Integer user_id){
        Orders order = new Orders();
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrders_ocode(orderCode);
        order.setOrders_number(number);
        order.setItem_id(item_id);
        order.setItem_kill_id(item_id);//保持一致
        order.setUser_id(user_id);
        return order;
    }
}
