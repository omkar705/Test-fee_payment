package com.test_feePayment.service;

import com.test_feePayment.model.PaymentGatewayLog;
import com.test_feePayment.model.PaymentSettlement;
import com.test_feePayment.model.Receipt;
import com.test_feePayment.model.Transaction;
import com.test_feePayment.repository.PaymentGatewayLogRepository;
import com.test_feePayment.repository.PaymentSettlementJdbcRepository;
import com.test_feePayment.repository.ReceiptRepository;
import com.test_feePayment.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentGatewayLogRepository logRepository;
    private final ReceiptRepository receiptRepository;
    private final PaymentSettlementJdbcRepository settlementRepository;

    public PaymentService(TransactionRepository transactionRepository,
                          PaymentGatewayLogRepository logRepository,
                          ReceiptRepository receiptRepository,
                          PaymentSettlementJdbcRepository settlementRepository) {
        this.transactionRepository = transactionRepository;
        this.logRepository = logRepository;
        this.receiptRepository = receiptRepository;
        this.settlementRepository = settlementRepository;
    }

    public int createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Optional<Transaction> getTransactionByReference(String reference) {
        return transactionRepository.findByTransactionReference(reference);
    }

    public int updateTransactionStatus(Long id, String status) {
        return transactionRepository.updateStatus(id, status);
    }

    public int logGatewayActivity(PaymentGatewayLog log) {
        return logRepository.save(log);
    }

    public List<PaymentGatewayLog> getLogsForTransaction(Long transactionId) {
        return logRepository.findByTransactionId(transactionId);
    }

    public int generateReceipt(Receipt receipt) {
        return receiptRepository.save(receipt);
    }

    public Optional<Receipt> getReceiptByTransactionId(Long transactionId) {
        return receiptRepository.findByTransactionId(transactionId);
    }

    // Settlement Operations
    public int createSettlement(PaymentSettlement settlement) {
        return settlementRepository.save(settlement);
    }

    public List<PaymentSettlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    public Optional<PaymentSettlement> getSettlementById(Long id) {
        return settlementRepository.findById(id);
    }

    public int updateSettlementStatus(Long id, String status) {
        return settlementRepository.updateStatus(id, status);
    }

    public int deleteSettlementById(Long id) {
        return settlementRepository.deleteById(id);
    }
}

