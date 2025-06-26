package service;

import db.DBConnection;
import model.Product;
import java.sql.*;

public class ProductService {
    public static Product[] getAllProducts() throws SQLException {
        Product[] products = new Product[100];
        int count = 0;
        
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory")) {
            
            while (rs.next()) {
                products[count++] = new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
            }
        }
        
        Product[] result = new Product[count];
        System.arraycopy(products, 0, result, 0, count);
        return result;
    }

    public static Product getProduct(int id) throws SQLException {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM inventory WHERE id = ?")) {
            
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
            }
        }
        return null;
    }

    public static boolean updateStock(int id, int qty) throws SQLException {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE inventory SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
            
            ps.setInt(1, qty);
            ps.setInt(2, id);
            ps.setInt(3, qty);
            return ps.executeUpdate() > 0;
        }
    }
}
