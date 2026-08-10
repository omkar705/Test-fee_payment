package com.test_feePayment.service;

import com.test_feePayment.repository.PaymentGatewayLogRepository;
import com.test_feePayment.repository.ReceiptRepository;
import com.test_feePayment.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentGatewayLogRepository logRepository;
    private final ReceiptRepository receiptRepository;

    public PaymentService(TransactionRepository transactionRepository,
                          PaymentGatewayLogRepository logRepository,
                          ReceiptRepository receiptRepository) {
        this.transactionRepository = transactionRepository;
        this.logRepository = logRepository;
        this.receiptRepository = receiptRepository;
    }

    // Database tables (transactions, payment_gateway_logs, receipts) are defined via JPA Entities
    // and automatically created in Supabase PostgreSQL by Hibernate when the application runs.
    // Business & payment processing logic will be added here later.
}
