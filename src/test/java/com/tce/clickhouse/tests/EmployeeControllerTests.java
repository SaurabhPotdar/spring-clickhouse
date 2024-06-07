package com.tce.clickhouse.tests;

import com.tce.clickhouse.config.AbstractIntegrationTest;
import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeControllerTests extends AbstractIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    public void testCreateEmployee() {
        final Employee employee = new Employee();
        employee.setName("Saurabh");
        employee.setSalary(5000);

        final Employee response = webTestClient.post()
                .uri("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(employee)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Employee.class)
                .returnResult()
                .getResponseBody();

        //Assert response
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(employee.getName(), response.getName());
        assertEquals(employee.getSalary(), response.getSalary());

        //Fetch from DB and assert
        final Employee fetchedEmployee = employeeService.findById(response.getId()).block();
        assertNotNull(fetchedEmployee);
        assertEquals(response.getId(), fetchedEmployee.getId());
        assertEquals(response.getName(), fetchedEmployee.getName());
        assertEquals(response.getSalary(), fetchedEmployee.getSalary());

        //Cleanup
        employeeService.deleteById(response.getId())
                .then().block();
        assertThrows(RuntimeException.class, () -> employeeService.findById(response.getId()).block());
    }

    @Test
    public void testGetAllEmployees() {
        final List<Employee> employees = webTestClient.get()
                .uri("/employees")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Employee.class)
                .returnResult()
                .getResponseBody();
        assertFalse(CollectionUtils.isEmpty(employees));
        assertEquals(3, employees.size());
    }

    @Test
    public void testGetEmployeeById() {
        final String employeeId = "8c3bd10e-d01e-44c1-acd0-2c6f77bdec1b";
        final Employee employee = webTestClient.get()
                .uri("/employees/{id}", employeeId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Employee.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(employee);
        assertEquals(employeeId, employee.getId());
        assertEquals("David", employee.getName());
        assertEquals(200, employee.getSalary());
    }

}
