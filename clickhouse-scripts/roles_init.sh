#!/bin/bash

databaseName="test_db"

readRoleCommands=(
"CREATE USER IF NOT EXISTS read_user IDENTIFIED WITH plaintext_password BY 'password'"
"CREATE ROLE IF NOT EXISTS read_role"
"GRANT SELECT ON $databaseName.* TO read_role"
"GRANT read_role TO read_user"
)

writeRoleCommands=(
"CREATE USER IF NOT EXISTS write_user IDENTIFIED WITH plaintext_password BY 'password'"
"CREATE ROLE IF NOT EXISTS write_role"
"GRANT SELECT, INSERT, UPDATE, DELETE ON $databaseName.* TO write_role"
"GRANT write_role TO write_user"
)

adminRoleCommands=(
"CREATE USER IF NOT EXISTS admin IDENTIFIED WITH plaintext_password BY 'password'"
"CREATE ROLE IF NOT EXISTS admin_role"
"GRANT ALL ON *.* TO admin_role WITH GRANT OPTION;"
"GRANT ALL PRIVILEGES ON *.* TO admin_role WITH GRANT OPTION"
"GRANT admin_role TO admin"
)

# Combine the arrays
roleCommands=("${readRoleCommands[@]}" "${writeRoleCommands[@]}" "${adminRoleCommands[@]}")

# Execute each SQL command separately
for sql_command in "${roleCommands[@]}"; do
  echo "$sql_command" | docker exec -i clickhouse-client clickhouse-client --host clickhouse
done