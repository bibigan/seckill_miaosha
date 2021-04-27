package com.java.receiver;


import com.java.service.ItemService;
import com.java.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.context.CanalContext;
import top.javatool.canal.client.handler.EntryHandler;
import top.javatool.canal.client.model.CanalModel;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@CanalTable(value = "all")		//这里写关联的数据表名，all表示所有
class CanalServer implements EntryHandler<Map<String, String>> {
    private static final Logger log = LoggerFactory.getLogger(CanalServer.class);
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    OrdersService ordersService;
    @Autowired
    ItemService itemService;

    // 延时时间：预估读数据库数据业务逻辑的耗时，用来做缓存再删除
    private static final int DELAY_MILLSECONDS = 1000;
    // 延时双删线程池
    private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());

    @Override
    public void insert(Map<String, String> map) {
        CanalModel canal = CanalContext.getModel();
        if(!"seckill1".equals(canal.getDatabase()))return;
        log.info("Canal：插入了一条数数据：{}"+map);
        //sse根据类型群发
        batchMessage(canal, map);
    }

    @Override
    public void update(Map<String, String> before, Map<String, String> after) {
        CanalModel canal = CanalContext.getModel();
        if(!"seckill1".equals(canal.getDatabase()))return;
        log.info("Canal：更新前：{}, 更新后：{}",before,after);
        //sse根据类型群发
        batchMessage(canal, after);

    }

    @Override
    public void delete(Map<String, String> map) {
        CanalModel canal = CanalContext.getModel();
        if(!"seckill1".equals(canal.getDatabase()))return;
        log.info("Canal：删除了一条数数据：{}"+map);
        //sse根据类型群发
        batchMessage(canal, map);
    }

    //根据改动的表群发消息
    public void batchMessage(CanalModel canal, Map<String,String> map) {
        log.info("Canal：改动表:{}",canal.getTable());
        switch (canal.getTable()) {
            case "orders":
                deleteOrderCache(map);
                break;
            case "item":
                deleteItemCache(map);
                break;
            case "item_kill":
//                log.info("Canal：!!!改动的表是item_kill!!!");
                break;
            case "users":
//                log.info("Canal：!!!改动的表是users!!!");
                break;
            default:
                return;
        }
    }
    /**
     * 秒杀下单接口删除库存缓存
     */
    public void deleteOrderCache(Map<String,String> map) {
        Integer user_id=(Integer.parseInt(map.get("user_id")));
        Integer id=(Integer.parseInt(map.get("id")));
        // TODO: 删除缓存
        log.info("Canal：删除orders表id:[{}],uid：[{}] 的库存缓存", id,user_id);
        //删除缓存
        ordersService.delOrderCache(user_id);
        try{
            // 延时指定时间后再次删除缓存
            cachedThreadPool.execute(new delOrderCacheByThread(user_id));
        }catch (Exception e){
//             上述再次删除缓存没成功，通知消息队列进行删除缓存
            sendTodelOrdersCache(String.valueOf(user_id));
        }
    }
    /**
     * 秒杀下单接口删除商品缓存
     */
    public void deleteItemCache(Map<String,String> map) {
        Integer id=(Integer.parseInt(map.get("id")));
        // TODO: 删除缓存
        log.info("Canal：删除item表商品id：[{}] 的库存缓存", id);
        //删除缓存
        itemService.delItemCache(id);
        try{
            // 延时指定时间后再次删除缓存
            cachedThreadPool.execute(new delItemCacheByThread(id));
        }catch (Exception e){
//             上述再次删除缓存没成功，通知消息队列进行删除缓存
            sendToDelItemCache(String.valueOf(id));
        }
    }
    private void sendTodelOrdersCache(String message) {
        log.info("Canal：这就去通知消息队列开始重试删除Order缓存：[{}]", message);
        this.rabbitTemplate.convertAndSend("delOrdersCache", message);
    }
    private void sendToDelItemCache(String message) {
        log.info("Canal：这就去通知消息队列开始重试删除Item缓存：[{}]", message);
        this.rabbitTemplate.convertAndSend("delItemsCache", message);
    }
    /**
     * 缓存再删除线程
     */
    private class delOrderCacheByThread implements Runnable {
        private int user_id;
        public delOrderCacheByThread(int user_id) {
            this.user_id = user_id;
        }
        public void run() {
            try {
                log.info("Canal：异步执行缓存再删除，订单uid：[{}]， 首先休眠：[{}] 毫秒", user_id, DELAY_MILLSECONDS);
                Thread.sleep(DELAY_MILLSECONDS);
                ordersService.delOrderCache(user_id);
                log.info("Canal：再次删除订单uid：[{}] 缓存", user_id);
            } catch (Exception e) {
                log.error("Canal：delOrderCacheByThread执行出错", e);
            }
        }
    }
    /**
     * 缓存再删除线程
     */
    private class delItemCacheByThread implements Runnable {
        private int id;
        public delItemCacheByThread(int id) {
            this.id = id;
        }
        public void run() {
            try {
                log.info("Canal：异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", id, DELAY_MILLSECONDS);
                Thread.sleep(DELAY_MILLSECONDS);
                itemService.delItemCache(id);
                log.info("Canal：再次删除商品id：[{}] 缓存", id);
            } catch (Exception e) {
                log.error("Canal：delItemCacheByThread执行出错", e);
            }
        }
    }
}
