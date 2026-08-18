package com.test_feePayment.model;

public class PaymentSimulationResponse {

    private boolean success;
    private String message;

    private Long transactionId;
    private String transactionReference;
    private String transactionStatus;

    private Double amount;
    private String paymentMethod;
    private String gatewayName;
    private String transactionDate;

    private Long receiptId;
    private String receiptUrl;

    private String paymentStatus;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public PaymentSimulationResponse() {
    }


    public PaymentSimulationResponse(
            boolean success,
            String message,
            Long transactionId,
            String transactionReference,
            String transactionStatus,
            Double amount,
            String paymentMethod,
            String gatewayName,
            String transactionDate,
            Long receiptId,
            String receiptUrl) {

        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.gatewayName = gatewayName;
        this.transactionDate = transactionDate;
        this.receiptId = receiptId;
        this.receiptUrl = receiptUrl;
        this.paymentStatus = transactionStatus;
    }


    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }


    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }


    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }


    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }


    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }


    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }


    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}