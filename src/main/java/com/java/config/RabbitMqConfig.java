package com.java.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue ordersQueue() {
        return new Queue("ordersQueue");
    }

    @Bean
    public Queue delItemsCacheQueue() {
        return new Queue("delItemsCache");
    }
    @Bean
    public Queue delOrdersCacheQueue() {
        return new Queue("delOrdersCache");
    }
}
