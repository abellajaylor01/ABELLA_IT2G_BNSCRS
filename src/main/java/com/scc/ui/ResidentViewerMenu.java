package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.SeniorRecordDao;
import com.scc.model.BenefitTransaction;
import com.scc.model.SeniorCitizen;
import com.scc.model.SeniorId;
import com.scc.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ResidentViewerMenu {
    private final User currentUser;
    private final SeniorRecordDao recordDao = new SeniorRecordDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public ResidentViewerMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println("\n--- SENIOR CITIZEN / RESIDENT VIEWER (READ-ONLY) ---");
            System.out.println("1. View Profile Status, ID Details & Benefit History");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    viewResidentDetails(scanner);
                    break;
                case "0":
                    logDao.recordLog(currentUser.userId(), "LOGOUT", "Viewer logged out", "127.0.0.1");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private void viewResidentDetails(Scanner scanner) {
        System.out.print("Enter your Senior Citizen ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric Senior Citizen ID.");
            return;
        }

        Optional<SeniorCitizen> seniorOpt = recordDao.findById(id);
        if (!seniorOpt.isPresent()) {
            System.out.println("Record not found.");
            return;
        }

        SeniorCitizen s = seniorOpt.get();
        System.out.printf("Name: %s %s | Purok: %s | Profile Status: %s%n",
                s.getFirstName(), s.getLastName(), s.getPurokAddress(), s.getStatus());

        Optional<SeniorId> cardOpt = recordDao.getSeniorIdCard(id);
        if (cardOpt.isPresent()) {
            SeniorId c = cardOpt.get();
            System.out.printf("ID Code: %s | Status: %s | Expiry: %s%n",
                    c.qrBarcodeCode(), c.idStatus(), c.expiryDate());
        }

        List<BenefitTransaction> benefits = recordDao.getBenefitsBySenior(id);
        for (BenefitTransaction b : benefits) {
            System.out.printf("Benefit: %s | Amount: %.2f | Status: %s | Date: %s%n",
                    b.benefitType(), b.amount(), b.claimStatus(), b.releaseDate());
        }
    }
}