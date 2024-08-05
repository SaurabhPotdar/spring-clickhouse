package com.tce.clickhouse.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> CLICKHOUSE_CONTAINER;

    static {
        final DockerImageName imageName = DockerImageName.parse("clickhouse/clickhouse-server:latest");
        CLICKHOUSE_CONTAINER = new GenericContainer<>(imageName);
        CLICKHOUSE_CONTAINER.withExposedPorts(8123);
        CLICKHOUSE_CONTAINER.start();

        try {
            CLICKHOUSE_CONTAINER.execInContainer(
                    "clickhouse-client", "--query", "CREATE DATABASE IF NOT EXISTS test_db ENGINE = Atomic;"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create database in ClickHouse container", e);
        }
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        TestPropertyValues values = TestPropertyValues.of(
                "spring.datasource.clickhouse.url=" + "jdbc:clickhouse://" + CLICKHOUSE_CONTAINER.getHost() + ":" + CLICKHOUSE_CONTAINER.getMappedPort(8123),
                "spring.datasource.clickhouse.username=" + "default",
                "spring.datasource.clickhouse.password="
        );
        values.applyTo(applicationContext);
    }

}