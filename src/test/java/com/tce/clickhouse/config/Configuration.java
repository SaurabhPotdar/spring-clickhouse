package com.tce.clickhouse.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(
        basePackages = {
                "com.tce.clickhouse.entities",
                "com.tce.clickhouse.repository"
        }
)
public class Configuration {
}
