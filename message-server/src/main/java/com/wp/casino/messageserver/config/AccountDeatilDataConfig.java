package com.wp.casino.messageserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryAccount",
        transactionManagerRef = "transactionManagerAccount",
        basePackages = {"com.wp.casino.messageserver.dao.mysql.accout"}
)
public class AccountDeatilDataConfig {

    @Autowired
    private HibernateProperties hibernateProperties;

    @Autowired
    private JpaProperties jpaProperties;

    private Map<String, Object> getVendorProperties() {
        Map<String, Object> map = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());return map;
    }

    @Primary
    @Bean(name = "accountDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.account")
    public DataSource targetDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "entityManagerFactoryAccount")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder, @Qualifier("accountDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .properties(getVendorProperties())
                .packages("com.wp.casino.messageserver.domain.mysql.account")
                .persistenceUnit("targetPersistenceUnit")
                .build();
    }

    @Primary
    @Bean(name = "transactionManagerAccount")
    public PlatformTransactionManager propertyTransactionManager(
            @Qualifier("entityManagerFactoryAccount") EntityManagerFactory propertyEntityManagerFactory) {
        return new JpaTransactionManager(propertyEntityManagerFactory);
    }
}