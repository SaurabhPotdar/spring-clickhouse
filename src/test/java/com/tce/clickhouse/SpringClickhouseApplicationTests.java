package com.tce.clickhouse;

import com.tce.clickhouse.config.TestContainersInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = TestContainersInitializer.class)
class SpringClickhouseApplicationTests {

    @Test
    public void testSimple() {
        System.out.println("Test simple");
    }

}
