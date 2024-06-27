package com.tce.clickhouse.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories(
        basePackages = {
                "com.tce.clickhouse.entities",
                "com.tce.clickhouse.repository"
        }
)
@EnableTransactionManagement
@Slf4j
public class ClickhouseDataSourceConfig {

    @Bean
    public ConnectionFactory connectionFactory(@Value("${clickhouse.datasource.url}") String url) {
        final QueryExecutionInfoFormatter formatter = getQueryExecutionInfoFormatter();
        final ConnectionFactory connectionFactory = ProxyConnectionFactory.builder(ConnectionFactories.get(url))
                .onAfterQuery(queryInfo -> log.info(formatter.format(queryInfo)))
                .build();

        final ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxSize(20).initialSize(5)
                .maxCreateConnectionTime(Duration.ofSeconds(5))
                .maxIdleTime(Duration.ofMinutes(10))
                .maxLifeTime(Duration.ofHours(1))
                .build();
        return new ConnectionPool(configuration);
    }

    //https://r2dbc.io/2021/04/14/r2dbc-proxy-tips-query-logging
    private QueryExecutionInfoFormatter getQueryExecutionInfoFormatter() {
        //If we want to show all the information
        //final QueryExecutionInfoFormatter formatter = QueryExecutionInfoFormatter.showAll();
        return new QueryExecutionInfoFormatter()
                .addConsumer((info, sb) -> {
                    // custom conversion
                    sb.append("ConnID=");
                    sb.append(info.getConnectionInfo().getConnectionId());
                })
                .newLine()
                .showQuery()
                .newLine()
                .showBindings()
                .newLine();
    }

    @Bean
    public ReactiveTransactionManager transactionManager(final ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

}
