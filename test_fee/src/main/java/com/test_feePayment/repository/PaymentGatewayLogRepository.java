package com.test_feePayment.repository;

import com.test_feePayment.entity.PaymentGatewayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentGatewayLogRepository extends JpaRepository<PaymentGatewayLog, Long> {
    List<PaymentGatewayLog> findByTransactionTransactionId(Long transactionId);
}
