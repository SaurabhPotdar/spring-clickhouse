CREATE TABLE IF NOT EXISTS employee
(
    id UUID,
    name String,
    salary UInt16
)
ENGINE = MergeTree
PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS book
(
    id UUID,
    title String,
    price UInt16
)
ENGINE = MergeTree
PRIMARY KEY (id);