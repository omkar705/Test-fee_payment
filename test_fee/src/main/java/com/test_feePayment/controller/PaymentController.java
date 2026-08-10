package com.test_feePayment.controller;

import com.test_feePayment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/status")
    public String checkStatus() {
        return "Team 2 Database Schema (transactions, payment_gateway_logs, receipts) is active and connected to Supabase.";
    }
}
