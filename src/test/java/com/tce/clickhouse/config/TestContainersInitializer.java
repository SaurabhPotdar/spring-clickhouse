package com.tce.clickhouse.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final GenericContainer<?> CLICKHOUSE_CONTAINER;

    static {
        final DockerImageName imageName = DockerImageName.parse("clickhouse/clickhouse-server:latest");
        CLICKHOUSE_CONTAINER = new GenericContainer<>(imageName);
        CLICKHOUSE_CONTAINER.withExposedPorts(8123);
        CLICKHOUSE_CONTAINER.withEnv("CLICKHOUSE_USER", "default");
        CLICKHOUSE_CONTAINER.withEnv("CLICKHOUSE_PASSWORD", "");
        CLICKHOUSE_CONTAINER.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        //This will set properties which are used by ClickhouseDataSourceConfig
        //r2dbc:clickhouse:http://{username}:{password}@{host}:{port}/{database}"
        final String url = String.format("r2dbc:clickhouse:http://%s:%s@%s:%s", "default", "",
                CLICKHOUSE_CONTAINER.getHost(), CLICKHOUSE_CONTAINER.getMappedPort(8123));
        TestPropertyValues values = TestPropertyValues.of(
                "clickhouse.datasource.url=" + url
        );
        values.applyTo(applicationContext);
    }

}