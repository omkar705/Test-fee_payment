package com.test_feePayment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id:rzp_test_TQtCrKTrktLiwZ}")
    private String keyId;

    @Value("${razorpay.key.secret:wvrEs8bf4D35fgv1HJQeujIj}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId.trim(), keySecret.trim());
    }

    public String getKeyId() {
        return keyId != null ? keyId.trim() : "";
    }

    public String getKeySecret() {
        return keySecret != null ? keySecret.trim() : "";
    }
}
