package com.tce.clickhouse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "employee", schema = "test_db")
public class Employee {

    @Id
    private String id;

    private String name;

    private int salary;

}
