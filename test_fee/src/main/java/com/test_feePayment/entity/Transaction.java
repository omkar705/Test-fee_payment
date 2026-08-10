package com.test_feePayment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // References FeePayment(paymentId) from another team's module

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "gateway_name", nullable = false)
    private String gatewayName;

    @Column(name = "transaction_status", nullable = false)
    private String transactionStatus; // INITIATE, PENDING, SUCCESS, FAILED, REFUNDED

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Version
    @Column(name = "version")
    private Long version; // Required for Hibernate Optimistic Locking

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship: 1 Transaction to Many PaymentGatewayLogs
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("transaction")
    private List<PaymentGatewayLog> gatewayLogs = new ArrayList<>();

    // Relationship: 1 Transaction to 0..1 Receipt
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("transaction")
    private Receipt receipt;

    public Transaction() {
    }

    public Transaction(Long paymentId, String transactionReference, String gatewayName, String transactionStatus, Double amount) {
        this.paymentId = paymentId;
        this.transactionReference = transactionReference;
        this.gatewayName = gatewayName;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Convenience method to add gateway logs
    public void addGatewayLog(PaymentGatewayLog log) {
        gatewayLogs.add(log);
        log.setTransaction(this);
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PaymentGatewayLog> getGatewayLogs() {
        return gatewayLogs;
    }

    public void setGatewayLogs(List<PaymentGatewayLog> gatewayLogs) {
        this.gatewayLogs = gatewayLogs;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        if (receipt != null) {
            receipt.setTransaction(this);
        }
    }
}
