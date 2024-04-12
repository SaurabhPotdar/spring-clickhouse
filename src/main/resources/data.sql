CREATE TABLE employee
(
    employee_id UInt32,
    name String,
    salary UInt16
)
ENGINE = MergeTree
PRIMARY KEY (employee_id)

INSERT INTO employee (employee_id, name,salary) VALUES
(101, 'Saurabh', 100),
(102, 'Rohit', 200),
(103, 'Anuja', 300)