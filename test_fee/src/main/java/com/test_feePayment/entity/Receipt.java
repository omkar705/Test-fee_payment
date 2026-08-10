package com.test_feePayment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    @JsonIgnore
    private Transaction transaction;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate;

    @Column(name = "receipt_url")
    private String receiptURL;

    public Receipt() {
    }

    public Receipt(Long studentId, String receiptURL) {
        this.studentId = studentId;
        this.receiptURL = receiptURL;
    }

    @PrePersist
    protected void onCreate() {
        if (this.generatedDate == null) {
            this.generatedDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getReceiptURL() {
        return receiptURL;
    }

    public void setReceiptURL(String receiptURL) {
        this.receiptURL = receiptURL;
    }
}
