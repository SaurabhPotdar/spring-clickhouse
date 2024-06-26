# ClickHouse

ClickHouse is an open-source column-oriented DBMS for online analytical processing (OLAP). It is capable of real-time generation of analytical data reports using SQL queries.

Postgres is an OLTP database, while ClickHouse is an OLAP database. OLTP databases are designed for transactional processing, while OLAP databases are designed for analytical processing.

## Row-Oriented vs. Column-Oriented

In row oriented, data is stored in a row. We have int, bool, string in a row, and we need padding to make sure that all rows have the same size.

In column oriented, data is stored in a column. So the int column is stored together in a file, the bool column is stored together in a file. We can compress it better because the data is similar. The name of the file is the name of the column.

What is the price of phone 102, clickhouse will have to figure out id 102 is the 13th row and return the 13th row from price file. But that's not an analytical question. We are using clickhouse to get avg price of all phones.

## Data format

ClickHouse supports formats like CSV, TSV, and JSON. It also supports custom formats like Avro, ORC, and Parquet. We can get data from S3, databases, Kafka, and other sources.

# Data types

## Nullable

```sql
CREATE TABLE my_table (
    id Int32,  -- If value is not passed it will be 0
    name String,
    age Int32
    metric Nullable(Float64)  -- If value is not passed it will be null, when calculating the average of a column, ClickHouse will ignore NULL values.
) ENGINE = MergeTree()
ORDER BY id;
-- For each nullable column, ClickHouse stores an additional byte per value to indicate whether the value is NULL or not. This will increase the size of the table.
```

## Arrays
```sql
CREATE TABLE my_table (
    id Int32,
    name String,
    tags Array(String)
) ENGINE = MergeTree()
ORDER BY id;

INSERT INTO my_table (id, name, tags) VALUES
(1, 'Alice', ['tag1', 'tag2']),
(2, 'Bob', ['tag3']);
```

## Enum

Clickhouse will internally store the enum as an integer. It will store the string in the dictionary and the integer in the table.

```sql
CREATE TABLE my_table (
    id Int32,
    name String,
    status Enum('active' = 1, 'inactive' = 0)
) ENGINE = MergeTree()
ORDER BY id;
```

## Low cardinality

Useful when you have a column with a small number of distinct values (say 10000). Clickhouse will store values as integers and use a dictionary to map integers to strings.

```sql
CREATE TABLE my_table (
    id Int32,
    name String,
    status LowCardinality(String)
) ENGINE = MergeTree()

INSERT INTO my_table (id, name, status) VALUES
(1, 'Alice', 'active'),
(2, 'Bob', 'inactive');
```

## Other datatypes

- Int8, Int16, Int32, Int64
- UInt8, UInt16, UInt32, UInt64
- Float32, Float64
- Decimal
- Date, DateTime
- UUID

# Primary key

For merge tree engine, the primary key is used to sort the data (it is the same as order by). If we have multiple columns in a primary key, then put the low cardinality column first (ordered by cardinality in ascending order).

```
StudentID   Lastname Firstname  Gender
 101         Smith    John       M
 102         Jones    James      M
 103         Mayo     Ann        F
 104         Jones    George     M
 105         Smith    Suse       F
 Here studentId has the highest cardinality, while gender has the lowest cardinality.
 ```

# MergeTree Architecture

We can have multiple rows with the same primary key.

Inserts should be performed in bulk, we can also use async inserts feature. Each bulk insert creates a part. A part is stored in its own folder, and each part can have millions of rows.

![Alt Text](photos/parts-1.jpg)

![Alt Text](photos/parts-2.jpg)

![Alt Text](photos/parts-3.png)

Each data part is divided into granules. Each granule has a min and max primary key value. Each granule can have 8192 rows.

When we query ClickHouse which parts/granules to read based on a primary key. So it is necessary to have primary key in increasing order of cardinality.

![Alt Text](photos/granule.png)

In the above example, if we query for (A,2), then ClickHouse will read granule 2 and 3.

# Table engines

ClickHouse will merge data asynchronously in the background. The merge policy controls the merge process. The merge policy is defined by the MergeTree engine.

1. MergeTree

    ```sql
    CREATE TABLE IF NOT EXISTS employee (
        id UInt32,
        name String,
        value UInt32
    ) ENGINE = MergeTree()
    ORDER BY (id);
     
    INSERT INTO employee (id, name, value) VALUES (1, 'A', 10), (1, 'B', 20), (1, 'C', 30), (2, 'D', 40);
    ```
    ```sql
    SELECT * FROM employee where id = 1;  -- Returns 3 rows
    ```
    ```sql
    SELECT SUM(value) FROM employee;  -- Returns 100
    ```
    ```sql
    SELECT * FROM employee FINAL where id = 1;  -- Throws an error
    ```
2. SummingMergeTree

   Summarizes values for the columns with the numeric data type.

    ```sql
    CREATE TABLE IF NOT EXISTS employee (
        id UInt32,
        name String,
        value UInt32
    ) ENGINE = SummingMergeTree()
    ORDER BY (id);

    INSERT INTO employee (id, name, value) VALUES (1, 'A', 10), (1, 'B', 20), (1, 'C', 30), (2, 'D', 40);
    ```
    ```sql
    SELECT * FROM employee where id = 1;  -- Returns 1 rows (1, 'A', 60)

    SELECT SUM(value) FROM employee;  -- Returns 100
    ```

3. AggregatingMergeTree

    ```sql
    CREATE TABLE IF NOT EXISTS employee (
        id UInt32,
        name String,
        value UInt32
    ) ENGINE = MergeTree()
    ORDER BY (id);
    ```
    ```sql
    CREATE TABLE IF NOT EXISTS employee_agg (
        id UInt32,
        value AggregateFunction(sum, UInt32)
    ) ENGINE = AggregatingMergeTree()
    ORDER BY (id);
    ```
    ```sql
    -- Every time we insert into employee, the employee_agg table will be updated
    CREATE MATERIALIZED VIEW employee_mv TO employee_agg AS SELECT id, sumState(value) AS value FROM employee GROUP BY id;

    INSERT INTO employee (id, name, value) VALUES (1, 'A', 10), (1, 'B', 20), (1, 'C', 30), (2, 'D', 40);
    ```
    ```sql
    SELECT id, sumMerge(value) AS values FROM employee_agg GROUP BY id HAVING id = 1

    SELECT id, sumMerge(value) AS values FROM employee_agg GROUP BY id;
    ```

4. ReplacingMergeTree

   removes duplicate entries with the same sorting key value (ORDER BY table section, not PRIMARY KEY)

    ```sql
    CREATE TABLE IF NOT EXISTS employee (
        id UInt32,
        name String,
        value UInt32
    ) ENGINE = ReplacingMergeTree()
    ORDER BY (id);

    INSERT INTO employee (id, name, value) VALUES (1, 'A', 10), (1, 'B', 30), (1, 'C', 20), (2, 'D', 40);
    ```
    ```sql
    SELECT * FROM employee where id = 1;  -- Returns last inserted row (1, 'C', 20)
    ```
    ```sql
    SELECT SUM(value) FROM employee;  -- Returns 60
    ```

   ver — column with the version number.\
   Type UInt*, Date, DateTime or DateTime64. Optional parameter.\
   Returns row with max ver column.
    ```sql
    CREATE TABLE IF NOT EXISTS employee (
        id UInt32,
        name String,
        value UInt32
    ) ENGINE = ReplacingMergeTree()
    ORDER BY (value);

    INSERT INTO employee (id, name, value) VALUES (1, 'A', 10), (1, 'B', 30), (1, 'C', 20), (2, 'D', 40);
    ```
    ```sql
    SELECT * FROM employee where id = 1;  -- Returns row with max ver column (1, 'B', 30)
    ```
    ```sql
    SELECT SUM(value) FROM employee;  -- Returns 70
    ```