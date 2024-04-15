package com.tce.clickhouse.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final ClickHouseContainer CLICKHOUSE_CONTAINER;

    static {
        DockerImageName imageName = DockerImageName.parse("clickhouse/clickhouse-server:latest");
        CLICKHOUSE_CONTAINER = new ClickHouseContainer(imageName);
        CLICKHOUSE_CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues values = TestPropertyValues.of(
                "spring.datasource.url=" + CLICKHOUSE_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + CLICKHOUSE_CONTAINER.getUsername(),
                "spring.datasource.password=" + CLICKHOUSE_CONTAINER.getPassword()
        );
        values.applyTo(applicationContext);
    }

}