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

        // Create a database and use that in the url
        try {
            CLICKHOUSE_CONTAINER.execInContainer(
                    "clickhouse-client",
                    "--query",
                    "CREATE DATABASE IF NOT EXISTS test_db ENGINE = Atomic;"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create database in ClickHouse container", e);
        }
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        //This will set properties which are used by ClickhouseDataSourceConfig
        //r2dbc:clickhouse:http://{username}:{password}@{host}:{port}/{database}"
        final String url = String.format("r2dbc:clickhouse:http://%s:%s@%s:%s/test_db", "default", "",
                CLICKHOUSE_CONTAINER.getHost(), CLICKHOUSE_CONTAINER.getMappedPort(8123));
        final String flywayUrl = String.format("jdbc:clickhouse://%s:%s/test_db", CLICKHOUSE_CONTAINER.getHost(),
                CLICKHOUSE_CONTAINER.getMappedPort(8123));
        TestPropertyValues values = TestPropertyValues.of(
                "clickhouse.datasource.url=" + url,
                "spring.flyway.url=" + flywayUrl,
                "spring.flyway.user=default",
                "spring.flyway.password="
        );
        values.applyTo(applicationContext);
    }

}