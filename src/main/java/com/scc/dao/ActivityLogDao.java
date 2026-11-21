package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void recordLog(int userId, String actionPerformed, String eventDetails, String ipAddress) {
        String sql = """
            INSERT INTO Audit_Logs (user_id, action_performed, event_details, timestamp, ip_address)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionPerformed);
            ps.setString(3, eventDetails);
            ps.setString(4, LocalDateTime.now().toString());
            ps.setString(5, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM Audit_Logs ORDER BY log_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("action_performed"),
                        rs.getString("event_details"),
                        LocalDateTime.parse(rs.getString("timestamp")),
                        rs.getString("ip_address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading audit logs: " + e.getMessage());
        }
        return logs;
    }
}