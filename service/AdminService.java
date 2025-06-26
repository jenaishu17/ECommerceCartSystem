package service;

import db.DBConnection;
import java.sql.*;

public class AdminService {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "root";

    public static boolean validateAdmin(String username, String password) {
        return USERNAME.equals(username) && PASSWORD.equals(password);
    }

    public static void viewTransactions() throws SQLException {
        String query = "SELECT o.order_id, o.order_date, oi.product_id, i.product_name, oi.quantity, oi.price, (oi.quantity * oi.price) AS total_price " +
                       "FROM orders o " +
                       "JOIN order_items oi ON o.order_id = oi.order_id " +
                       "JOIN inventory i ON oi.product_id = i.id " +
                       "ORDER BY o.order_id, oi.product_id";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\nTRANSACTIONS:");
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Timestamp orderDate = rs.getTimestamp("order_date");
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double totalPrice = rs.getDouble("total_price");

                System.out.printf("Order ID: %d, Date: %s, Product: %s (ID: %d), Quantity: %d, Price: ₹%.2f, Total: ₹%.2f\n",
                                  orderId, orderDate, productName, productId, quantity, price, totalPrice);
            }
        }
    }

    public static void viewTotalSales() throws SQLException {
        String query = "SELECT SUM(oi.price * oi.quantity) AS total_sales FROM order_items oi";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                double totalSales = rs.getDouble("total_sales");
                System.out.printf("\nTOTAL SALES: ₹%.2f\n", totalSales);
            } else {
                System.out.println("No sales data available.");
            }
        }
    }
}

