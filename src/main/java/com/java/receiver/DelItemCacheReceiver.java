package com.java.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "delItemsCache")
public class DelItemCacheReceiver {
    //消费者
    private static final Logger LOGGER = LoggerFactory.getLogger(DelItemCacheReceiver.class);

    @Autowired
    private com.java.service.ItemService itemService;

    @RabbitHandler
    public void process(String message) {
        LOGGER.info("mq：DelItemCacheReceiver收到消息: " + message);
        LOGGER.info("mq：DelItemCacheReceiver开始删除缓存: " + message);
        itemService.delItemCache(Integer.parseInt(message));
        itemService.delStockCountCache(Integer.parseInt(message));
    }
}
