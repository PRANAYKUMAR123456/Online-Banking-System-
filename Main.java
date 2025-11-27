package com.bank;

import com.bank.service.BankService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BankService bankService = new BankService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("==================================");
            System.out.println("        ONLINE BANKING SYSTEM     ");
            System.out.println("==================================");
            System.out.println("1. Create New Account");
            System.out.println("2. Login to Existing Account");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    bankService.createAccount(scanner);
                    break;
                case 2:
                    bankService.login(scanner);
                    break;
                case 3:
                    System.out.println("Thank you for using Online Banking System. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice! Please enter 1, 2 or 3.");
            }
        }
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
