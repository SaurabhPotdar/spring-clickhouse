package com.tce.clickhouse.controller;

import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public Mono<ResponseEntity<Employee>> createEmployee(@RequestBody Employee employee) {
        return employeeService.save(employee)
                .map(e -> ResponseEntity.status(201).body(e));
    }

    @GetMapping
    public Flux<Employee> getAllEmployees() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Employee> getEmployeeById(@PathVariable(value = "id") String employeeId) {
        return employeeService.findById(employeeId);
    }

}
