package com.test_feePayment.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentSettlement {

    private Long settlementId;
    private Long transactionId;
    private String merchantAccount;
    private BigDecimal settledAmount;
    private BigDecimal commissionFee;
    private String settlementStatus;
    private OffsetDateTime settlementDate;
    private OffsetDateTime createdAt;

    public PaymentSettlement() {
    }

    public PaymentSettlement(Long transactionId, String merchantAccount, BigDecimal settledAmount, BigDecimal commissionFee, String settlementStatus) {
        this.transactionId = transactionId;
        this.merchantAccount = merchantAccount;
        this.settledAmount = settledAmount;
        this.commissionFee = commissionFee;
        this.settlementStatus = settlementStatus;
    }

    public Long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Long settlementId) {
        this.settlementId = settlementId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getMerchantAccount() {
        return merchantAccount;
    }

    public void setMerchantAccount(String merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(BigDecimal settledAmount) {
        this.settledAmount = settledAmount;
    }

    public BigDecimal getCommissionFee() {
        return commissionFee;
    }

    public void setCommissionFee(BigDecimal commissionFee) {
        this.commissionFee = commissionFee;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public OffsetDateTime getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(OffsetDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
