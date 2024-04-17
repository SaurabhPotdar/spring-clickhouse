package com.tce.clickhouse.repository;

import com.tce.clickhouse.entities.Employee;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class EmployeeRepository {

    @Autowired
    private ConnectionFactory connectionFactory;

    public Mono<Employee> findByEmployeeId(String id) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(conn -> conn.createStatement("SELECT id, name, salary FROM employee WHERE id = :id")
                        .bind("id", id)
                        .execute())
                .flatMap(result -> result.map((row, rowMetadata) -> new Employee(row
                        .get("id", String.class), row.get("name", String.class), row.get("salary", Integer.class))))
                .singleOrEmpty();
    }

    public Flux<Employee> findAll() {
        return Mono.from(connectionFactory.create())
                .flatMapMany(conn -> conn.createStatement("SELECT id, name, salary FROM employee").execute())
                .flatMap(result -> result.map((row, rowMetadata) -> new Employee(row
                        .get("id", String.class), row.get("name", String.class), row.get("salary", Integer.class))));
    }

    public Mono<Void> save(Employee employee) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(conn -> execute(employee, conn)).then();
    }

    private Publisher<? extends Result> execute(Employee employee, Connection conn) {
        return conn.createStatement("insert into test_db.employee values (:id, :name, :salary)")
                .bind("id", employee.getId())
                .bind("name", employee.getName())
                .bind("salary", employee.getSalary())
                .execute();
    }

    public Mono<Void> delete() {
        return Mono.from(connectionFactory.create())
                .flatMapMany(conn -> conn.createStatement("TRUNCATE TABLE employee").execute()).then();
    }

}
