package com.projectmanager.sv.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DatabaseManager {

    public static final String JDBC_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;

    static {
        Properties properties = new Properties();
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/h2config.properties")) {
            if (is == null) {
                throw new IllegalStateException("Missing /h2config.properties");
            }
            properties.load(is);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        JDBC_URL = properties.getProperty("db.url");
        DB_USER = properties.getProperty("db.user", "sa");
        DB_PASSWORD = properties.getProperty("db.password", "");
    }

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        log.debug("Opening H2 connection");
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    public static void initializeDatabase() {
        log.info("Initializing H2 database at {}", JDBC_URL);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String ddl = loadResource("/db/schema.sql");
            for (String statement : ddl.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

            try (ResultSet rs = stmt.executeQuery("select count(*) from projects")) {
                if (rs.next() && rs.getLong(1) == 0) {
                    seedSampleData(stmt);
                }
            }
        } catch (Exception e) {
            log.error("H2 database initialization failed", e);
            throw new RuntimeException("Error initializing H2 database", e);
        }
    }

    private static void seedSampleData(Statement stmt) throws SQLException {
        stmt.execute("insert into projects (title, mission) values ('Billing Core', 'Core payments')");
        stmt.execute("insert into projects (title, mission) values ('Mobile Portal', 'Client app')");

        stmt.execute("insert into sprints (sprint_number, start_date, end_date) values (1, date '2026-11-02', date '2026-11-15')");
        stmt.execute("insert into sprints (sprint_number, start_date, end_date) values (2, date '2026-11-16', date '2026-11-29')");

        stmt.execute("insert into deliverables (project_id, name, goal) values (1, 'Invoice Engine', 'Generate and validate invoices')");
        stmt.execute("insert into deliverables (project_id, name, goal) values (1, 'Payment Gateway', 'Process card charges')");
        stmt.execute("insert into deliverables (project_id, name, goal) values (2, 'User Login', 'Authenticate customer accounts')");

        stmt.execute("insert into deliverable_tasks (deliverable_id, sprint_id, title, progress) values (1, 1, 'Design invoice schema', 100.00)");
        stmt.execute("insert into deliverable_tasks (deliverable_id, sprint_id, title, progress) values (1, 2, 'Implement tax calculation', 50.00)");
        stmt.execute("insert into deliverable_tasks (deliverable_id, sprint_id, title, progress) values (2, 2, 'Integrate card processor', 0.00)");
        stmt.execute("insert into deliverable_tasks (deliverable_id, sprint_id, title, progress) values (3, 1, 'Build login screen', 100.00)");
    }

    private static String loadResource(String path) throws Exception {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
