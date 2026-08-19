package com.test_feePayment.model;

import java.time.OffsetDateTime;

public class Receipt {

    private Long receiptId;
    private Long transactionId;
    private Long studentId;
    private OffsetDateTime generatedDate;
    private String receiptURL;

    public Receipt() {
    }

    public Receipt(Long transactionId, Long studentId, String receiptURL) {
        this.transactionId = transactionId;
        this.studentId = studentId;
        this.receiptURL = receiptURL;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public OffsetDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(OffsetDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getReceiptURL() {
        return receiptURL;
    }

    public void setReceiptURL(String receiptURL) {
        this.receiptURL = receiptURL;
    }
}
