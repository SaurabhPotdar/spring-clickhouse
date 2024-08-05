package com.tce.clickhouse.config;

import com.clickhouse.client.config.ClickHouseClientOption;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "clickhouseEntityManager",
        transactionManagerRef = "clickhouseTransactionManager",
        basePackages = {
                "com.tce.clickhouse.entities",
                "com.tce.clickhouse.repository"
        }
)
@RequiredArgsConstructor
public class ClickhouseDataSourceConfig {


    @Value("${spring.datasource.clickhouse.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.clickhouse.url}")
    private String url;

    @Value("${spring.datasource.clickhouse.database}")
    private String database;

    @Value("${spring.datasource.clickhouse.username}")
    private String username;

//    @Value("${spring.datasource.clickhouse.password}")
//    private String password;

    @Value("${spring.application.name}")
    private String appName;

    private final JpaProperties jpaProperties;

    private final HibernateProperties hibernateProperties;

    public DataSource getClickhouseDataSource() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(ClickHouseClientOption.DATABASE.getKey(), database);
        properties.setProperty(ClickHouseClientOption.CLIENT_NAME.getKey(), username);
        properties.setProperty("user", username);
        //properties.setProperty("password", password);

        return new ClickHouseDataSource(url, properties);
    }

    @Bean(name = "clickhouseDataSource")
    public DataSource clickhouseDataSource() throws SQLException {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(getClickhouseDataSource());
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setPoolName(appName + "-pool");
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(120000L);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setRegisterMbeans(true);
        return new P6DataSource(new HikariDataSource(hikariConfig));
    }

    @Bean(name = "clickhouseEntityManager")
    public LocalContainerEntityManagerFactoryBean clickhouseEntityManager(@Qualifier("clickhouseDataSource") final DataSource dataSource,
                                                                          final EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .properties(getVendorProperties())
                .packages("com.tce.clickhouse.entities")
                .persistenceUnit("clickhouseEntityManager")
                .build();
    }

    private Map<String, Object> getVendorProperties() {
        final Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return hibernateProperties.determineHibernateProperties(
                properties, new HibernateSettings());
    }

    @Bean(name = "clickhouseTransactionManager")
    public PlatformTransactionManager clickhouseTransactionManager(final LocalContainerEntityManagerFactoryBean clickhouseEntityManager) {
        final JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(clickhouseEntityManager.getObject());
        return txManager;
    }

}
