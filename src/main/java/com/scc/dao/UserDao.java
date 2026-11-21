package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDao {

    public void seedDefaultUsers() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM System_Users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                createUser("admin", "admin123", "Barangay Captain / Admin", "Admin");
                createUser("staff1", "staff123", "Records Encoder One", "Staff");
                createUser("viewer1", "viewer123", "Senior Resident Viewer", "Viewer");
            }
        }
    }

    public boolean createUser(String username, String plainPassword, String fullName, String role) {
        String sql = """
            INSERT INTO System_Users (username, password_hash, full_name, user_role, date_created)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, SecurityUtil.hashPassword(plainPassword));
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.setString(5, LocalDateTime.now().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> authenticate(String username, String plainPassword) {
        String sql = "SELECT * FROM System_Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, SecurityUtil.hashPassword(plainPassword));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("user_role"),
                            LocalDateTime.parse(rs.getString("date_created"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return Optional.empty();
    }
}