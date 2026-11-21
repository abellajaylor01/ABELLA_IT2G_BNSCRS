package com.scc.dao;

import com.scc.model.BenefitTransaction;
import com.scc.model.SeniorCitizen;
import com.scc.model.SeniorId;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeniorRecordDao {

    public int registerSenior(SeniorCitizen s) {
        int calculatedAge = Period.between(s.getBirthdate(), LocalDate.now()).getYears();
        s.setAge(calculatedAge);
        s.setStatus("Active");

        String sql = """
            INSERT INTO Senior_Citizens
            (first_name, last_name, birthdate, age, gender, purok_address,
             contact_number, emergency_contact_name, emergency_contact_phone,
             health_conditions, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getBirthdate().toString());
            ps.setInt(4, s.getAge());
            ps.setString(5, s.getGender());
            ps.setString(6, s.getPurokAddress());
            ps.setString(7, s.getContactNumber());
            ps.setString(8, s.getEmergencyContactName());
            ps.setString(9, s.getEmergencyContactPhone());
            ps.setString(10, s.getHealthConditions());
            ps.setString(11, s.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    issueInitialSeniorId(conn, newId);
                    return newId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error registering senior: " + e.getMessage());
        }
        return -1;
    }

    private void issueInitialSeniorId(Connection conn, int seniorId) throws SQLException {
        String code = "NAGA-SC-" + String.format("%05d", seniorId);
        LocalDate issue = LocalDate.now();
        LocalDate expiry = issue.plusYears(3);

        String sql = """
            INSERT INTO Senior_IDs (senior_id, issue_date, expiry_date, id_status, qr_barcode_code)
            VALUES (?, ?, ?, 'Pending', ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            ps.setString(2, issue.toString());
            ps.setString(3, expiry.toString());
            ps.setString(4, code);
            ps.executeUpdate();
        }
    }

    public List<SeniorCitizen> searchSeniors(String keyword) {
        List<SeniorCitizen> results = new ArrayList<>();
        String sql = """
            SELECT * FROM Senior_Citizens
            WHERE CAST(senior_id AS TEXT) LIKE ?
               OR LOWER(last_name) LIKE LOWER(?)
               OR LOWER(purok_address) LIKE LOWER(?)
            """;
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapSenior(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
        return results;
    }

    public Optional<SeniorCitizen> findById(int seniorId) {
        String sql = "SELECT * FROM Senior_Citizens WHERE senior_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSenior(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding senior: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean updateProfile(int seniorId, String contact, String purok,
                                 String healthConditions, String emergName, String emergPhone) {
        String sql = """
            UPDATE Senior_Citizens
            SET contact_number = ?, purok_address = ?, health_conditions = ?,
                emergency_contact_name = ?, emergency_contact_phone = ?
            WHERE senior_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contact);
            ps.setString(2, purok);
            ps.setString(3, healthConditions);
            ps.setString(4, emergName);
            ps.setString(5, emergPhone);
            ps.setInt(6, seniorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateIdStatus(int seniorId, String newStatus) {
        String sql = "UPDATE Senior_IDs SET id_status = ?, issue_date = ? WHERE senior_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, seniorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ID status update failed: " + e.getMessage());
            return false;
        }
    }

    public Optional<SeniorId> getSeniorIdCard(int seniorId) {
        String sql = "SELECT * FROM Senior_IDs WHERE senior_id = ? ORDER BY id_card_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new SeniorId(
                            rs.getInt("id_card_id"),
                            rs.getInt("senior_id"),
                            LocalDate.parse(rs.getString("issue_date")),
                            LocalDate.parse(rs.getString("expiry_date")),
                            rs.getString("id_status"),
                            rs.getString("qr_barcode_code")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading ID card: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean logBenefit(int seniorId, String benefitType, BigDecimal amount,
                              String claimStatus, int processedByUserId) {
        String sql = """
            INSERT INTO Benefit_Transactions
            (senior_id, benefit_type, amount, release_date, claim_status, processed_by_user_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            ps.setString(2, benefitType);
            ps.setBigDecimal(3, amount);
            ps.setString(4, LocalDate.now().toString());
            ps.setString(5, claimStatus);
            ps.setInt(6, processedByUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging benefit: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBenefitClaimStatus(int transactionId, String claimStatus) {
        String sql = "UPDATE Benefit_Transactions SET claim_status = ?, release_date = ? WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, claimStatus);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating claim status: " + e.getMessage());
            return false;
        }
    }

    public List<BenefitTransaction> getBenefitsBySenior(int seniorId) {
        List<BenefitTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Benefit_Transactions WHERE senior_id = ? ORDER BY transaction_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BenefitTransaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("senior_id"),
                            rs.getString("benefit_type"),
                            rs.getBigDecimal("amount"),
                            LocalDate.parse(rs.getString("release_date")),
                            rs.getString("claim_status"),
                            rs.getInt("processed_by_user_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching benefits: " + e.getMessage());
        }
        return list;
    }

    public boolean archiveSenior(int seniorId) {
        String sql = "UPDATE Senior_Citizens SET status = 'Archived' WHERE senior_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seniorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Archive failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDuplicateRecord(int seniorId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Benefit_Transactions WHERE senior_id = ?");
                 PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Senior_IDs WHERE senior_id = ?");
                 PreparedStatement ps3 = conn.prepareStatement("DELETE FROM Senior_Citizens WHERE senior_id = ?")) {
                ps1.setInt(1, seniorId);
                ps1.executeUpdate();
                ps2.setInt(1, seniorId);
                ps2.executeUpdate();
                ps3.setInt(1, seniorId);
                int rows = ps3.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Delete failed: " + e.getMessage());
            return false;
        }
    }

    public void printAndExportSummaryReport(String exportBaseFilename) {
        StringBuilder report = new StringBuilder();
        report.append("=== BARANGAY NAGA SENIOR CITIZEN DEMOGRAPHICS & BENEFIT SUMMARY ===\n");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            report.append("\n-- Total Registered Seniors per Purok --\n");
            try (ResultSet rsPurok = stmt.executeQuery("""
                SELECT purok_address, COUNT(*) AS total
                FROM Senior_Citizens GROUP BY purok_address
                """)) {
                while (rsPurok.next()) {
                    report.append(String.format("Purok %-15s : %d%n",
                            rsPurok.getString("purok_address"), rsPurok.getInt("total")));
                }
            }

            report.append("\n-- Age Group Distribution --\n");
            try (ResultSet rsAge = stmt.executeQuery("""
                SELECT
                    SUM(CASE WHEN age BETWEEN 60 AND 70 THEN 1 ELSE 0 END) AS g60_70,
                    SUM(CASE WHEN age BETWEEN 71 AND 80 THEN 1 ELSE 0 END) AS g71_80,
                    SUM(CASE WHEN age > 80 THEN 1 ELSE 0 END) AS g80_plus
                FROM Senior_Citizens
                """)) {
                if (rsAge.next()) {
                    report.append(String.format("60-70: %d | 71-80: %d | 80+: %d%n",
                            rsAge.getInt("g60_70"), rsAge.getInt("g71_80"), rsAge.getInt("g80_plus")));
                }
            }

            report.append("\n-- Profile Status (Active vs Archived) --\n");
            try (ResultSet rsStatus = stmt.executeQuery("""
                SELECT status, COUNT(*) AS cnt FROM Senior_Citizens GROUP BY status
                """)) {
                while (rsStatus.next()) {
                    report.append(String.format("%-10s : %d%n",
                            rsStatus.getString("status"), rsStatus.getInt("cnt")));
                }
            }

            report.append("\n-- Pension / Benefit Payout Status --\n");
            try (ResultSet rsPay = stmt.executeQuery("""
                SELECT claim_status, COUNT(*) AS tx_count, COALESCE(SUM(amount), 0) AS total_amt
                FROM Benefit_Transactions GROUP BY claim_status
                """)) {
                while (rsPay.next()) {
                    report.append(String.format("%-10s : %d transactions (Total: %.2f)%n",
                            rsPay.getString("claim_status"), rsPay.getInt("tx_count"), rsPay.getDouble("total_amt")));
                }
            }

        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }

        System.out.println(report);

        try (PrintWriter pw = new PrintWriter(new FileWriter(exportBaseFilename + ".csv"))) {
            pw.print(report);
            System.out.println("Exported report to " + exportBaseFilename + ".csv");
        } catch (IOException e) {
            System.err.println("Export error: " + e.getMessage());
        }
    }

    private SeniorCitizen mapSenior(ResultSet rs) throws SQLException {
        return new SeniorCitizen(
                rs.getInt("senior_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                LocalDate.parse(rs.getString("birthdate")),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("purok_address"),
                rs.getString("contact_number"),
                rs.getString("emergency_contact_name"),
                rs.getString("emergency_contact_phone"),
                rs.getString("health_conditions"),
                rs.getString("status")
        );
    }
}