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
        int id = createOrder(item,number,user_id);
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
        createOrder(item,number,user_id);
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
        createOrder(item,number,user_id);
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
        createOrder(item,number,user_id);
        //删除用户订单缓存
        delOrderCache(user_id);
        return leftStock;
    }

    @Override
    public int createVerifiedOrder(int item_id, Integer number, Integer user_id, String verifyHash) throws Exception {
        return 0;
    }

    @Override
    public void createOrderByMq(int item_id, Integer number, Integer user_id) throws Exception {

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
     * @param item
     * @param number
     * @param user_id
     * @return
     */
    private int createOrder(Item item,Integer number,Integer user_id) {
        Orders order = new Orders();
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrders_ocode(orderCode);
        order.setOrders_number(number);
        order.setItem_id(item.getId());
        order.setItem_kill_id(item.getId());//保持一致
        order.setUser_id(user_id);
        //创建时间数据库自动生成
        return ordersMapper.insertSelective(order);
    }
}
