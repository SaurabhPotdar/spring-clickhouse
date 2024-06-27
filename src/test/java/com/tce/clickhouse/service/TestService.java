package com.tce.clickhouse.service;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class TestService {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("classpath:data.sql")
    private Resource initScript;

    @Value("classpath:clear.sql")
    private Resource clearScript;

    public void init() {
        init(initScript, connectionFactory);
    }

    public void clear() {
        init(clearScript, connectionFactory);
    }

    private void init(final Resource script, final ConnectionFactory connectionFactory) {
        try {
            final String sqlScriptContent = FileCopyUtils.copyToString(new InputStreamReader(script.getInputStream(), StandardCharsets.UTF_8));
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

}
