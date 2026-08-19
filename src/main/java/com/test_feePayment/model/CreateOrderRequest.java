package com.test_feePayment.model;

import java.math.BigDecimal;

public class CreateOrderRequest {

    private Long paymentId;
    private Long studentId;
    private BigDecimal amount;
    private String currency = "INR";

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long paymentId, Long studentId, BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.studentId = studentId;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
