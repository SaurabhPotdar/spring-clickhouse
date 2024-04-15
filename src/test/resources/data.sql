CREATE TABLE IF NOT EXISTS default.employee
(
    id UUID,
    name String,
    salary UInt16
)
ENGINE = MergeTree
PRIMARY KEY (id);