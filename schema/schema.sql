CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;
CREATE TABLE IF NOT EXISTS inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);
INSERT INTO inventory (product_name, price, stock) VALUES
('shirt', 1200.00, 100),
('shoes', 750.00, 50),
('t-shirt', 350.00, 200),
('kurti', 600.00, 75),
('jeans', 899.00, 60);
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL
);
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES inventory(id) ON DELETE CASCADE
);

CREATE VIEW transaction_summary AS
SELECT 
    o.order_id,
    o.order_date,
    oi.product_id,
    i.product_name,
    oi.quantity,
    oi.price,
    (oi.quantity * oi.price) AS total_price
FROM 
    orders o
JOIN 
    order_items oi ON o.order_id = oi.order_id
JOIN 
    inventory i ON oi.product_id = i.id;
ALTER TABLE orders ADD COLUMN customer_name VARCHAR(100);

