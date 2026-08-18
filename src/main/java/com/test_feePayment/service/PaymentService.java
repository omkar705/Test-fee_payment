package com.test_feePayment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;

import com.test_feePayment.config.RazorpayConfig;
import com.test_feePayment.model.*;
import com.test_feePayment.repository.PaymentGatewayLogRepository;
import com.test_feePayment.repository.PaymentSettlementJdbcRepository;
import com.test_feePayment.repository.ReceiptRepository;
import com.test_feePayment.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentGatewayLogRepository logRepository;
    private final ReceiptRepository receiptRepository;
    private final PaymentSettlementJdbcRepository settlementRepository;
    private final RazorpayConfig razorpayConfig;
    private final RazorpayClient razorpayClient;

    public PaymentService(
            TransactionRepository transactionRepository,
            PaymentGatewayLogRepository logRepository,
            ReceiptRepository receiptRepository,
            PaymentSettlementJdbcRepository settlementRepository,
            RazorpayConfig razorpayConfig,
            RazorpayClient razorpayClient) {

        this.transactionRepository = transactionRepository;
        this.logRepository = logRepository;
        this.receiptRepository = receiptRepository;
        this.settlementRepository = settlementRepository;
        this.razorpayConfig = razorpayConfig;
        this.razorpayClient = razorpayClient;
    }

@Transactional
public PaymentSimulationResponse simulatePayment(
        PaymentSimulationRequest request) {

    String reference =
            "TXN-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();

    String paymentMethod = request.getPaymentMethod();

    if (paymentMethod == null || paymentMethod.isBlank()) {
        paymentMethod = "card";
    }

    String methodName = switch (paymentMethod.toLowerCase()) {
        case "upi" -> "UPI";
        case "netbanking" -> "Net Banking";
        case "card" -> "Credit / Debit Card";
        default -> "Credit / Debit Card";
    };

    /*
     * 1. Create transaction
     */
    Transaction transaction = new Transaction(
            request.getPaymentId(),
            reference,
            "RAZORPAY_SIMULATION",
            "SUCCESS",
            request.getAmount()
    );

    transaction.setVerifiedBy("SYSTEM");

    Long transactionId =
            transactionRepository.saveAndReturnId(transaction);

    /*
     * 2. Create gateway log
     */
    PaymentGatewayLog gatewayLog = new PaymentGatewayLog(
            transactionId,
            "RAZORPAY_SIMULATION",
            "{\"payment_method\":\"" + methodName + "\"}",
            "{\"status\":\"success\",\"reference\":\"" + reference + "\"}",
            "SUCCESS"
    );

    logRepository.save(gatewayLog);

    /*
     * 3. Generate receipt
     */
    String receiptUrl =
            "/api/payments/receipts/transaction/" + transactionId;

    Receipt receipt = new Receipt(
            transactionId,
            request.getStudentId(),
            receiptUrl
    );

    receiptRepository.save(receipt);

    /*
     * 4. Read receipt back from DB
     */
    Receipt savedReceipt =
            receiptRepository
                    .findByTransactionId(transactionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Receipt was not generated"));

    /*
     * 5. Return response to frontend
     */
    PaymentSimulationResponse response =
            new PaymentSimulationResponse();

    response.setSuccess(true);
    response.setTransactionId(transactionId);
    response.setTransactionReference(reference);
    response.setPaymentStatus("SUCCESS");
    response.setAmount(request.getAmount());
    response.setPaymentMethod(methodName);
    response.setGatewayName("Razorpay Simulation");
    response.setReceiptId(savedReceipt.getReceiptId());
    response.setReceiptUrl(receiptUrl);
    response.setTransactionDate(
            OffsetDateTime.now().toString());
    response.setMessage(
            "Payment simulation completed successfully");

    return response;
}
    // =========================================================
    // EXISTING TRANSACTION OPERATIONS
    // =========================================================

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


    // =========================================================
    // GATEWAY LOG
    // =========================================================

    public int logGatewayActivity(PaymentGatewayLog log) {
        return logRepository.save(log);
    }

    public List<PaymentGatewayLog> getLogsForTransaction(
            Long transactionId) {

        return logRepository.findByTransactionId(transactionId);
    }


    // =========================================================
    // RECEIPT
    // =========================================================

    public int generateReceipt(Receipt receipt) {
        return receiptRepository.save(receipt);
    }

    public Optional<Receipt> getReceiptByTransactionId(
            Long transactionId) {

        return receiptRepository.findByTransactionId(transactionId);
    }


    // =========================================================
    // RAZORPAY PAYMENT SIMULATION
    // =========================================================

    @Transactional
    public PaymentSimulationResponse simulateRazorpayPayment(
            PaymentSimulationRequest request) {

        // -----------------------------------------------------
        // 1. Generate simulated Razorpay transaction reference
        // -----------------------------------------------------

        String transactionReference =
                "pay_" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 14);


        // -----------------------------------------------------
        // 2. Create Transaction
        // -----------------------------------------------------

        Transaction transaction =
                new Transaction();

        transaction.setPaymentId(
                request.getPaymentId()
        );

        transaction.setTransactionReference(
                transactionReference
        );

        transaction.setGatewayName(
                "RAZORPAY_SIMULATION"
        );

        transaction.setTransactionStatus(
                "SUCCESS"
        );

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setVerifiedBy(
                "RAZORPAY_SIMULATOR"
        );

        transaction.setVersion(1L);

        int transactionRows =
                transactionRepository.save(transaction);

        if (transactionRows <= 0) {

            throw new RuntimeException(
                    "Transaction could not be created"
            );
        }


        // -----------------------------------------------------
        // 3. Read transaction back from database
        // -----------------------------------------------------

        Optional<Transaction> savedTransaction =
                transactionRepository
                        .findByTransactionReference(
                                transactionReference
                        );

        if (savedTransaction.isEmpty()) {

            throw new RuntimeException(
                    "Transaction was created but could not be retrieved"
            );
        }

        Transaction dbTransaction =
                savedTransaction.get();


        // -----------------------------------------------------
        // 4. Simulate Razorpay Gateway Log
        // -----------------------------------------------------

        String requestData =
                "{"
                + "\"gateway\":\"RAZORPAY\","
                + "\"simulation\":true,"
                + "\"amount\":" + request.getAmount()
                + "}";


        String responseData =
                "{"
                + "\"status\":\"success\","
                + "\"payment_id\":\""
                + transactionReference
                + "\","
                + "\"simulation\":true"
                + "}";


        PaymentGatewayLog gatewayLog =
                new PaymentGatewayLog();

        gatewayLog.setTransactionId(
                dbTransaction.getTransactionId()
        );

        gatewayLog.setGatewayName(
                "RAZORPAY_SIMULATION"
        );

        gatewayLog.setRequestData(
                requestData
        );

        gatewayLog.setResponseData(
                responseData
        );

        gatewayLog.setStatus(
                "SUCCESS"
        );

        logRepository.save(gatewayLog);


        // -----------------------------------------------------
        // 5. Generate Receipt
        // -----------------------------------------------------

        String receiptUrl =
                "/api/payments/receipts/transaction/"
                + dbTransaction.getTransactionId();


        Receipt receipt =
                new Receipt();

        receipt.setTransactionId(
                dbTransaction.getTransactionId()
        );

        receipt.setStudentId(
                request.getStudentId()
        );

        receipt.setReceiptURL(
                receiptUrl
        );

        int receiptRows =
                receiptRepository.save(receipt);

        if (receiptRows <= 0) {

            throw new RuntimeException(
                    "Receipt could not be generated"
            );
        }


        // -----------------------------------------------------
        // 6. Read generated receipt
        // -----------------------------------------------------

        Optional<Receipt> savedReceipt =
                receiptRepository
                        .findByTransactionId(
                                dbTransaction.getTransactionId()
                        );


        // -----------------------------------------------------
        // 7. Return everything to frontend
        // -----------------------------------------------------

        Long receiptId = null;
        String finalReceiptUrl = receiptUrl;

        if (savedReceipt.isPresent()) {

            receiptId =
                    savedReceipt.get().getReceiptId();

            finalReceiptUrl =
                    savedReceipt.get().getReceiptURL();
        }


        OffsetDateTime transactionDate =
                dbTransaction.getTransactionDate();


        return new PaymentSimulationResponse(

                true,

                "Razorpay payment simulated successfully",

                dbTransaction.getTransactionId(),

                dbTransaction.getTransactionReference(),

                dbTransaction.getTransactionStatus(),

                dbTransaction.getAmount(),

                request.getPaymentMethod(),

                "RAZORPAY_SIMULATION",

                transactionDate != null
                        ? transactionDate.toString()
                        : OffsetDateTime.now().toString(),

                receiptId,

                finalReceiptUrl
        );
    }


    // =========================================================
    // SETTLEMENT OPERATIONS
    // =========================================================

    public int createSettlement(
            PaymentSettlement settlement) {

        return settlementRepository.save(
                settlement
        );
    }

    public List<PaymentSettlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    public Optional<PaymentSettlement> getSettlementById(
            Long id) {

        return settlementRepository.findById(id);
    }

    public int updateSettlementStatus(
            Long id,
            String status) {

        return settlementRepository.updateStatus(
                id,
                status
        );
    }

    public int deleteSettlementById(Long id) {
        return settlementRepository.deleteById(id);
    }

    // =========================================================
    // REAL RAZORPAY STANDARD CHECKOUT OPERATIONS
    // =========================================================

    public CreateOrderResponse createRazorpayOrder(CreateOrderRequest request) {
        CreateOrderResponse response = new CreateOrderResponse();
        try {
            BigDecimal amount = request.getAmount() != null ? request.getAmount() : new BigDecimal("5050.00");
            long amountInPaise = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
            String currency = request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency() : "INR";

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "rcpt_" + (request.getStudentId() != null ? request.getStudentId() : "stu") + "_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);

            String orderId = order.get("id");

            response.setSuccess(true);
            response.setOrderId(orderId);
            response.setAmountInPaise(amountInPaise);
            response.setAmount(amount);
            response.setCurrency(currency);
            response.setKeyId(razorpayConfig.getKeyId());
            response.setStudentId(request.getStudentId());
            response.setPaymentId(request.getPaymentId());
            response.setMessage("Razorpay order created successfully");

            return response;
        } catch (RazorpayException e) {
            response.setSuccess(false);
            response.setMessage("Failed to create Razorpay order: " + e.getMessage());
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Unexpected error creating order: " + e.getMessage());
            return response;
        }
    }

    @Transactional
    public PaymentVerifyResponse verifyAndProcessRazorpayPayment(PaymentVerifyRequest request) {
        PaymentVerifyResponse response = new PaymentVerifyResponse();

        String orderId = request.getRazorpayOrderId();
        String paymentId = request.getRazorpayPaymentId();
        String signature = request.getRazorpaySignature();
        String secret = razorpayConfig.getKeySecret();

        boolean isValid = false;

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            isValid = Utils.verifyPaymentSignature(options, secret);
        } catch (Exception e) {
            isValid = verifyHmacSha256(orderId + "|" + paymentId, signature, secret);
        }

        if (!isValid) {
            response.setSuccess(false);
            response.setMessage("Payment signature verification failed");
            return response;
        }

        /*
         * 1. Save Transaction to PostgreSQL
         */
        Double amount = request.getAmount() != null ? request.getAmount().doubleValue() : 5050.0;
        Transaction transaction = new Transaction(
                request.getPaymentId() != null ? request.getPaymentId() : 1L,
                paymentId,
                "RAZORPAY",
                "SUCCESS",
                amount
        );
        transaction.setVerifiedBy("RAZORPAY_SIGNATURE_VERIFIED");
        transaction.setVersion(1L);

        Long transactionId = transactionRepository.saveAndReturnId(transaction);

        /*
         * 2. Log to payment_gateway_logs
         */
        String reqJson = "{\"order_id\":\"" + orderId + "\",\"payment_id\":\"" + paymentId + "\",\"method\":\"" + (request.getPaymentMethod() != null ? request.getPaymentMethod() : "RAZORPAY") + "\"}";
        String resJson = "{\"signature\":\"" + signature + "\",\"status\":\"SUCCESS\"}";
        PaymentGatewayLog gatewayLog = new PaymentGatewayLog(
                transactionId,
                "RAZORPAY",
                reqJson,
                resJson,
                "SUCCESS"
        );
        logRepository.save(gatewayLog);

        /*
         * 3. Generate Receipt
         */
        String receiptUrl = "/api/payments/receipts/transaction/" + transactionId;
        Receipt receipt = new Receipt(
                transactionId,
                request.getStudentId() != null ? request.getStudentId() : 1L,
                receiptUrl
        );
        receiptRepository.save(receipt);

        Receipt savedReceipt = receiptRepository.findByTransactionId(transactionId)
                .orElse(null);

        /*
         * 4. Populate Response
         */
        response.setSuccess(true);
        response.setMessage("Payment verified and recorded successfully");
        response.setTransactionId(transactionId);
        response.setTransactionReference(paymentId);
        response.setRazorpayPaymentId(paymentId);
        response.setRazorpayOrderId(orderId);
        response.setPaymentStatus("SUCCESS");
        response.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(amount));
        if (savedReceipt != null) {
            response.setReceiptId(savedReceipt.getReceiptId());
            response.setReceiptUrl(receiptUrl);
        }
        response.setTransactionDate(OffsetDateTime.now().toString());

        return response;
    }

    private boolean verifyHmacSha256(String data, String signature, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                    new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}