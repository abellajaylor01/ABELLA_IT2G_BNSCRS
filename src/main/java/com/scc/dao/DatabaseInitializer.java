package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS System_Users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                user_role TEXT NOT NULL CHECK (user_role IN ('Admin', 'Staff', 'Viewer')),
                date_created TEXT NOT NULL
            );
            """;

        String createSeniors = """
            CREATE TABLE IF NOT EXISTS Senior_Citizens (
                senior_id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                birthdate TEXT NOT NULL,
                age INTEGER NOT NULL,
                gender TEXT NOT NULL,
                purok_address TEXT NOT NULL,
                contact_number TEXT,
                emergency_contact_name TEXT,
                emergency_contact_phone TEXT,
                health_conditions TEXT,
                status TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Archived'))
            );
            """;

        String createSeniorIds = """
            CREATE TABLE IF NOT EXISTS Senior_IDs (
                id_card_id INTEGER PRIMARY KEY AUTOINCREMENT,
                senior_id INTEGER NOT NULL,
                issue_date TEXT NOT NULL,
                expiry_date TEXT NOT NULL,
                id_status TEXT NOT NULL,
                qr_barcode_code TEXT UNIQUE NOT NULL,
                FOREIGN KEY (senior_id) REFERENCES Senior_Citizens(senior_id)
            );
            """;

        String createBenefits = """
            CREATE TABLE IF NOT EXISTS Benefit_Transactions (
                transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                senior_id INTEGER NOT NULL,
                benefit_type TEXT NOT NULL CHECK (benefit_type IN ('Financial Assistance', 'Medicine', 'Pension')),
                amount REAL NOT NULL,
                release_date TEXT NOT NULL,
                claim_status TEXT NOT NULL CHECK (claim_status IN ('Claimed', 'Pending')),
                processed_by_user_id INTEGER NOT NULL,
                FOREIGN KEY (senior_id) REFERENCES Senior_Citizens(senior_id),
                FOREIGN KEY (processed_by_user_id) REFERENCES System_Users(user_id)
            );
            """;

        String createLogs = """
            CREATE TABLE IF NOT EXISTS Audit_Logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_performed TEXT NOT NULL,
                event_details TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                ip_address TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES System_Users(user_id)
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createSeniors);
            stmt.execute(createSeniorIds);
            stmt.execute(createBenefits);
            stmt.execute(createLogs);

            new UserDao().seedDefaultUsers();
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}