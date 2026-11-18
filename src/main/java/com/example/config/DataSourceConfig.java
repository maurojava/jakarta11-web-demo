package com.example.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
@DataSourceDefinition(
    name = "java:global/jdbc/dukesPool",
    className = "org.mariadb.jdbc.MariaDbDataSource",
    url = "jdbc:mariadb://${ENV=DB_HOST:localhost}:${ENV=DB_PORT:3306}/${ENV=DB_NAME:dukes_db}",
    user = "${ENV=DB_USER:duke}",
    password = "${ENV=DB_PASSWORD:secretpassword}"
)
public class DataSourceConfig {
}
