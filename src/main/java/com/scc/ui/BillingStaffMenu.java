package com.scc.ui;

import com.scc.dao.BillDao;
import com.scc.dao.CustomerDao;
import com.scc.model.Bill;
import com.scc.model.Customer;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BillingStaffMenu {

    private static final String[] BILL_STATUSES = {"UNPAID", "PAID"};

    private static final int CANCELLED = 0;

    private final Scanner scanner;
    private final CustomerDao customerDao;
    private final BillDao billDao;

    public BillingStaffMenu(Scanner scanner, CustomerDao customerDao,
                            BillDao billDao) {
        this.scanner = scanner;
        this.customerDao = customerDao;
        this.billDao = billDao;
    }

    public void show(User staff) {
        boolean loggedIn = true;

        while (loggedIn) {
            ConsoleUi.printHeader("BILLING STAFF MENU");
            System.out.println("Logged in as: " + staff.getUsername());
            System.out.println("1. Add Customer");
            System.out.println("2. View Customers");
            System.out.println("3. Process Bill");
            System.out.println("4. View Bills");
            System.out.println("5. Update Bill Payment Status");
            System.out.println("6. Log Out");
            System.out.println("----------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addCustomer(staff);
                    break;

                case "2":
                    viewCustomers();
                    break;

                case "3":
                    processBill(staff);
                    break;

                case "4":
                    viewBills();
                    break;

                case "5":
                    updateBillStatus(staff);
                    break;

                case "6":
                    loggedIn = false;
                    System.out.println(
                            "You have logged out successfully."
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    private void addCustomer(User staff) {
        System.out.println("\n--- ADD CUSTOMER ---");

        String name;
        while (true) {
            System.out.print("Enter customer name (0 to cancel): ");
            name = scanner.nextLine().trim();

            if (name.equals("0")) {
                System.out.println("Add customer cancelled.");
                return;
            }
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("Customer name is required.");
        }

        System.out.print("Enter address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter contact number: ");
        String contact = scanner.nextLine().trim();

        try {
            int id = customerDao.addCustomer(
                    staff.getUserId(), name, address, contact);

            if (id > 0) {
                System.out.println("Customer '" + name
                        + "' added with ID " + id + ".");
            } else {
                System.out.println("Unable to add customer.");
            }

        } catch (SQLException e) {
            System.out.println("Add customer failed: " + e.getMessage());
        }
    }

    private void viewCustomers() {
        System.out.println("\n--- CUSTOMERS ---");

        try {
            List<Customer> customers = customerDao.getAllCustomers();

            if (customers.isEmpty()) {
                System.out.println("No customers found.");
                return;
            }

            printCustomerTable(customers);

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load customers: " + e.getMessage());
        }
    }

    private void processBill(User staff) {
        System.out.println("\n--- PROCESS BILL ---");

        try {
            List<Customer> customers = customerDao.getAllCustomers();

            if (customers.isEmpty()) {
                System.out.println(
                        "No customers found. Add a customer first.");
                return;
            }

            printCustomerTable(customers);

            Customer target;
            while (true) {
                int customerId = readIntInput(
                        "Enter Customer ID (0 to cancel): ");

                if (customerId == CANCELLED) {
                    System.out.println("Billing cancelled.");
                    return;
                }

                target = findCustomer(customers, customerId);

                if (target == null) {
                    System.out.println("Customer ID " + customerId
                            + " does not exist.");
                    continue;
                }
                break;
            }

            double previous = readDoubleInput(
                    "Enter previous meter reading: ");

            double current;
            while (true) {
                current = readDoubleInput(
                        "Enter current meter reading: ");

                if (current >= previous) {
                    break;
                }
                System.out.println("Current reading cannot be lower "
                        + "than the previous reading.");
            }

            int billId = billDao.processBill(
                    staff.getUserId(), target.getCustomerId(),
                    previous, current);

            if (billId > 0) {
                double consumption = current - previous;
                System.out.println(String.format(
                        "Bill #%d created for %s: %.2f m3 x P%.2f = P%.2f",
                        billId, target.getCustomerName(), consumption,
                        BillDao.RATE_PER_CUBIC_METER,
                        consumption * BillDao.RATE_PER_CUBIC_METER));
            } else {
                System.out.println("Unable to process bill.");
            }

        } catch (SQLException e) {
            System.out.println("Billing failed: " + e.getMessage());
        }
    }

    private void viewBills() {
        System.out.println("\n--- BILLS ---");

        try {
            List<Bill> bills = billDao.getAllBills();

            if (bills.isEmpty()) {
                System.out.println("No bills found.");
                return;
            }

            printBillTable(bills);

        } catch (SQLException e) {
            System.out.println("Unable to load bills: " + e.getMessage());
        }
    }

    private void updateBillStatus(User staff) {
        System.out.println("\n--- UPDATE BILL PAYMENT STATUS ---");

        try {
            List<Bill> bills = billDao.getAllBills();

            if (bills.isEmpty()) {
                System.out.println("No bills found.");
                return;
            }

            printBillTable(bills);

            while (true) {
                int billId = readIntInput(
                        "Enter Bill ID to update (0 to cancel): ");

                if (billId == CANCELLED) {
                    System.out.println("Update cancelled.");
                    return;
                }

                if (findBill(bills, billId) == null) {
                    System.out.println("Bill ID " + billId
                            + " does not exist.");
                    continue;
                }

                String newStatus = readValidBillStatus();
                if (newStatus == null) {
                    System.out.println("Update cancelled.");
                    return;
                }

                if (billDao.updatePaymentStatus(
                        staff.getUserId(), billId, newStatus)) {
                    System.out.println("Bill #" + billId
                            + " payment status set to " + newStatus + ".");
                } else {
                    System.out.println("Update failed.");
                }
                return;
            }

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    // ---------- Input validation ----------

    private int readIntInput(String prompt) {
        while (true) {
            System.out.println();
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);
                if (value >= 0) {
                    return value;
                }
                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Returns a valid status, or null if 0 was entered to cancel
    private String readValidBillStatus() {
        return ConsoleUi.chooseOption(scanner, "Valid statuses:",
                BILL_STATUSES, "Enter new status (0 to cancel): ",
                "Invalid status. Please try again.");
    }

    // ---------- Helpers ----------

    private Customer findCustomer(List<Customer> customers, int id) {
        for (Customer c : customers) {
            if (c.getCustomerId() == id) {
                return c;
            }
        }
        return null;
    }

    private Bill findBill(List<Bill> bills, int id) {
        for (Bill b : bills) {
            if (b.getBillId() == id) {
                return b;
            }
        }
        return null;
    }

    private void printCustomerTable(List<Customer> customers) {
        String[] headers = {"ID", "Name", "Address", "Contact"};
        int[] widths = {5, 22, 24, 14};

        List<String[]> rows = new ArrayList<>();
        for (Customer c : customers) {
            rows.add(new String[]{
                    String.valueOf(c.getCustomerId()),
                    c.getCustomerName(),
                    c.getAddress() == null ? "" : c.getAddress(),
                    c.getContactNumber() == null
                            ? "" : c.getContactNumber()
            });
        }

        TablePrinter.print(headers, widths, rows);
    }

    private void printBillTable(List<Bill> bills) {
        String[] headers = {
                "Bill", "Customer", "Consumption", "Amount",
                "Date", "Status"
        };
        int[] widths = {6, 8, 12, 12, 10, 8};
        boolean[] rightAlign = {false, false, true, true, false, false};

        List<String[]> rows = new ArrayList<>();
        for (Bill b : bills) {
            rows.add(new String[]{
                    String.valueOf(b.getBillId()),
                    String.valueOf(b.getCustomerId()),
                    String.format("%.2f", b.getConsumption()),
                    String.format("P%.2f", b.getBillAmount()),
                    b.getBillDate(),
                    b.getPaymentStatus()
            });
        }

        TablePrinter.print(headers, widths, rows, rightAlign);
    }
}