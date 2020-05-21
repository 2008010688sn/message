package com.wp.casino.messageserver.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
//@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableMongoRepositories(basePackages = "com.wp.casino.messageserver.dao.mongodb.record",
        mongoTemplateRef = RecordMongoConfig.MONGO_TEMPLATE)
public class RecordMongoConfig {
	protected static final String MONGO_TEMPLATE = "recordMongoTemplate";
}
