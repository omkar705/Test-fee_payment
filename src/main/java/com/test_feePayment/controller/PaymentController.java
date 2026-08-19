package com.test_feePayment.controller;

import com.test_feePayment.model.*;
import com.test_feePayment.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =========================================================
    // STATUS
    // =========================================================

    @GetMapping("/status")
    public String checkStatus() {
        return "Payment system backend is active";
    }

    // =========================================================
    // REAL RAZORPAY STANDARD CHECKOUT
    // =========================================================

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {

        CreateOrderResponse response = paymentService.createRazorpayOrder(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @RequestBody PaymentVerifyRequest request) {

        PaymentVerifyResponse response = paymentService.verifyAndProcessRazorpayPayment(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // =========================================================
    // RAZORPAY PAYMENT SIMULATION
    // =========================================================

    @PostMapping("/simulate")
    public ResponseEntity<PaymentSimulationResponse> simulatePayment(
            @RequestBody PaymentSimulationRequest request) {

        try {
            PaymentSimulationResponse response =
                    paymentService.simulatePayment(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            PaymentSimulationResponse response =
                    new PaymentSimulationResponse();

            response.setSuccess(false);
            response.setMessage(
                    "Payment simulation failed: " + e.getMessage()
            );

            return ResponseEntity.badRequest().body(response);
        }
    }

    // =========================================================
    // TRANSACTIONS
    // =========================================================

    @PostMapping("/transactions")
    public ResponseEntity<String> createTransaction(
            @RequestBody Transaction transaction) {

        int rows = paymentService.createTransaction(transaction);

        if (rows > 0) {
            return ResponseEntity.ok(
                    "Transaction inserted successfully"
            );
        }

        return ResponseEntity.badRequest()
                .body("Failed to insert transaction.");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        return ResponseEntity.ok(
                paymentService.getAllTransactions()
        );
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable Long id) {

        return paymentService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/transactions/{id}/status")
    public ResponseEntity<String> updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        int updated =
                paymentService.updateTransactionStatus(id, status);

        if (updated > 0) {
            return ResponseEntity.ok(
                    "Transaction status updated"
            );
        }

        return ResponseEntity.notFound().build();
    }

    // =========================================================
    // GATEWAY LOGS
    // =========================================================

    @PostMapping("/logs")
    public ResponseEntity<String> createGatewayLog(
            @RequestBody PaymentGatewayLog log) {

        int rows = paymentService.logGatewayActivity(log);

        if (rows > 0) {
            return ResponseEntity.ok(
                    "Gateway log inserted successfully"
            );
        }

        return ResponseEntity.badRequest()
                .body("Failed to insert gateway log.");
    }

    @GetMapping("/logs/transaction/{transactionId}")
    public ResponseEntity<List<PaymentGatewayLog>>
    getLogsByTransaction(
            @PathVariable Long transactionId) {

        return ResponseEntity.ok(
                paymentService.getLogsForTransaction(transactionId)
        );
    }

    // =========================================================
    // RECEIPTS
    // =========================================================

    @PostMapping("/receipts")
    public ResponseEntity<String> createReceipt(
            @RequestBody Receipt receipt) {

        int rows = paymentService.generateReceipt(receipt);

        if (rows > 0) {
            return ResponseEntity.ok(
                    "Receipt generated successfully"
            );
        }

        return ResponseEntity.badRequest()
                .body("Failed to generate receipt.");
    }

    @GetMapping("/receipts/transaction/{transactionId}")
    public ResponseEntity<Receipt> getReceiptByTransaction(
            @PathVariable Long transactionId) {

        return paymentService
                .getReceiptByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // SETTLEMENTS
    // =========================================================

    @PostMapping("/settlements")
    public ResponseEntity<String> createSettlement(
            @RequestBody PaymentSettlement settlement) {

        int rows =
                paymentService.createSettlement(settlement);

        if (rows > 0) {
            return ResponseEntity.ok(
                    "Payment Settlement inserted successfully"
            );
        }

        return ResponseEntity.badRequest()
                .body("Failed to insert settlement.");
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<PaymentSettlement>>
    getAllSettlements() {

        return ResponseEntity.ok(
                paymentService.getAllSettlements()
        );
    }

    @GetMapping("/settlements/{id}")
    public ResponseEntity<PaymentSettlement>
    getSettlementById(@PathVariable Long id) {

        return paymentService
                .getSettlementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/settlements/{id}/status")
    public ResponseEntity<String> updateSettlementStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        int updated =
                paymentService.updateSettlementStatus(id, status);

        if (updated > 0) {
            return ResponseEntity.ok(
                    "Settlement status updated"
            );
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/settlements/{id}")
    public ResponseEntity<String> deleteSettlement(
            @PathVariable Long id) {

        int deleted =
                paymentService.deleteSettlementById(id);

        if (deleted > 0) {
            return ResponseEntity.ok(
                    "Settlement deleted successfully"
            );
        }

        return ResponseEntity.notFound().build();
    }
}
