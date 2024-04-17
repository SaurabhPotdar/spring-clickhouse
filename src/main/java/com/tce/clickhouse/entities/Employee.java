package com.tce.clickhouse.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "employee", schema = "test_db")
public class Employee {

    @Id
    private String id;

    private String name;

    private Integer salary;

}