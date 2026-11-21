package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.SeniorRecordDao;
import com.scc.model.BenefitTransaction;
import com.scc.model.SeniorCitizen;
import com.scc.model.SeniorId;
import com.scc.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class StaffMenu {
    protected final User currentUser;
    protected final SeniorRecordDao recordDao = new SeniorRecordDao();
    protected final ActivityLogDao logDao = new ActivityLogDao();

    public StaffMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println("\n--- RECORDS STAFF / ENCODER MENU ---");
            System.out.println("1. Register Senior Citizen & Assign ID");
            System.out.println("2. Search Senior Profiles (ID / Last Name / Purok)");
            System.out.println("3. View Full Profile, ID & Benefit History");
            System.out.println("4. Update Profile / Health / Emergency Contact");
            System.out.println("5. Update Senior ID Status");
            System.out.println("6. Log Benefit / Pension Distribution");
            System.out.println("7. Update Benefit Claim Status");
            System.out.println("8. Archive Senior Profile (Soft Delete)");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    registerSenior(scanner);
                    break;
                case "2":
                    searchSeniors(scanner);
                    break;
                case "3":
                    viewFullProfile(scanner);
                    break;
                case "4":
                    updateProfile(scanner);
                    break;
                case "5":
                    updateIdStatus(scanner);
                    break;
                case "6":
                    logBenefit(scanner);
                    break;
                case "7":
                    updateBenefitStatus(scanner);
                    break;
                case "8":
                    archiveSenior(scanner);
                    break;
                case "0":
                    logDao.recordLog(currentUser.userId(), "LOGOUT", "User logged out", "127.0.0.1");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    protected Optional<Integer> promptInt(Scanner sc, String prompt) {
        System.out.print(prompt);
        try {
            return Optional.of(Integer.parseInt(sc.nextLine().trim()));
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid whole number.");
            return Optional.empty();
        }
    }

    protected void registerSenior(Scanner sc) {
        System.out.print("First Name: ");
        String fn = sc.nextLine().trim();
        System.out.print("Last Name: ");
        String ln = sc.nextLine().trim();

        LocalDate dob;
        System.out.print("Birthdate (YYYY-MM-DD): ");
        try {
            dob = LocalDate.parse(sc.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        System.out.print("Gender: ");
        String gender = sc.nextLine().trim();
        System.out.print("Purok/Sub-village: ");
        String purok = sc.nextLine().trim();
        System.out.print("Contact Number: ");
        String contact = sc.nextLine().trim();
        System.out.print("Emergency Contact Name: ");
        String eName = sc.nextLine().trim();
        System.out.print("Emergency Contact Phone: ");
        String ePhone = sc.nextLine().trim();
        System.out.print("Health Conditions/Disability: ");
        String health = sc.nextLine().trim();

        SeniorCitizen s = new SeniorCitizen(0, fn, ln, dob, 0, gender, purok, contact, eName, ePhone, health, "Active");
        int id = recordDao.registerSenior(s);
        if (id > 0) {
            logDao.recordLog(currentUser.userId(), "CREATE_SENIOR", "Registered Senior ID #" + id + " (" + fn + " " + ln + ")", "127.0.0.1");
            System.out.println("Registered successfully! Senior ID: " + id);
        } else {
            System.out.println("Failed to register senior citizen.");
        }
    }

    protected void searchSeniors(Scanner sc) {
        System.out.print("Enter Senior ID, Last Name, or Purok: ");
        List<SeniorCitizen> list = recordDao.searchSeniors(sc.nextLine());
        if (list.isEmpty()) {
            System.out.println("No matching senior citizen records found.");
            return;
        }
        for (SeniorCitizen s : list) {
            System.out.printf("ID: %d | %s, %s | Age: %d | Purok: %s | Status: %s%n",
                    s.getSeniorId(), s.getLastName(), s.getFirstName(), s.getAge(), s.getPurokAddress(), s.getStatus());
        }
    }

    protected void viewFullProfile(Scanner sc) {
        Optional<Integer> idOpt = promptInt(sc, "Enter Senior ID: ");
        if (!idOpt.isPresent()) {
            return;
        }
        int id = idOpt.get();

        Optional<SeniorCitizen> seniorOpt = recordDao.findById(id);
        if (!seniorOpt.isPresent()) {
            System.out.println("Senior not found.");
            return;
        }

        SeniorCitizen s = seniorOpt.get();
        System.out.printf(
                "=== PROFILE #%d ===%n" +
                        "Name: %s %s | Birthdate: %s (Age: %d) | Gender: %s%n" +
                        "Purok: %s | Contact: %s | Status: %s%n" +
                        "Health/Disability: %s%n" +
                        "Emergency Contact: %s (%s)%n",
                s.getSeniorId(), s.getFirstName(), s.getLastName(), s.getBirthdate(),
                s.getAge(), s.getGender(), s.getPurokAddress(), s.getContactNumber(),
                s.getStatus(), s.getHealthConditions(), s.getEmergencyContactName(),
                s.getEmergencyContactPhone()
        );

        Optional<SeniorId> cardOpt = recordDao.getSeniorIdCard(id);
        if (cardOpt.isPresent()) {
            SeniorId card = cardOpt.get();
            System.out.printf("ID Card: %s | Status: %s | Issued: %s | Expires: %s%n",
                    card.qrBarcodeCode(), card.idStatus(), card.issueDate(), card.expiryDate());
        }

        System.out.println("-- Benefit Transactions --");
        List<BenefitTransaction> benefits = recordDao.getBenefitsBySenior(id);
        for (BenefitTransaction b : benefits) {
            System.out.printf("Tx#%d | %s | %.2f | %s | %s%n",
                    b.transactionId(), b.benefitType(), b.amount(), b.claimStatus(), b.releaseDate());
        }
    }

    protected void updateProfile(Scanner sc) {
        Optional<Integer> idOpt = promptInt(sc, "Senior ID to update: ");
        if (!idOpt.isPresent()) {
            return;
        }
        int id = idOpt.get();

        System.out.print("New Contact Number: ");
        String contact = sc.nextLine().trim();
        System.out.print("New Purok Address: ");
        String purok = sc.nextLine().trim();
        System.out.print("Updated Health/Disability Status: ");
        String health = sc.nextLine().trim();
        System.out.print("Emergency Contact Name: ");
        String eName = sc.nextLine().trim();
        System.out.print("Emergency Contact Phone: ");
        String ePhone = sc.nextLine().trim();

        if (recordDao.updateProfile(id, contact, purok, health, eName, ePhone)) {
            logDao.recordLog(currentUser.userId(), "UPDATE_PROFILE", "Updated profile for Senior ID #" + id, "127.0.0.1");
            System.out.println("Profile updated.");
        } else {
            System.out.println("Profile not updated (Senior ID may not exist).");
        }
    }

    protected void updateIdStatus(Scanner sc) {
        Optional<Integer> idOpt = promptInt(sc, "Senior ID: ");
        if (!idOpt.isPresent()) {
            return;
        }
        int id = idOpt.get();

        System.out.print("ID Status (Issued/Pending/Lost/Replacement Requested): ");
        String status = sc.nextLine().trim();
        if (recordDao.updateIdStatus(id, status)) {
            logDao.recordLog(currentUser.userId(), "UPDATE_ID_STATUS", "Senior #" + id + " ID status -> " + status, "127.0.0.1");
            System.out.println("ID status updated.");
        } else {
            System.out.println("ID status not updated.");
        }
    }

    protected void logBenefit(Scanner sc) {
        Optional<Integer> idOpt = promptInt(sc, "Senior ID: ");
        if (!idOpt.isPresent()) {
            return;
        }
        int id = idOpt.get();

        System.out.print("Benefit Type (Financial Assistance/Medicine/Pension): ");
        String type = sc.nextLine().trim();

        BigDecimal amt;
        System.out.print("Amount: ");
        try {
            amt = new BigDecimal(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Claim Status (Claimed/Pending): ");
        String status = sc.nextLine().trim();

        if (recordDao.logBenefit(id, type, amt, status, currentUser.userId())) {
            logDao.recordLog(currentUser.userId(), "LOG_BENEFIT", "Logged " + type + " (" + amt + ") for Senior #" + id, "127.0.0.1");
            System.out.println("Benefit transaction recorded.");
        } else {
            System.out.println("Failed to record benefit transaction.");
        }
    }

    protected void updateBenefitStatus(Scanner sc) {
        Optional<Integer> txIdOpt = promptInt(sc, "Transaction ID: ");
        if (!txIdOpt.isPresent()) {
            return;
        }
        int txId = txIdOpt.get();

        System.out.print("New Claim Status (Claimed/Pending): ");
        String status = sc.nextLine().trim();
        if (recordDao.updateBenefitClaimStatus(txId, status)) {
            logDao.recordLog(currentUser.userId(), "UPDATE_CLAIM", "Tx #" + txId + " status -> " + status, "127.0.0.1");
            System.out.println("Claim status updated.");
        } else {
            System.out.println("Claim status not updated.");
        }
    }

    protected void archiveSenior(Scanner sc) {
        Optional<Integer> idOpt = promptInt(sc, "Senior ID to archive: ");
        if (!idOpt.isPresent()) {
            return;
        }
        int id = idOpt.get();

        if (recordDao.archiveSenior(id)) {
            logDao.recordLog(currentUser.userId(), "ARCHIVE_SENIOR", "Archived Senior ID #" + id, "127.0.0.1");
            System.out.println("Senior profile archived.");
        } else {
            System.out.println("Senior profile not archived.");
        }
    }
}