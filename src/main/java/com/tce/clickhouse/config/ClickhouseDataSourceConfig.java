package com.tce.clickhouse.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import static io.r2dbc.spi.ConnectionFactories.get;

@Configuration
@EnableR2dbcRepositories(
        basePackages = {
                "com.tce.clickhouse.entities",
                "com.tce.clickhouse.repository"
        }
)
public class ClickhouseDataSourceConfig {

    @Value("${clickhouse.datasource.url}")
    private String url;

    @Bean
    public ConnectionFactory connectionFactory() {
        return get(url);
    }

}
