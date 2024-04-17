package com.tce.clickhouse.tests;

import com.tce.clickhouse.config.TestContainersInitializer;
import com.tce.clickhouse.service.EmployeeService;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestContainersInitializer.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeControllerTests {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("classpath:data.sql")
    private Resource sqlScript;

    @Autowired
    private EmployeeService employeeService;

    private void executeScriptBlocking() {
        try {
            final String sqlScriptContent = FileCopyUtils.copyToString(new InputStreamReader(sqlScript.getInputStream(), StandardCharsets.UTF_8));
            // Split the script into individual statements
            final String[] sqlStatements = sqlScriptContent.split(";");

            // Create a connection
            Mono.from(connectionFactory.create())
                    .flatMapMany(connection -> {
                        // Convert each SQL statement into a Publisher
                        return Flux.fromArray(sqlStatements)
                                .filter(sql -> !sql.trim().isEmpty())  // Ignore empty statements
                                .concatMap(sql -> {
                                    // Execute each SQL statement and wait for its completion before executing the next one
                                    return connection.createStatement(sql.trim()).execute();
                                });
                    })
                    .then()
                    .block();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL script", e);
        }
    }

    @BeforeAll
    public void rollOutTestData() {
        executeScriptBlocking();
    }

    @Test
    public void testFindAll() {
        employeeService.findAll().count().subscribe(count -> {
            assertEquals(3, count);
        });
    }

}
