package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void addTransaction(String accountNumber, String type, double amount, String description) {
        String sql = "INSERT INTO transactions (account_number, type, amount, description) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, description);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding transaction: " + e.getMessage());
        }
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, account_number, type, amount, timestamp, description " +
                     "FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                String accNo = rs.getString("account_number");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                Timestamp ts = rs.getTimestamp("timestamp");
                LocalDateTime time = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                String desc = rs.getString("description");

                Transaction t = new Transaction(id, accNo, type, amount, time, desc);
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }
}
