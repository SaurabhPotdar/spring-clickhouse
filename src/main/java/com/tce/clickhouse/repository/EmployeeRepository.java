package com.tce.clickhouse.repository;

import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.util.DatabaseUtils;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class EmployeeRepository {

    @Autowired
    private ConnectionFactory connectionFactory;

    public Mono<Employee> findByEmployeeId(String id) {
        return DatabaseUtils.executeQuery(connectionFactory,
                        "SELECT * FROM employee WHERE id = :id",
                        Map.of("id", id))
                .flatMap(result -> result.map((row, rowMetadata) -> new Employee(row
                        .get("id", String.class), row.get("name", String.class), row.get("salary", Integer.class))))
                .singleOrEmpty();
    }

    public Flux<Employee> findAll() {
        return DatabaseUtils.executeQuery(connectionFactory, "SELECT * FROM employee")
                .flatMap(result -> result.map((row, rowMetadata) -> new Employee(row.get("id", String.class),
                        row.get("name", String.class), row.get("salary", Integer.class))));
    }

    public Mono<Void> save(Employee employee) {
        return DatabaseUtils.executeQuery(connectionFactory,
                        "INSERT INTO employee (id, name, salary) VALUES (:id, :name, :salary)",
                        Map.of(
                                "id", employee.getId(),
                                "name", employee.getName(),
                                "salary", employee.getSalary()))
                .then();
    }

    public Mono<Void> delete() {
        return DatabaseUtils.executeQuery(connectionFactory,
                "TRUNCATE TABLE employee").then();
    }

    public Mono<Void> deleteById(String id) {
        return DatabaseUtils.executeQuery(connectionFactory,
                        "DELETE FROM employee WHERE id = :id",
                        Map.of("id", id))
                .then();
    }

}
