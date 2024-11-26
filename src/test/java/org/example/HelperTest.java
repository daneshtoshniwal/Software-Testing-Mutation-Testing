package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private Helper helper;
    private PrintStream mockOut;
    private ByteArrayOutputStream outputStream;
    private LinkedHashMap<String, Customer> customers;
    private LinkedHashMap<String, Product> inventory;
    private LinkedHashMap<String, Order> orders;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        mockOut = new PrintStream(outputStream);
        helper = new Helper(mockOut);

        customers = new LinkedHashMap<>();
        inventory = new LinkedHashMap<>();
        orders = new LinkedHashMap<>();
    }

    @Test
    void print() {
        // Test the print method
        helper.print("Hello, World!");
        assertEquals("Hello, World!\n", outputStream.toString());
    }

    @Test
    void readCustomers() {
        String input = "2\n" +
                "C001 John john@example.com \"123 Street\" Regular\n" +
                "C002 Alice alice@example.com \"456 Avenue\" Premium";

        Scanner scanner = new Scanner(input);
        helper.readCustomers(scanner, customers);

        assertEquals(2, customers.size());
        assertTrue(customers.containsKey("C001"));
        assertTrue(customers.containsKey("C002"));
        assertEquals("John", customers.get("C001").getName());
        assertEquals("Alice", customers.get("C002").getName());
    }
    @Test
    void readProducts() {
        String input = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(2, inventory.size());
        assertTrue(inventory.containsKey("P001"));
        assertTrue(inventory.containsKey("P002"));

        Product product1 = inventory.get("P001");
        assertTrue(product1 instanceof Electronics);
        assertEquals("Phone", product1.getName());

        Product product2 = inventory.get("P002");
        assertTrue(product2 instanceof Fashion);
        assertEquals("T-Shirt", product2.getName());
    }

    @Test
    void readCartAdditions() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        String cartInput = "1\n" +
                "C001 P001 5";  // Adding 5 Phones to John's cart

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        Customer customer = customers.get("C001");
        Order order = customer.getOrder();
        assertNotNull(order);
        assertEquals(1, order.cart.size());
        assertEquals(5, order.cart.get(0).getCartQuantity());
    }

    @Test
    void printCustomerCartDetails() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        String cartInput = "1\n" +
                "C001 P001 2";  // Adding 2 Phones to John's cart

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        helper.printCustomerCartDetails(customers);

        String expectedOutput = "Customer: John (Regular)\n" +
                "Address: 123 Street\n" +
                "Shopping Cart:\n" +
                "P001 Phone 500.00 Quantity: 2\n" +
                "Total: 1000.00\n";

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void readOrders() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        String cartInput = "1\n" +
                "C001 P001 2";  // Adding 2 Phones to John's cart

        String orderInput = "1\n" +
                "O001 C001 Online CreditCard";  // Order for John

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        scanner = new Scanner(orderInput);
        helper.readOrders(scanner, customers, inventory, orders);

        assertEquals(1, orders.size());
        assertTrue(orders.containsKey("O001"));

        Order order = orders.get("O001");
        assertNotNull(order);
        assertEquals("O001", order.getOrderId());
        assertEquals("C001", order.getCustomerId());
        assertEquals(2, order.cart.get(0).getCartQuantity());
    }
}
