package com.java.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.pojo.Item;
import com.java.pojo.Orders;
import com.java.service.ItemService;
import com.java.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "ordersQueue")
public class OrderMqReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderMqReceiver.class);

    @Autowired
    private ItemService temService;

    @Autowired
    private OrdersService ordersService;

    @RabbitHandler
    public void process(String message) {
        LOGGER.info("mq：OrderMqReceiver收到消息开始用户下单流程: " + message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        try {
            JSONObject orders=jsonObject.getJSONObject("orders");
            Integer item_id=jsonObject.getInteger("item_id");
            Orders o= JSON.toJavaObject(orders, Orders.class);
            ordersService.createOrderByMq(o,item_id);
        } catch (Exception e) {
            LOGGER.error("消息处理异常：", e);
        }
    }
}
