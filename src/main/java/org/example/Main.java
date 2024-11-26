package org.example;

import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<String, Customer> customers = new LinkedHashMap<>();
        LinkedHashMap<String, Product> inventory = new LinkedHashMap<>();
        LinkedHashMap<String, Order> orders = new LinkedHashMap<>();

        Scanner scanner = new Scanner(System.in);
        Helper helper = new Helper(System.out);

        helper.readCustomers(scanner, customers);
        helper.readProducts(scanner, inventory);
        helper.readCartAdditions(scanner, customers, inventory);
        helper.printCustomerCartDetails(customers);
        helper.readOrders(scanner, customers, inventory, orders);
    }
}

class Helper {
    private PrintStream out;

    public Helper(PrintStream out) {
        this.out = out;
    }

    public void print(String message) {
        out.println(message);
    }

    public void readCustomers(Scanner scanner, LinkedHashMap<String, Customer> customers) {
        int numberOfCustomers = Integer.parseInt(scanner.nextLine().trim());
        if (numberOfCustomers > 100) {
            return;
        }
        for (int i = 0; i < numberOfCustomers; i++) {
            String line = scanner.nextLine();
            String[] tokens = parseLine(line);
//            System.out.println(tokens.length);
            if (tokens.length != 5) {
                continue;
            }
            String customerId = tokens[0];
            String name = tokens[1];
            String email = tokens[2];
            String address = tokens[3];
            String customerType = tokens[4];

            if (customerId == null || customerId.isEmpty() || customers.containsKey(customerId)) {
                print("INVALID_CUSTOMER_ID");
                continue;
            }
            if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                continue;
            }
            if (!customerType.equals("Regular") && !customerType.equals("Premium")) {
                continue;
            }
            Customer customer;
            if (customerType.equals("Regular")) {
                customer = new RegularCustomer(customerId, name, email, address);
            } else {
                customer = new PremiumCustomer(customerId, name, email, address);
            }
            customers.put(customerId, customer);
        }
//        System.out.println(customers.size());
    }

    public void readProducts(Scanner scanner, LinkedHashMap<String, Product> inventory) {
        int numberOfProducts = Integer.parseInt(scanner.nextLine().trim());
        if (numberOfProducts > 200) {
            return;
        }
        for (int i = 0; i < numberOfProducts; i++) {
            String line = scanner.nextLine();
            String[] tokens = parseLine(line);
            if (tokens.length < 6) {
                continue;
            }
            String productId = tokens[0];
            String productType = tokens[1];
            String productName = tokens[2];
            double price = Double.parseDouble(tokens[3]);
            int quantity = Integer.parseInt(tokens[4]);

            if (productId == null || productId.isEmpty() || inventory.containsKey(productId)) {
                print("INVALID_PRODUCT_ID");
                continue;
            }
            // Adjusted price constraint to allow prices up to 200,000
            if (price <= 0 || price > 200000) {
                continue;
            }
            if (quantity <= 0 || quantity > 500) {
                continue;
            }

            Product product;
            if (productType.equals("Electronics")) {
                int warranty = Integer.parseInt(tokens[5]);
                if (warranty < 0 || warranty > 36) {
                    print("INVALID_WARRANTY_PERIOD");
                    continue;
                }
                product = new Electronics(productId, productName, price, quantity, warranty);
            } else if (productType.equals("Fashion")) {
                String size = tokens[5];
                if (!size.equals("S") && !size.equals("M") && !size.equals("L") && !size.equals("XL")
                        && !size.equals("XXL")) {
                    print("INVALID_SIZE");
                    continue;
                }
                product = new Fashion(productId, productName, price, quantity, size);
            } else {
                continue;
            }
            inventory.put(productId, product);
        }
    }

    public void readCartAdditions(Scanner scanner, LinkedHashMap<String, Customer> customers,
            LinkedHashMap<String, Product> inventory) {
        int numberOfCartAdditions = Integer.parseInt(scanner.nextLine().trim());
        for (int i = 0; i < numberOfCartAdditions; i++) {
            String line = scanner.nextLine();
            String[] tokens = parseLine(line);
            if (tokens.length != 3) {
                continue;
            }
            String customerId = tokens[0];
            String productId = tokens[1];
            int quantity = Integer.parseInt(tokens[2]);

            if (!customers.containsKey(customerId)) {
                print("INVALID_CUSTOMER_ID");
                continue;
            }
            if (!inventory.containsKey(productId)) {
                print("INVALID_PRODUCT_ID");
                continue;
            }
            if (quantity <= 0) {
                print("INVALID_QUANTITY");
                continue;
            }
            Product product = inventory.get(productId);
            if (quantity > product.getQuantity()) {
                print("OUT_OF_STOCK");
                continue;
            }

            Customer customer = customers.get(customerId);
            Order order = customer.getOrder();
            if (order == null) {
                order = new OnlineOrder("TEMP_ORDER_" + customerId, customerId, customer);
                customer.setOrder(order);
            }
            try {
                order.addToCart(product, quantity);
            } catch (IllegalArgumentException e) {
                print(e.getMessage());
            }
        }
    }

    public void printCustomerCartDetails(LinkedHashMap<String, Customer> customers) {
        for (String customerId : customers.keySet()) {
            Customer customer = customers.get(customerId);
            Order order = customer.getOrder();
            if (order == null || order.cart.isEmpty()) {
                continue;
            }
            print("Customer: " + customer.getName() + " (" + customer.getCustomerType() + ")");
            print("Address: " + customer.getAddress());
            print("Shopping Cart:");
            for (Product product : order.cart) {
                print(product.getItemId() + " " + product.getName() + " " + String.format("%.2f", product.getPrice())
                        + " Quantity: " + product.getCartQuantity());
            }
            double total = order.calculateTotal();
            if (customer instanceof PremiumCustomer) {
                print("Total (after 10% discount): " + String.format("%.2f", total));
            } else {
                print("Total: " + String.format("%.2f", total));
            }
        }
    }

    public void readOrders(Scanner scanner, LinkedHashMap<String, Customer> customers,
            LinkedHashMap<String, Product> inventory,
            LinkedHashMap<String, Order> orders) {
        if (!scanner.hasNextLine()) {
            return;
        }
        int numberOfOrders = Integer.parseInt(scanner.nextLine().trim());
        if (numberOfOrders > 50) {
            return;
        }
        for (int i = 0; i < numberOfOrders; i++) {
            String line = scanner.nextLine();
            String[] tokens = parseLine(line);
            if (tokens.length != 4) {
                continue;
            }
            String orderId = tokens[0];
            String customerId = tokens[1];
            String orderType = tokens[2];
            String paymentType = tokens[3];

            if (!customers.containsKey(customerId)) {
                print("INVALID_CUSTOMER_ID");
                continue;
            }
            if (!orderType.equals("Online") && !orderType.equals("InStore")) {
                print("INVALID_ORDER_TYPE");
                continue;
            }
            if (!paymentType.equals("CreditCard") && !paymentType.equals("PayPal")) {
                print("INVALID_PAYMENT_METHOD");
                continue;
            }

            Customer customer = customers.get(customerId);
            Order tempOrder = customer.getOrder();
            if (tempOrder == null || tempOrder.cart.isEmpty()) {
                print("Order cannot be processed: Cart is empty");
                continue;
            }

            Order order;
            if (orderType.equals("Online")) {
                order = new OnlineOrder(orderId, customerId, customer);
            } else {
                order = new InStoreOrder(orderId, customerId, customer);
            }
            order.cart = tempOrder.cart; // Transfer cart items to new order
            customer.setOrder(order); // Update customer's order

            Payment payment;
            if (paymentType.equals("CreditCard")) {
                payment = new CreditCardPayment();
            } else {
                payment = new PayPalPayment();
            }
            double totalAmount = order.calculateTotal();
            boolean paymentStatus = order.processOrder(payment, totalAmount);

            print("Processing Order " + orderId + " for " + customer.getName());
            print("Payment Method: " + (paymentType.equals("CreditCard") ? "Credit Card" : "PayPal"));
            print("Payment Status: " + (paymentStatus ? "Successful" : "Failed"));

            orders.put(orderId, order);
        }
    }

    private String[] parseLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(line);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else {
                tokens.add(matcher.group(2));
            }
        }
        return tokens.toArray(new String[0]);
    }
}

abstract class Order {
    protected String orderId;
    protected String customerId;
    protected ArrayList<Product> cart;
    protected Customer customer;

    public Order(String orderId, String customerId, Customer customer) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customer = customer;
        this.cart = new ArrayList<>();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void addToCart(Product product, int quantity) {
        if (quantity <= 0 || quantity > product.getQuantity()) {
            throw new IllegalArgumentException("INVALID_QUANTITY");
        }
        int totalItemsInCart = cart.stream().mapToInt(p -> p.getCartQuantity()).sum();
        if (totalItemsInCart + quantity > 20) {
            throw new IllegalArgumentException("CART_LIMIT_EXCEEDED");
        }
        product.setQuantity(product.getQuantity() - quantity);

        Product cartProduct = null;
        for (Product p : cart) {
            if (p.getItemId().equals(product.getItemId())) {
                cartProduct = p;
                break;
            }
        }
        if (cartProduct != null) {
            cartProduct.setCartQuantity(cartProduct.getCartQuantity() + quantity);
        } else {
            Product newProduct;
            if (product instanceof Electronics) {
                Electronics e = (Electronics) product;
                newProduct = new Electronics(e.getItemId(), e.getName(), e.getPrice(), 0, e.getWarranty());
            } else if (product instanceof Fashion) {
                Fashion f = (Fashion) product;
                newProduct = new Fashion(f.getItemId(), f.getName(), f.getPrice(), 0, f.getSize());
            } else {
                newProduct = new Product(product.getItemId(), product.getName(), product.getPrice(), 0);
            }
            newProduct.setCartQuantity(quantity);
            cart.add(newProduct);
        }
    }

    public double calculateTotal() {
        double total = 0.0;
        for (Product product : cart) {
            double price = product.getPrice() * product.getCartQuantity();
            if (customer instanceof PremiumCustomer) {
                price *= 0.90; // Apply 10% discount on each item
            }
            total += price;
        }
        return total;
    }

    public abstract boolean processOrder(Payment paymentMethod, double amount);
}

class OnlineOrder extends Order {
    public OnlineOrder(String orderId, String customerId, Customer customer) {
        super(orderId, customerId, customer);
    }

    @Override
    public boolean processOrder(Payment paymentMethod, double amount) {
        return paymentMethod.processPayment(amount);
    }
}

class InStoreOrder extends Order {
    public InStoreOrder(String orderId, String customerId, Customer customer) {
        super(orderId, customerId, customer);
    }

    @Override
    public boolean processOrder(Payment paymentMethod, double amount) {
        return paymentMethod.processPayment(amount);
    }
}

interface Payment {
    boolean processPayment(double amount);
}

class CreditCardPayment implements Payment {
    @Override
    public boolean processPayment(double amount) {
        return amount > 0;
    }
}

class PayPalPayment implements Payment {
    @Override
    public boolean processPayment(double amount) {
        return amount > 0;
    }
}

class Customer {
    protected String customerId;
    protected String name;
    protected String email;
    protected String address;
    private Order order;

    public Customer(String customerId, String name, String email, String address) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.order = null;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }


    public String getAddress() {
        return address;
    }

    public String getCustomerType() {
        return "Regular";
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}

class RegularCustomer extends Customer {
    public RegularCustomer(String customerId, String name, String email, String address) {
        super(customerId, name, email, address);
    }

    @Override
    public String getCustomerType() {
        return "Regular";
    }
}

class PremiumCustomer extends Customer {
    public PremiumCustomer(String customerId, String name, String email, String address) {
        super(customerId, name, email, address);
    }

    @Override
    public String getCustomerType() {
        return "Premium";
    }
}

class Product {
    protected String itemId;
    protected String name;
    protected double price;
    protected int quantity;
    protected int cartQuantity;

    public Product(String itemId, String name, double price, int quantity) {
        if (price <= 0 || quantity < 0) { // Allow quantity to be zero
            throw new IllegalArgumentException("Invalid value.");
        }
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.cartQuantity = 0;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public int getCartQuantity() {
        return cartQuantity;
    }

    public void setCartQuantity(int cartQuantity) {
        this.cartQuantity = cartQuantity;
    }

    @Override
    public String toString() {
        return itemId + " " + name + " " + price + " Quantity: " + cartQuantity;
    }
}

class Electronics extends Product {
    private int warranty;

    public Electronics(String itemId, String name, double price, int quantity, int warranty) {
        super(itemId, name, price, quantity);
        this.warranty = warranty;
    }

    public int getWarranty() {
        return warranty;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + warranty + " months warranty)";
    }
}

class Fashion extends Product {
    private String size;

    public Fashion(String itemId, String name, double price, int quantity, String size) {
        super(itemId, name, price, quantity);
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String toString() {
        return super.toString() + " (Size: " + size + ")";
    }
}
