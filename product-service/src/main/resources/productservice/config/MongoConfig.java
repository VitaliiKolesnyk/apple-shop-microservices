package org.productservice.config;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoConfig {

    @Bean
    public MongoTransactionManager mongoTransactionManager(MongoClient mongoClient) {
        return new MongoTransactionManager(new SimpleMongoClientDatabaseFactory(mongoClient, "yourDatabaseName"));
    }
}
