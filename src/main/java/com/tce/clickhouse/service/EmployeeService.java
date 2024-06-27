package com.tce.clickhouse.service;

import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Flux<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mono<Employee> findById(String id) {
        return employeeRepository.findByEmployeeId(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found with ID: " + id)));
    }

    @Transactional
    public Mono<Employee> save(Employee employee) {
        final String uuid = UUID.randomUUID().toString();
        employee.setId(uuid);
        return employeeRepository.save(employee)
                .thenReturn(employee);
    }

    @Transactional
    public Mono<Void> deleteById(final String id) {
        return employeeRepository.deleteById(id);
    }

    @Transactional
    public Mono<Void> testTransaction(Employee employee) {
        return employeeRepository.save(employee)
                .then(Mono.error(new RuntimeException("Transaction test exception")));
    }

}
