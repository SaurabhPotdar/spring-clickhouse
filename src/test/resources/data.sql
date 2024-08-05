CREATE TABLE IF NOT EXISTS test_db.employee
(
    id UUID,
    name String,
    salary UInt16
)
ENGINE = MergeTree
PRIMARY KEY (id);