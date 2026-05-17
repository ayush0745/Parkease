package com.parkease.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * RazorpayPaymentService — wraps Razorpay SDK for payment processing.
 *
 * Razorpay amounts are in the smallest currency unit (paise for INR).
 * Amount is multiplied by 100: ₹100.00 → 10000 paise.
 */
@Slf4j
@Service
public class RazorpayPaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient client;

    @PostConstruct
    public void init() throws Exception {
        client = new RazorpayClient(keyId, keySecret);
        log.info("Razorpay SDK initialized");
    }

    /**
     * Create a Razorpay order and return the order ID.
     * The frontend uses this order ID to open the Razorpay checkout.
     *
     * @param amount      amount in INR (converted to paise internally)
     * @param description receipt/notes description
     * @return Razorpay order ID (stored as transactionId)
     */
    public String processPayment(BigDecimal amount, String description) throws Exception {
        JSONObject options = new JSONObject();
        options.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue()); // paise
        options.put("currency", "INR");
        options.put("receipt", description);
        options.put("payment_capture", 1); // auto-capture

        Order order = client.orders.create(options);
        String orderId = order.get("id");
        log.info("Razorpay order created: orderId={}, amount={}INR", orderId, amount);
        return orderId;
    }

    /**
     * Refund a captured Razorpay payment.
     *
     * @param paymentId Razorpay payment ID to refund
     * @param amount    amount to refund in INR (null = full refund)
     * @return Razorpay refund ID
     */
    public String refundPayment(String paymentId, BigDecimal amount) throws Exception {
        JSONObject options = new JSONObject();
        if (amount != null) {
            options.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue()); // paise
        }

        Refund refund = client.payments.refund(paymentId, options);
        String refundId = refund.get("id");
        log.info("Razorpay refund created: refundId={}, paymentId={}", refundId, paymentId);
        return refundId;
    }
}
