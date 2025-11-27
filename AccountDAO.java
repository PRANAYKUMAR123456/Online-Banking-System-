package com.bank.dao;

import com.bank.model.Account;
import com.bank.util.DBConnection;

import java.sql.*;

public class AccountDAO {

    public Account findByAccountNumber(String accountNumber) {
        String sql = "SELECT account_number, holder_name, pin, balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setHolderName(rs.getString("holder_name"));
                acc.setPin(rs.getString("pin"));
                acc.setBalance(rs.getDouble("balance"));
                return acc;
            }
        } catch (SQLException e) {
            System.out.println("Error finding account: " + e.getMessage());
        }
        return null;
    }

    public boolean create(Account account) {
        String sql = "INSERT INTO accounts (account_number, holder_name, pin, balance) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getHolderName());
            ps.setString(3, account.getPin());
            ps.setDouble(4, account.getBalance());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBalance(String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
            return false;
        }
    }
}
