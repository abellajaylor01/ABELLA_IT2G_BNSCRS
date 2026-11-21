package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class AdminMenu extends StaffMenu {
    private final UserDao userDao = new UserDao();
    private final ActivityLogViewer logViewer = new ActivityLogViewer();

    public AdminMenu(User currentUser) {
        super(currentUser);
    }

    @Override
    public void show(Scanner scanner) {
        while (true) {
            System.out.println("\n=== BARANGAY ADMINISTRATOR MENU ===");
            System.out.println("1. Register Senior Citizen & Assign ID");
            System.out.println("2. Search Senior Profiles");
            System.out.println("3. View Full Profile, ID & Benefit History");
            System.out.println("4. Update Profile / Health / Emergency Contact");
            System.out.println("5. Update Senior ID Status");
            System.out.println("6. Log Benefit / Pension Distribution");
            System.out.println("7. Update Benefit Claim Status");
            System.out.println("8. Archive Senior Profile (Soft Delete)");
            System.out.println("9. Delete Duplicate / Erroneous Record");
            System.out.println("10. Generate & Export Demographics/Benefit Report");
            System.out.println("11. View System Audit Logs");
            System.out.println("12. Create System User Account");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> registerSenior(scanner);
                case "2" -> searchSeniors(scanner);
                case "3" -> viewFullProfile(scanner);
                case "4" -> updateProfile(scanner);
                case "5" -> updateIdStatus(scanner);
                case "6" -> logBenefit(scanner);
                case "7" -> updateBenefitStatus(scanner);
                case "8" -> archiveSenior(scanner);
                case "9" -> deleteDuplicate(scanner);
                case "10" -> recordDao.printAndExportSummaryReport("barangay_naga_sc_report");
                case "11" -> logViewer.displayLogs();
                case "12" -> createUserAccount(scanner);
                case "0" -> {
                    logDao.recordLog(currentUser.userId(), "LOGOUT", "Admin logged out", "127.0.0.1");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void deleteDuplicate(Scanner sc) {
        System.out.print("Enter duplicate/erroneous Senior ID to permanently remove: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        if (recordDao.deleteDuplicateRecord(id)) {
            logDao.recordLog(currentUser.userId(), "DELETE_DUPLICATE", "Removed Senior ID #" + id, "127.0.0.1");
            System.out.println("Record removed.");
        }
    }

    private void createUserAccount(Scanner sc) {
        System.out.print("Username: "); String u = sc.nextLine().trim();
        System.out.print("Password: "); String p = sc.nextLine().trim();
        System.out.print("Full Name: "); String fn = sc.nextLine().trim();
        System.out.print("Role (Admin/Staff/Viewer): "); String r = sc.nextLine().trim();
        if (userDao.createUser(u, p, fn, r)) {
            logDao.recordLog(currentUser.userId(), "CREATE_USER", "Created user '" + u + "' as " + r, "127.0.0.1");
            System.out.println("User account created.");
        }
    }
}