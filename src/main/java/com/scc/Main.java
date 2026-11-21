package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.model.User;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;
import com.scc.ui.ResidentViewerMenu;
import com.scc.ui.StaffMenu;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        Authentication auth = new Authentication();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                Authentication.AuthResult result = auth.promptAuthMenu(scanner);
                if (result.isExitRequested()) {
                    break;
                }

                Optional<User> loggedIn = result.getUser();
                if (!loggedIn.isPresent()) {
                    continue;
                }

                User user = loggedIn.get();
                switch (user.userRole()) {
                    case "Admin":
                        new AdminMenu(user).show(scanner);
                        break;
                    case "Staff":
                        new StaffMenu(user).show(scanner);
                        break;
                    case "Viewer":
                        new ResidentViewerMenu(user).show(scanner);
                        break;
                    default:
                        System.out.println("Unrecognized role: " + user.userRole());
                        break;
                }
            }
        }
    }
}