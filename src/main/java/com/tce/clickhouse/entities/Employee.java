package com.tce.clickhouse.entities;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Employee {

    private int id;

    private String name;

    private int salary;

}
