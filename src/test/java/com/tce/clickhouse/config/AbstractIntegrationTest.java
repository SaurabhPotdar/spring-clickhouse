package com.tce.clickhouse.config;

import com.tce.clickhouse.service.TestService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestContainersInitializer.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractIntegrationTest {

    @Autowired
    private TestService testService;

    @Autowired
    public WebTestClient webTestClient;

    @BeforeAll
    public void setUp() {
        testService.executeScriptBlocking();
    }

}
