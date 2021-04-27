package com.java.receiver;

import com.java.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@RabbitListener(queues = "delOrdersCache")
public class DelOrderCacheReceiver {
//消费者
    private static final Logger LOGGER = LoggerFactory.getLogger(DelOrderCacheReceiver.class);

    @Autowired
    private OrdersService ordersService;

    @RabbitHandler
    public void process(String message) {
        LOGGER.info("DelOrderCacheReceiver收到消息: " + message);
        LOGGER.info("DelOrderCacheReceiver开始删除缓存: " + message);
        ordersService.delOrderCache(Integer.parseInt(message));
    }
}
