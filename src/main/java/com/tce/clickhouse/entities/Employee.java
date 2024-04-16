package com.tce.clickhouse.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;

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
