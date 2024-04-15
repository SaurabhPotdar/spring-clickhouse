package com.tce.clickhouse.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.clickhouse.config.TestContainersInitializer;
import com.tce.clickhouse.entities.Employee;
import com.tce.clickhouse.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(initializers = TestContainersInitializer.class)
@AutoConfigureMockMvc
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class EmployeeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    public void setup() {
        employeeService.deleteAll();
    }

    @DisplayName("Create Employee")
    @Test
    public void testCreateEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setSalary(1000);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated());

        List<Employee> employees = employeeService.findAll();
        assertEquals(1, employees.size());
        Employee savedEmployee = employees.get(0);
        assertEquals("John Doe", savedEmployee.getName());
        assertEquals(1000, savedEmployee.getSalary());
    }

    @Test
    @DisplayName("Find all employees")
    public void testGetAllEmployees() throws Exception {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setSalary(1000);
        employeeService.save(employee);

        MvcResult mvcResult = mockMvc.perform(get("/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<Employee> response = objectMapper.readerForListOf(Employee.class).readValue(mvcResult.getResponse().getContentAsString());
        assertTrue(response != null && response.size() == 1);
        response.forEach(e -> assertNotNull(e.getId()));
    }

    @Test
    @DisplayName("Find by id")
    public void testGetEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setSalary(1000);
        final String id = employeeService.save(employee).getId();

        MvcResult mvcResult = mockMvc.perform(get("/employees/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Employee savedEmployee = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Employee.class);
        assertNotNull(savedEmployee);
        assertEquals(id, savedEmployee.getId());
        assertEquals(employee.getName(), savedEmployee.getName());
        assertEquals(employee.getSalary(), savedEmployee.getSalary());
    }

}
