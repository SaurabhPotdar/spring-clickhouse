# Use the official Apache Superset image as a base
FROM apache/superset

# Switch to the root user so we can install packages
USER root

# Install the necessary drivers
RUN apt-get update && apt-get install -y wget
RUN pip install clickhouse-connect

# Add the superset_config.py file to the image
COPY superset_config.py /app/superset/superset_config.py

# Add the roles_init.py file to the image
COPY roles_init.py /app/roles_init.py
RUN chmod +x /app/roles_init.py

# Switch back to using the `superset` user
USER superset