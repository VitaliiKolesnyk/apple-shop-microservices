package org.service.paymentservice.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.paymentservice.dto.PaymentRequest;
import org.service.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody PaymentRequest paymentRequest) {
        BigDecimal paymentAmount = paymentRequest.getAmount();
        double amountAsDouble = paymentAmount.doubleValue();
        String email = paymentRequest.getUserEmail();
        String orderNumber = paymentRequest.getOrderNumber();
        List<String> skuCodes = paymentRequest.getSkuCodes();

        log.warn("Payment request - {}", paymentRequest);

        try {
            Session session = paymentService.createCheckoutSession(amountAsDouble, email, orderNumber, skuCodes);

            String sessionId = session.getId();

            return Map.of("sessionId", sessionId);
        } catch (StripeException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/payment/success")
    public void handlePaymentSuccess(@RequestParam("session_id") String sessionId, @RequestParam("sku") String skuCodes, HttpServletResponse response) throws StripeException, IOException, ExecutionException, InterruptedException {

        Session session = Session.retrieve(sessionId);

        String orderNumber = session.getMetadata().get("order_number");
        String userEmail = session.getMetadata().get("user_email");

        paymentService.handlePaymentStatus("Success", userEmail, orderNumber, skuCodes);

        response.sendRedirect("http://localhost:30000");
    }

    @GetMapping("/payment/fail")
    public void handlePaymentFail(@RequestParam("session_id") String sessionId, @RequestParam("sku") String skuCodes, HttpServletResponse response) throws StripeException, IOException, ExecutionException, InterruptedException {
        Session session = Session.retrieve(sessionId);

        String orderNumber = session.getMetadata().get("order_number");
        String userEmail = session.getMetadata().get("user_email");

        paymentService.handlePaymentStatus("Fail", userEmail, orderNumber, skuCodes);

        response.sendRedirect("http://localhost:30000");
    }

}
