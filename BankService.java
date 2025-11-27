package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.Transaction;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BankService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final Random random = new Random();

    public void createAccount(Scanner scanner) {
        System.out.println("------ Create New Account ------");
        System.out.print("Enter account holder name: ");
        String name = scanner.nextLine().trim();

        String pin;
        while (true) {
            System.out.print("Set 4-digit PIN: ");
            pin = scanner.nextLine().trim();
            if (pin.matches("\\d{4}")) {
                break;
            } else {
                System.out.println("PIN must be exactly 4 digits. Try again.");
            }
        }

        double initialDeposit = readDouble(scanner, "Enter initial deposit amount (>= 0): ");

        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, name, pin, initialDeposit);

        boolean created = accountDAO.create(account);
        if (created) {
            transactionDAO.addTransaction(accountNumber, "ACCOUNT_CREATED", initialDeposit, "Account opened");
            System.out.println("Account created successfully!");
            System.out.println("Your Account Number: " + accountNumber);
        } else {
            System.out.println("Failed to create account. Try again.");
        }
    }

    public void login(Scanner scanner) {
        System.out.println("------ Login ------");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        System.out.print("Enter 4-digit PIN: ");
        String pin = scanner.nextLine().trim();

        Account account = accountDAO.findByAccountNumber(accNo);
        if (account == null) {
            System.out.println("No account found with this account number.");
            return;
        }

        if (!account.getPin().equals(pin)) {
            System.out.println("Incorrect PIN!");
            return;
        }

        System.out.println("Login successful. Welcome, " + account.getHolderName() + "!");
        accountMenu(scanner, account);
    }

    private void accountMenu(Scanner scanner, Account account) {
        while (true) {
            System.out.println("\n------ Account Menu (" + account.getAccountNumber() + ") ------");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. View Transaction History");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    viewBalance(account);
                    break;
                case 2:
                    deposit(scanner, account);
                    break;
                case 3:
                    withdraw(scanner, account);
                    break;
                case 4:
                    transfer(scanner, account);
                    break;
                case 5:
                    viewTransactions(account);
                    break;
                case 6:
                    System.out.println("Logged out successfully.\n");
                    return;
                default:
                    System.out.println("Invalid choice! Please choose between 1-6.");
            }
        }
    }

    private void viewBalance(Account account) {
        Account fresh = accountDAO.findByAccountNumber(account.getAccountNumber());
        if (fresh != null) {
            account.setBalance(fresh.getBalance());
        }
        System.out.println("Current Balance: " + account.getBalance());
    }

    private void deposit(Scanner scanner, Account account) {
        double amount = readDouble(scanner, "Enter amount to deposit: ");
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }
        double newBalance = account.getBalance() + amount;
        boolean updated = accountDAO.updateBalance(account.getAccountNumber(), newBalance);
        if (updated) {
            account.setBalance(newBalance);
            transactionDAO.addTransaction(account.getAccountNumber(), "DEPOSIT", amount, "Amount deposited");
            System.out.println("Deposit successful. New Balance: " + account.getBalance());
        } else {
            System.out.println("Deposit failed. Try again.");
        }
    }

    private void withdraw(Scanner scanner, Account account) {
        double amount = readDouble(scanner, "Enter amount to withdraw: ");
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }
        if (amount > account.getBalance()) {
            System.out.println("Insufficient balance.");
            return;
        }
        double newBalance = account.getBalance() - amount;
        boolean updated = accountDAO.updateBalance(account.getAccountNumber(), newBalance);
        if (updated) {
            account.setBalance(newBalance);
            transactionDAO.addTransaction(account.getAccountNumber(), "WITHDRAW", amount, "Amount withdrawn");
            System.out.println("Withdrawal successful. New Balance: " + account.getBalance());
        } else {
            System.out.println("Withdrawal failed. Try again.");
        }
    }

    private void transfer(Scanner scanner, Account fromAccount) {
        System.out.print("Enter target Account Number: ");
        String targetAccNo = scanner.nextLine().trim();

        if (targetAccNo.equals(fromAccount.getAccountNumber())) {
            System.out.println("You cannot transfer to the same account.");
            return;
        }

        Account targetAccount = accountDAO.findByAccountNumber(targetAccNo);
        if (targetAccount == null) {
            System.out.println("Target account not found!");
            return;
        }

        double amount = readDouble(scanner, "Enter amount to transfer: ");
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }
        if (amount > fromAccount.getBalance()) {
            System.out.println("Insufficient balance.");
            return;
        }

        double newFromBalance = fromAccount.getBalance() - amount;
        double newToBalance = targetAccount.getBalance() + amount;

        boolean fromUpdated = accountDAO.updateBalance(fromAccount.getAccountNumber(), newFromBalance);
        boolean toUpdated = accountDAO.updateBalance(targetAccount.getAccountNumber(), newToBalance);

        if (fromUpdated && toUpdated) {
            fromAccount.setBalance(newFromBalance);
            targetAccount.setBalance(newToBalance);

            transactionDAO.addTransaction(fromAccount.getAccountNumber(), "TRANSFER_OUT", amount,
                    "Transferred to " + targetAccount.getAccountNumber());
            transactionDAO.addTransaction(targetAccount.getAccountNumber(), "TRANSFER_IN", amount,
                    "Received from " + fromAccount.getAccountNumber());

            System.out.println("Transfer successful. Your New Balance: " + fromAccount.getBalance());
        } else {
            System.out.println("Transfer failed. Try again.");
        }
    }

    private void viewTransactions(Account account) {
        System.out.println("------ Transaction History ------");
        List<Transaction> list = transactionDAO.findByAccountNumber(account.getAccountNumber());
        if (list.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : list) {
                System.out.println(
                        "[" + t.getTimestamp() + "] " +
                        t.getType() + " | Amount: " + t.getAmount() +
                        " | " + t.getDescription()
                );
            }
        }
    }

    private String generateAccountNumber() {
        int num = 100000 + random.nextInt(900000);
        return "AC" + num;
    }

    private int readInt(Scanner scanner) {
        while (true) {
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private double readDouble(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid amount.\n");
            }
        }
    }
}
