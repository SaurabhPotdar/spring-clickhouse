package com.tce.clickhouse.tests.service;

import com.tce.clickhouse.config.AbstractIntegrationTest;
import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceTests extends AbstractIntegrationTest {

    @SpyBean
    private EmployeeService employeeService;

    @BeforeAll
    public void setUp() {
        testService.init();
    }

    @AfterAll
    public void tearDown() {
        testService.clear();
    }

    @DisplayName("Find all")
    @Test
    public void testFindAll() {
        final List<Employee> employees = employeeService.findAll().collectList().block();
        assertNotNull(employees);
        assertEquals(3, employees.size());
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Find By Id")
    class FindById {

        @DisplayName("Valid Id")
        @Test
        public void validId() {
            final String employeeId = "8c3bd10e-d01e-44c1-acd0-2c6f77bdec1b";
            final Employee employee = assertDoesNotThrow(() -> employeeService.findById(employeeId).block());
            assertNotNull(employee);
            assertEquals(employeeId, employee.getId());
            assertEquals("David", employee.getName());
            assertEquals(200, employee.getSalary());
        }

        @DisplayName("Invalid Id")
        @Test
        public void invalidId() {
            assertThrows(RuntimeException.class, () -> employeeService.findById("invalid-id").block());
        }

    }

    @Test
    @DisplayName("Save")
    void testSave() {
        final Employee employee = new Employee();
        employee.setName("Saurabh");
        employee.setSalary(1000);
        final Employee savedEmployee = employeeService.save(employee).block();

        //Assert response
        assertNotNull(savedEmployee);
        assertNotNull(savedEmployee.getId());
        assertEquals("Saurabh", savedEmployee.getName());
        assertEquals(1000, savedEmployee.getSalary());

        //Assert database
        final Employee employeeFromDb = assertDoesNotThrow(() -> employeeService.findById(savedEmployee.getId()).block());
        assertEquals(savedEmployee.getId(), employeeFromDb.getId());
        assertEquals("Saurabh", employeeFromDb.getName());
        assertEquals(1000, employeeFromDb.getSalary());

        // Clean up
        employeeService.deleteById(savedEmployee.getId()).block();
        assertThrows(RuntimeException.class, () -> employeeService.findById(savedEmployee.getId()).block());
    }

    @DisplayName("Delete by id")
    @Test
    public void deleteById() {
        final Employee employee = new Employee();
        employee.setName("Saurabh");
        employee.setSalary(4000);
        final Employee savedEmployee = employeeService.save(employee).block();
        assertNotNull(savedEmployee);

        final String employeeId = savedEmployee.getId();
        assertDoesNotThrow(() -> employeeService.findById(employeeId).block());

        employeeService.deleteById(employeeId).block();
        assertThrows(RuntimeException.class, () -> employeeService.findById(employeeId).block());
        assertEquals(3, employeeService.findAll().count().block());
    }

    @Test
    @DisplayName("Test transaction")
    void testTransaction() {
        List<Employee> employees = employeeService.findAll().collectList().block();
        assertNotNull(employees);
        final int size = employees.size();

        final Employee employee = new Employee();
        employee.setName("Saurabh");
        employee.setSalary(4000);
        employeeService.testTransaction(employee).onErrorResume(e -> Mono.empty()).block();

        employees = employeeService.findAll().collectList().block();
        assertNotNull(employees);
        assertEquals(size, employees.size());
    }

}
