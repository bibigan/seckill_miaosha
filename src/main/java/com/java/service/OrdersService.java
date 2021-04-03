package com.java.service;

import com.java.pojo.Orders;

import java.util.List;

public interface OrdersService {
    void add(Orders c);
    void delete(int id);
    void update(Orders c);
    Orders get(int id);
    List<Orders> list();
    List<Orders> listByUid(int uid);
    void delOrderCache(int uid);
    /**
     * 创建错误订单
     * @param item_id
     * @param number
     * @param user_id
     * @return
     * @throws Exception
     */
    public int createWrongOrder(int item_id,Integer number,Integer user_id);

    /**
     * 创建正确订单：下单悲观锁 for update
     * @param item_id
     * @param number
     * @param user_id
     * @return
     * @throws Exception
     */
    public int createPessimisticOrder(int item_id,Integer number,Integer user_id);

    /**
     * 创建正确订单：下单乐观锁
     * @param item_id
     * @param number
     * @param user_id
     * @return
     * @throws Exception
     */
    public int createOptimisticOrder(int item_id,Integer number,Integer user_id);

    /**
     * 创建正确订单：下单乐观锁+删除缓存
     * @param item_id
     * @param number
     * @param user_id
     * @return
     * @throws Exception
     */
    public int createOrderWithRedis(int item_id,Integer number,Integer user_id);

    /**
     * 创建正确订单：验证库存 + 用户 + 时间 合法性 + 下单乐观锁
     * @param item_id
     * @param number
     * @param user_id
     * @param verifyHash
     * @return
     * @throws Exception
     */
    public int createVerifiedOrder(int item_id,Integer number,Integer user_id, String verifyHash) throws Exception;

    /**
     * 创建正确订单：验证库存 + 下单乐观锁 + 更新订单信息到缓存
     * @param item_id
     * @param number
     * @param user_id
     * @throws Exception
     */
    public void createOrderByMq(int item_id,Integer number,Integer user_id) throws Exception;

    /**
     * 检查缓存中用户是否已经有订单
     * @param item_id
     * @param number
     * @param user_id
     * @return
     * @throws Exception
     */
    public Boolean checkUserOrderInfoInCache(int item_id,Integer number,Integer user_id) throws Exception;
}
