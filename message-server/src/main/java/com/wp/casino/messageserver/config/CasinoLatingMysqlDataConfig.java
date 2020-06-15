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
        entityManagerFactoryRef = "entityManagerFactoryCasinoLating",
        transactionManagerRef = "transactionManagerCasinoLating",
        basePackages = {"com.wp.casino.messageserver.dao.mysql.casinolating"}
)
public class CasinoLatingMysqlDataConfig {

    @Autowired
    private HibernateProperties hibernateProperties;

    @Autowired
    private JpaProperties jpaProperties;

    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings()
        );
    }

//     # 配置数据源获取的涞源
    @Bean(name = "casinoLatingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.casino-lating1")
    public DataSource casinoLatingDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "entityManagerFactoryCasinoLating")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder, @Qualifier("casinoLatingDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .properties(getVendorProperties())
                .packages("com.wp.casino.messageserver.domain.mysql.casino")
                .persistenceUnit("primaryPersistenceUnit")
                .build();
    }


    @Bean(name = "transactionManagerCasinoLating")
    public PlatformTransactionManager propertyTransactionManager(
            @Qualifier("entityManagerFactoryCasinoLating") EntityManagerFactory propertyEntityManagerFactory) {
        return new JpaTransactionManager(propertyEntityManagerFactory);
    }
}