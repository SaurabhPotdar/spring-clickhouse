CREATE DATABASE IF NOT EXISTS test_db ENGINE = Atomic;

CREATE TABLE IF NOT EXISTS test_db.employee
(
    id UUID,
    name String,
    salary UInt16
)
ENGINE = MergeTree
PRIMARY KEY (id);

INSERT INTO employee (id, name, salary) VALUES
(generateUUIDv4(), 'James', 100),
(generateUUIDv4(), 'David', 200),
(generateUUIDv4(), 'Bob', 300);