package com.tce.clickhouse.util;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class DatabaseUtils {

    public static Flux<Result> executeQuery(final ConnectionFactory connectionFactory, final String query) {
        return executeQuery(connectionFactory, query, Collections.emptyMap());
    }

    public static Flux<Result> executeQuery(final ConnectionFactory connectionFactory, final String query, final Map<String, Object> bindings) {
        return Flux.usingWhen(
                connectionFactory.create(),
                conn -> {
                    var statement = conn.createStatement(query);
                    bindings.forEach(statement::bind);
                    return statement.execute();
                },
                Connection::close
        );
    }

}