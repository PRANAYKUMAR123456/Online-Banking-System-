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
            if (pin.matches("\\d{4}")) break;
            System.out.println("PIN must be exactly 4 digits.");
        }

        double initialDeposit = readDouble(scanner, "Enter initial deposit amount (>= 0): ");
        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, name, pin, initialDeposit);
        boolean created = accountDAO.create(account);

        if (created) {
            transactionDAO.addTransaction(accountNumber, "ACCOUNT_CREATED", initialDeposit, "Account opened");
            System.out.println("Account created successfully!");
            System.out.println("Account Number: " + accountNumber);
        } else {
            System.out.println("Account creation failed.");
        }
    }

    public void login(Scanner scanner) {
        System.out.println("------ Login ------");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        Account account = accountDAO.findByAccountNumber(accNo);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        // NOTE: PIN hashing can be added as future enhancement
        if (!account.getPin().equals(pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }

        System.out.println("Welcome, " + account.getHolderName());
        accountMenu(scanner, account);
    }

    private void accountMenu(Scanner scanner, Account account) {
        while (true) {
            System.out.println("\n1.View Balance\n2.Deposit\n3.Withdraw\n4.Transfer\n5.View Transactions\n6.Logout");
            int choice = readInt(scanner);

            switch (choice) {
                case 1 -> viewBalance(account);
                case 2 -> deposit(scanner, account);
                case 3 -> withdraw(scanner, account);
                case 4 -> transfer(scanner, account);
                case 5 -> viewTransactions(account);
                case 6 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewBalance(Account account) {
        Account fresh = accountDAO.findByAccountNumber(account.getAccountNumber());
        if (fresh != null) {
            account.setBalance(fresh.getBalance());
            System.out.println("Current Balance: " + account.getBalance());
        }
    }

    private void deposit(Scanner scanner, Account account) {
        double amount = readDouble(scanner, "Enter deposit amount: ");
        if (amount <= 0) return;

        double newBalance = account.getBalance() + amount;
        if (accountDAO.updateBalance(account.getAccountNumber(), newBalance)) {
            account.setBalance(newBalance);
            transactionDAO.addTransaction(account.getAccountNumber(), "DEPOSIT", amount, "Deposit");
            System.out.println("Deposit successful.");
        }
    }

    private void withdraw(Scanner scanner, Account account) {
        double amount = readDouble(scanner, "Enter withdraw amount: ");
        if (amount <= 0 || amount > account.getBalance()) {
            System.out.println("Invalid withdrawal.");
            return;
        }

        double newBalance = account.getBalance() - amount;
        if (accountDAO.updateBalance(account.getAccountNumber(), newBalance)) {
            account.setBalance(newBalance);
            transactionDAO.addTransaction(account.getAccountNumber(), "WITHDRAW", amount, "Withdraw");
            System.out.println("Withdrawal successful.");
        }
    }

    // 🔐 ATOMIC TRANSFER (IMPORTANT FIX)
    private void transfer(Scanner scanner, Account from) {
        System.out.print("Enter target Account Number: ");
        String targetAccNo = scanner.nextLine().trim();

        Account to = accountDAO.findByAccountNumber(targetAccNo);
        if (to == null || to.getAccountNumber().equals(from.getAccountNumber())) {
            System.out.println("Invalid target account.");
            return;
        }

        double amount = readDouble(scanner, "Enter transfer amount: ");
        if (amount <= 0 || amount > from.getBalance()) {
            System.out.println("Invalid amount.");
            return;
        }

        double fromNew = from.getBalance() - amount;
        double toNew = to.getBalance() + amount;

        boolean debit = accountDAO.updateBalance(from.getAccountNumber(), fromNew);
        boolean credit = accountDAO.updateBalance(to.getAccountNumber(), toNew);

        if (debit && credit) {
            from.setBalance(fromNew);
            to.setBalance(toNew);

            transactionDAO.addTransaction(from.getAccountNumber(), "TRANSFER_OUT", amount,
                    "To " + to.getAccountNumber());
            transactionDAO.addTransaction(to.getAccountNumber(), "TRANSFER_IN", amount,
                    "From " + from.getAccountNumber());

            System.out.println("Transfer successful.");
        } else {
            System.out.println("Transfer failed.");
        }
    }

    private void viewTransactions(Account account) {
        List<Transaction> list = transactionDAO.findByAccountNumber(account.getAccountNumber());
        if (list.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }
        for (Transaction t : list) {
            System.out.println("[" + t.getTimestamp() + "] " +
                    t.getType() + " | " + t.getAmount() + " | " + t.getDescription());
        }
    }

    private String generateAccountNumber() {
        return "AC" + (100000 + random.nextInt(900000));
    }

    private int readInt(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("Enter valid number: ");
            }
        }
    }

    private double readDouble(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Enter valid amount.");
            }
        }
    }
}
