package com.tce.clickhouse.repository;

import com.tce.clickhouse.entities.Employee;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

    private final EntityManager entityManager;

    public List<Employee> findAll() {
        String sql = "SELECT id, name, salary FROM employee";
        return (List<Employee>) entityManager.createNativeQuery(sql, Employee.class)
                .getResultList();
    }

    public Optional<Employee> findById(String id) {
        String sql = "SELECT id, name, salary FROM employee WHERE id = :id";
        Employee employee = (Employee) entityManager.createNativeQuery(sql, Employee.class)
                .setParameter("id", id)
                .getSingleResult();
        return Optional.ofNullable(employee);
    }

    public Employee save(Employee employee) {
        String sql = "INSERT INTO employee (id, name, salary) VALUES (:id, :name, :salary)";
        final String uuid = UUID.randomUUID().toString();
        entityManager.createNativeQuery(sql)
                .setParameter("id", uuid)
                .setParameter("name", employee.getName())
                .setParameter("salary", employee.getSalary())
                .executeUpdate();
        employee.setId(uuid);
        return employee;
    }

    public void deleteAll() {
        String sql = "TRUNCATE TABLE employee";
        entityManager.createNativeQuery(sql).executeUpdate();
    }

}
