package com.test_feePayment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_gateway_logs")
public class PaymentGatewayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gatewayLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonIgnore
    private Transaction transaction;

    @Column(name = "gateway_name", nullable = false)
    private String gatewayName;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "status", nullable = false)
    private String status; // INITIATE, VERIFY, STATUS_CHECK, REFUND, SUCCESS, FAILED

    @Column(name = "log_time")
    private LocalDateTime logTime;

    public PaymentGatewayLog() {
    }

    public PaymentGatewayLog(String gatewayName, String requestData, String responseData, String status) {
        this.gatewayName = gatewayName;
        this.requestData = requestData;
        this.responseData = responseData;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (this.logTime == null) {
            this.logTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getGatewayLogId() {
        return gatewayLogId;
    }

    public void setGatewayLogId(Long gatewayLogId) {
        this.gatewayLogId = gatewayLogId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }
}
