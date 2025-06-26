package ui;

import model.Product;
import service.AdminService;
import service.CartService;
import service.ProductService;
import java.sql.*;
import java.util.Scanner;

import db.DBConnection;

public class Main {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            while (true) {
                showMenu();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void showMenu() throws SQLException {
        System.out.println("\n    							                                  E-COMMERCE MENU\n             						                       -----------------------------------\n    							                                  1. View Products\n    							                                  2. Add to Cart\n    							                                  3. View Cart\n    							                                  4. Remove from Cart\n    							                                  5. Checkout\n    							                                  6. Admin Login\n    							                                  7. Exit\n             						                       -----------------------------------");
        System.out.print("Choose option: ");
        
        int choice = sc.nextInt();
        sc.nextLine();
        switch (choice) {
            case 1 -> viewProducts();
            case 2 -> {viewProducts(); addToCart();}
            case 3 -> viewCart();
            case 4 -> removeFromCart();
            case 5 -> checkout();
            case 6 -> adminLogin();
            case 7 -> {System.out.println("Exiting...."); System.exit(0);}
            default -> System.out.println("Invalid option!");
        }
    }


    static void viewProducts() throws SQLException {
        System.out.println("\nPRODUCTS:");
        Product[] products = ProductService.getAllProducts();
        for (Product p : products) {
            System.out.printf("%d. %-15s ₹%-6.2f (Stock: %d)\n", 
                p.id, p.name, p.price, p.stock);
        }
    }

    static void addToCart() throws SQLException {
        System.out.print("\nEnter Product ID: ");
        int productId = sc.nextInt();
        sc.nextLine();
        Product p = ProductService.getProduct(productId);
        if (p == null) {
            System.out.println("Product not found!");
            return;
        }

        System.out.print("Enter Quantity: ");
        p.quantity = sc.nextInt();
        sc.nextLine();
        CartService.add(p);
        System.out.println("Added to cart!");
    }

    static void viewCart() {
        Product[] cart = CartService.getAll();
        if (cart.length == 0) {
            System.out.println("Cart is empty!");
            return;
        }

        System.out.println("\nYOUR CART:");
        for (Product p : cart) {
            System.out.printf("\n%d. %-15s ₹%-6.2f x%d = ₹%.2f\n", 
                p.id, p.name, p.price, p.quantity, p.price * p.quantity);
        }
        System.out.printf("TOTAL: ₹%.2f\n", CartService.getTotal());
    }

    static void removeFromCart() {
        System.out.print("\nEnter Product ID to remove: ");
        int productId = sc.nextInt();
        sc.nextLine();
        CartService.remove(productId);
        System.out.println("Removed from cart!");
    }

    static void checkout() throws SQLException {
        Product[] cart = CartService.getAll();
        if (cart.length == 0) {
            System.out.println("Cart is empty!");
            return;
        }

        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.print("Enter your address: ");
        String address = sc.nextLine();

        double total = CartService.getTotal();
        System.out.printf("\nORDER SUMMARY:\nCustomer: %s\nShipping: %s\n", name, address);
        viewCart();
        System.out.printf("\nTOTAL (+10%% tax): ₹%.2f\n", total * 1.1);

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOrder = conn.prepareStatement(
                "INSERT INTO orders (total_amount, shipping_address, customer_name) VALUES (?,?,?)", 
                Statement.RETURN_GENERATED_KEYS)) {
                
                psOrder.setDouble(1, total * 1.1);
                psOrder.setString(2, address);
                psOrder.setString(3, name);
                
                if (psOrder.executeUpdate() == 0) {
                    conn.rollback();
                    throw new SQLException("Order creation failed");
                }

                int orderId;
                try (ResultSet generatedKeys = psOrder.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        conn.rollback();
                        throw new SQLException("No order ID generated");
                    }
                    orderId = generatedKeys.getInt(1);
                }

                try (PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?,?,?,?)")) {
                    
                    for (Product p : cart) {
                        if (!ProductService.updateStock(p.id, p.quantity)) {
                            conn.rollback();
                            throw new SQLException("Insufficient stock for: " + p.name);
                        }
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, p.id);
                        psItem.setInt(3, p.quantity);
                        psItem.setDouble(4, p.price);
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }
                conn.commit();
                CartService.clear();
                System.out.println("Order placed successfully!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    static void adminLogin() throws SQLException {
        System.out.print("Enter Admin Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();

        if (AdminService.validateAdmin(username, password)) {
            System.out.println("Admin logged in successfully!");
            adminMenu();
        } else {
            System.out.println("Invalid admin credentials!");
        }
    }

    static void adminMenu() throws SQLException {
        while (true) {
            System.out.println("\nADMIN MENU");
            System.out.println("1. View Transactions");
            System.out.println("2. View Total Sales");
            System.out.println("3. Logout");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> AdminService.viewTransactions();
                case 2 -> AdminService.viewTotalSales();
                case 3 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }
}
