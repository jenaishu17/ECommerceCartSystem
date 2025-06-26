package service;

import model.Product;

public class CartService {
    private static Product[] cart = new Product[20];
    private static int cartSize = 0;

    public static void add(Product product) {
        if (cartSize < cart.length) {
            cart[cartSize++] = product;
        } else {
            System.out.println("Cart is full!");
        }
    }

    public static void remove(int productId) {
        for (int i = 0; i < cartSize; i++) {
            if (cart[i].id == productId) {
                for (int j = i; j < cartSize - 1; j++) {
                    cart[j] = cart[j + 1];
                }
                cart[--cartSize] = null;
                break;
            }
        }
    }

    public static Product[] getAll() {
        Product[] currentCart = new Product[cartSize];
        System.arraycopy(cart, 0, currentCart, 0, cartSize);
        return currentCart;
    }

    public static void clear() {
        cart = new Product[20];
        cartSize = 0;
    }

    public static double getTotal() {
        double total = 0;
        for (int i = 0; i < cartSize; i++) {
            total += cart[i].price * cart[i].quantity;
        }
        return total;
    }
}
