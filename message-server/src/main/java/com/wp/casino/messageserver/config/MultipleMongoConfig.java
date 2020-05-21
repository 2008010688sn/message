package com.wp.casino.messageserver.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
//@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//@RefreshScope//自动刷新配置
public class MultipleMongoConfig {

	@Autowired
    private MultipleMongoProperties mongoProperties;

	//message
	@Primary
	@Bean(name = MessageMongoConfig.MONGO_TEMPLATE)
	public MongoTemplate messageMongoTemplate() throws Exception {
		return new MongoTemplate(messageFactory(this.mongoProperties.getMessage()));
	}


	//replay
	@Bean
	@Qualifier(ReplayMongoConfig.MONGO_TEMPLATE)
	public MongoTemplate replayMongoTemplate() throws Exception {
		return new MongoTemplate(replayFactory(this.mongoProperties.getReplay()));
	}

	//record
	@Bean
	@Qualifier(RecordMongoConfig.MONGO_TEMPLATE)
	public MongoTemplate recordMongoTemplate() throws Exception {
		return new MongoTemplate(recordFactory(this.mongoProperties.getRecord()));
	}


	@Bean
    @Primary
	public MongoDbFactory messageFactory(MongoProperties mongo) throws Exception {

		return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(), mongo.getPort()),
				mongo.getDatabase());
//		ServerAddress serverAddress = new ServerAddress(mongo.getHost(), mongo.getPort());
//		List<MongoCredential> mongoCredentialList = new ArrayList<>();
//		mongoCredentialList
//				.add(MongoCredential.createCredential(mongo.getUsername(), mongo.getDatabase(), mongo.getPassword()));
//		return new SimpleMongoDbFactory(new MongoClient(serverAddress, mongoCredentialList),
//				mongo.getDatabase());
	}


	@Bean
	public MongoDbFactory replayFactory(MongoProperties mongo) throws Exception {
		return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(), mongo.getPort()),
				mongo.getDatabase());
	}

	@Bean
	public MongoDbFactory recordFactory(MongoProperties mongo) throws Exception {
		return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(), mongo.getPort()),
				mongo.getDatabase());
	}


}
