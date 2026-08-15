package com.test_feePayment.model;

import java.time.OffsetDateTime;

public class PaymentGatewayLog {

    private Long gatewayLogId;
    private Long transactionId;
    private String gatewayName;
    private String requestData;
    private String responseData;
    private String status;
    private OffsetDateTime logTime;

    public PaymentGatewayLog() {
    }

    public PaymentGatewayLog(Long transactionId, String gatewayName, String requestData, String responseData, String status) {
        this.transactionId = transactionId;
        this.gatewayName = gatewayName;
        this.requestData = requestData;
        this.responseData = responseData;
        this.status = status;
    }

    public Long getGatewayLogId() {
        return gatewayLogId;
    }

    public void setGatewayLogId(Long gatewayLogId) {
        this.gatewayLogId = gatewayLogId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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

    public OffsetDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(OffsetDateTime logTime) {
        this.logTime = logTime;
    }
}
