package com.artichourey.ecommerce.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name("order-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockFailedTopic() {
        return TopicBuilder.name("stock-failed-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name("stock-reserved-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryDLQTopic() {
        return TopicBuilder.name("inventory-dlq-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}