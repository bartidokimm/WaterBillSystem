package com.scc.model;

public class Customer {

    private final int customerId;
    private final String customerName;
    private final String address;
    private final String contactNumber;
    private final String status;

    public Customer(int customerId, String customerName, String address,
                    String contactNumber, String status) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.status = status;
    }

    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getStatus() { return status; }
}