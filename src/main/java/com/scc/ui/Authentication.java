package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public static class AuthResult {
        private final User user;
        private final boolean exitRequested;

        private AuthResult(User user, boolean exitRequested) {
            this.user = user;
            this.exitRequested = exitRequested;
        }

        public static AuthResult authenticated(User user) {
            return new AuthResult(user, false);
        }

        public static AuthResult failed() {
            return new AuthResult(null, false);
        }

        public static AuthResult exit() {
            return new AuthResult(null, true);
        }

        public Optional<User> getUser() {
            return Optional.ofNullable(user);
        }

        public boolean isExitRequested() {
            return exitRequested;
        }
    }

    public AuthResult promptAuthMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== BARANGAY NAGA SENIOR CITIZEN SYSTEM ===");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return login(scanner);
                case "2":
                    register(scanner);
                    break;
                case "0":
                    System.out.println("Exiting system. Goodbye!");
                    return AuthResult.exit();
                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 0.");
                    break;
            }
        }
    }

    public AuthResult login(Scanner scanner) {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Optional<User> userOpt = userDao.authenticate(username, password);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            logDao.recordLog(u.userId(), "LOGIN", "User logged in (" + u.userRole() + ")", "127.0.0.1");
            System.out.println("Welcome, " + u.fullName() + " [" + u.userRole() + "]!");
            return AuthResult.authenticated(u);
        } else {
            System.out.println("Invalid credentials.");
            return AuthResult.failed();
        }
    }

    public void register(Scanner scanner) {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        System.out.print("Role (Admin / Staff / Viewer) [default: Viewer]: ");
        String roleInput = scanner.nextLine().trim();
        String role = normalizeRole(roleInput);

        boolean created = userDao.createUser(username, password, fullName, role);
        if (created) {
            System.out.println("Registration successful! You can now log in as [" + role + "].");
            userDao.authenticate(username, password).ifPresent(newUser ->
                    logDao.recordLog(newUser.userId(), "REGISTER", "New account registered (" + role + ")", "127.0.0.1")
            );
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private String normalizeRole(String input) {
        if ("admin".equalsIgnoreCase(input)) {
            return "Admin";
        }
        if ("staff".equalsIgnoreCase(input)) {
            return "Staff";
        }
        return "Viewer";
    }
}