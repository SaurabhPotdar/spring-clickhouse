package com.tce.clickhouse.tests.controller;

import com.tce.clickhouse.controller.EmployeeController;
import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(EmployeeController.class)
public class EmployeeControllerTests {

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Create employee")
    void createEmployee() {
        final Employee employee = new Employee("emp-1", "John Doe", 100);
        when(employeeService.save(any(Employee.class))).thenReturn(Mono.just(employee));

        webTestClient.post().uri("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(employee)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Employee.class);
    }

    @Test
    @DisplayName("Get all employees")
    void getAllEmployees() {
        final Employee employee1 = new Employee("emp-1", "John Doe", 100);
        final Employee employee2 = new Employee("emp-2", "Jane Doe", 200);
        when(employeeService.findAll()).thenReturn(Flux.just(employee1, employee2));

        webTestClient.get().uri("/employees")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Employee.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("Get employee by id")
    void getEmployeeById() {
        final Employee employee = new Employee("emp-1", "John Doe", 100);
        when(employeeService.findById("emp-1")).thenReturn(Mono.just(employee));

        webTestClient.get().uri("/employees/emp-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Employee.class)
                .isEqualTo(employee);
    }

}
