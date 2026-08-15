package com.test_feePayment.controller;

import com.test_feePayment.model.PaymentGatewayLog;
import com.test_feePayment.model.PaymentSettlement;
import com.test_feePayment.model.Receipt;
import com.test_feePayment.model.Transaction;
import com.test_feePayment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/status")
    public String checkStatus() {
        return "Team 2 Native SQL Data Access Layer (transactions, payment_gateway_logs, receipts, payment_settlements) active and connected to Supabase PostgreSQL without ORM/Hibernate.";
    }

    @PostMapping("/transactions")
    public ResponseEntity<String> createTransaction(@RequestBody Transaction transaction) {
        int rows = paymentService.createTransaction(transaction);
        if (rows > 0) {
            return ResponseEntity.ok("Transaction inserted into Supabase PostgreSQL successfully via Native SQL!");
        }
        return ResponseEntity.badRequest().body("Failed to insert transaction.");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactions());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return paymentService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/transactions/{id}/status")
    public ResponseEntity<String> updateTransactionStatus(@PathVariable Long id, @RequestParam String status) {
        int updated = paymentService.updateTransactionStatus(id, status);
        if (updated > 0) {
            return ResponseEntity.ok("Transaction status updated in Supabase PostgreSQL via Native SQL!");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/logs")
    public ResponseEntity<String> createGatewayLog(@RequestBody PaymentGatewayLog log) {
        int rows = paymentService.logGatewayActivity(log);
        if (rows > 0) {
            return ResponseEntity.ok("Gateway log inserted into Supabase PostgreSQL successfully via Native SQL!");
        }
        return ResponseEntity.badRequest().body("Failed to insert gateway log.");
    }

    @GetMapping("/logs/transaction/{transactionId}")
    public ResponseEntity<List<PaymentGatewayLog>> getLogsByTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(paymentService.getLogsForTransaction(transactionId));
    }

    @PostMapping("/receipts")
    public ResponseEntity<String> createReceipt(@RequestBody Receipt receipt) {
        int rows = paymentService.generateReceipt(receipt);
        if (rows > 0) {
            return ResponseEntity.ok("Receipt inserted into Supabase PostgreSQL successfully via Native SQL!");
        }
        return ResponseEntity.badRequest().body("Failed to insert receipt.");
    }

    @GetMapping("/receipts/transaction/{transactionId}")
    public ResponseEntity<Receipt> getReceiptByTransaction(@PathVariable Long transactionId) {
        return paymentService.getReceiptByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Settlement Endpoints
    @PostMapping("/settlements")
    public ResponseEntity<String> createSettlement(@RequestBody PaymentSettlement settlement) {
        int rows = paymentService.createSettlement(settlement);
        if (rows > 0) {
            return ResponseEntity.ok("Payment Settlement inserted into Supabase PostgreSQL successfully via Native SQL!");
        }
        return ResponseEntity.badRequest().body("Failed to insert settlement.");
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<PaymentSettlement>> getAllSettlements() {
        return ResponseEntity.ok(paymentService.getAllSettlements());
    }

    @GetMapping("/settlements/{id}")
    public ResponseEntity<PaymentSettlement> getSettlementById(@PathVariable Long id) {
        return paymentService.getSettlementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/settlements/{id}/status")
    public ResponseEntity<String> updateSettlementStatus(@PathVariable Long id, @RequestParam String status) {
        int updated = paymentService.updateSettlementStatus(id, status);
        if (updated > 0) {
            return ResponseEntity.ok("Settlement status updated in Supabase PostgreSQL via Native SQL!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/settlements/{id}")
    public ResponseEntity<String> deleteSettlement(@PathVariable Long id) {
        int deleted = paymentService.deleteSettlementById(id);
        if (deleted > 0) {
            return ResponseEntity.ok("Settlement deleted from Supabase PostgreSQL via Native SQL!");
        }
        return ResponseEntity.notFound().build();
    }
}

