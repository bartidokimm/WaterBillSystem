package com.scc.model;

public class Bill {

    private final int billId;
    private final int customerId;
    private final double consumption;
    private final double billAmount;
    private final String billDate;
    private final String paymentStatus;

    public Bill(int billId, int customerId, double consumption,
                double billAmount, String billDate, String paymentStatus) {
        this.billId = billId;
        this.customerId = customerId;
        this.consumption = consumption;
        this.billAmount = billAmount;
        this.billDate = billDate;
        this.paymentStatus = paymentStatus;
    }

    public int getBillId() { return billId; }
    public int getCustomerId() { return customerId; }
    public double getConsumption() { return consumption; }
    public double getBillAmount() { return billAmount; }
    public String getBillDate() { return billDate; }
    public String getPaymentStatus() { return paymentStatus; }
}