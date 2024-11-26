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
    void readCustomers_validData() {
        String input = "2\n" +
                "C001 John john@example.com \"123 Street\" Regular\n" +
                "C002 Alice alice@example.com \"456 Avenue\" Premium";

        Scanner scanner = new Scanner(input);
        helper.readCustomers(scanner, customers);

        assertEquals(2, customers.size());
        assertTrue(customers.containsKey("C001"));
        assertTrue(customers.containsKey("C002"));
        assertEquals("John", customers.get("C001").getName());
        assertEquals("C001", customers.get("C001").getCustomerId());
        assertEquals("john@example.com", customers.get("C001").getEmail());
    }

    @Test
    void readCustomers_invalidData() {
        String input = "2\n" +
                "C001 John john@example.com \"123 Street\" Regular\n" +  // valid
                "C002 Alice alice@example.com \"456 Avenue\" InvalidType"; // Invalid customer type

        Scanner scanner = new Scanner(input);
        helper.readCustomers(scanner, customers);

        assertEquals(1, customers.size());
        assertTrue(customers.containsKey("C001"));
    }

    @Test
    void readCustomers_invalidCustomerID() {
        String input = "2\n" +
                "C001 John john@example.com \"123 Street\" Regular\n" +
                "C001 Priya priya@example.com \"456 Street\" Regular";

        Scanner scanner = new Scanner(input);
        helper.readCustomers(scanner, customers);

        assertEquals(1, customers.size());
        assertTrue(outputStream.toString().contains("INVALID_CUSTOMER_ID"));
    }

    @Test
    void readProducts_validData() {
        String input = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(2, inventory.size());
        assertTrue(inventory.containsKey("P001"));
        assertTrue(inventory.containsKey("P002"));
    }

    @Test
    void readProducts_invalidProductID() {
        String input = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P001 Electronics Laptop 50000.00 10 12";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertTrue(outputStream.toString().contains("INVALID_PRODUCT_ID"));
    }

    @Test
    void readProducts_invalidPriceMinAmount() {
        String input = "1\n" +
                "P001 Electronics Phone -1.00 10 12"; // Invalid price

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(0, inventory.size());
    }
    @Test
    void readProducts_invalidPriceMaxAmount() {
        String input = "1\n" +
                "P001 Electronics Phone 200001 10 12"; // Invalid price

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(0, inventory.size());
    }

    @Test
    void readProducts_invalidQuantityMin() {
        String input = "1\n" +
                "P001 Electronics Phone 500.00 -1 12"; // Invalid quantity

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(0, inventory.size());
    }

    @Test
    void readProducts_invalidQuantityMax() {
        String input = "1\n" +
                "P001 Electronics Phone 500.00 501 12"; // Invalid quantity

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertEquals(0, inventory.size());
    }

    @Test
    void readProducts_invalidWarrantyPeriodMin() {
        String input = "2\n" +
                "P001 Electronics Phone 500.00 10 -1\n" +
                "P001 Electronics Phone 500.00 10 0";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertTrue(outputStream.toString().contains("INVALID_WARRANTY_PERIOD"));
    }
    @Test
    void readProducts_invalidWarrantyPeriodMax() {
        String input = "1\n" +
                "P001 Electronics Phone 500.00 10 37";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertTrue(outputStream.toString().contains("INVALID_WARRANTY_PERIOD"));
    }

    @Test
    void readProducts_invalidSize() {
        String input = "1\n" +
                "F001 Fashion \"Shirt\" 500.00 15 XS";

        Scanner scanner = new Scanner(input);
        helper.readProducts(scanner, inventory);

        assertTrue(outputStream.toString().contains("INVALID_SIZE"));
    }

    @Test
    void readCartAdditions_validData() {
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
    void readCartAdditions_invalidCustomerID() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "1\n" +
                "P001 Electronics Phone 500.00 10 12";

        String cartInput = "1\n" +
                "C002 P001 10";

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        assertTrue(outputStream.toString().contains("INVALID_CUSTOMER_ID"));
    }

    @Test
    void readCartAdditions_cartLimitExceeded() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "1\n" +
                "P001 Electronics Phone 500.00 10 12";

        String cartInput = "1\n" +
                "C001 P001 25";  // Adding 25 Phones, exceeds cart limit

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        assertTrue(outputStream.toString().contains("OUT_OF_STOCK"));
    }

    @Test
    void readCartAdditions_invalidQuantity() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "1\n" +
                "P001 Electronics Phone 500.00 10 12";

        String cartInput = "1\n" +
                "C001 P001 -1";

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        assertTrue(outputStream.toString().contains("INVALID_QUANTITY"));
    }

    @Test
    void readCartAdditions_invalidProductID() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "1\n" +
                "P001 Electronics Phone 500.00 10 12";

        String cartInput = "1\n" +
                "C001 P002 10";

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        assertTrue(outputStream.toString().contains("INVALID_PRODUCT_ID"));
    }

    @Test
    void printCustomerCartDetails_validData() {
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
    void readOrders_invalidPaymentMethod() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String productInput = "2\n" +
                "P001 Electronics Phone 500.00 10 12\n" +
                "P002 Fashion T-Shirt 20.00 50 M";

        String cartInput = "1\n" +
                "C001 P001 2";  // Adding 2 Phones to John's cart

        String orderInput = "1\n" +
                "O001 C001 Online Bitcoin";  // Invalid payment type

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(productInput);
        helper.readProducts(scanner, inventory);

        scanner = new Scanner(cartInput);
        helper.readCartAdditions(scanner, customers, inventory);

        scanner = new Scanner(orderInput);
        helper.readOrders(scanner, customers, inventory, orders);

        assertTrue(outputStream.toString().contains("INVALID_PAYMENT_METHOD"));
    }

    @Test
    void readOrders_emptyCart() {
        String customerInput = "1\n" +
                "C001 John john@example.com \"123 Street\" Regular";

        String orderInput = "1\n" +
                "O001 C001 Online CreditCard";  // Order with no cart

        Scanner scanner = new Scanner(customerInput);
        helper.readCustomers(scanner, customers);

        scanner = new Scanner(orderInput);
        helper.readOrders(scanner, customers, inventory, orders);

        assertTrue(outputStream.toString().contains("Order cannot be processed: Cart is empty"));
    }
}
