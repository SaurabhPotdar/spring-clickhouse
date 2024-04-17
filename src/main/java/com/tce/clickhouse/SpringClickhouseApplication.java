package com.tce.clickhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class
})
@EnableWebFlux
public class SpringClickhouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringClickhouseApplication.class, args);
    }

}
