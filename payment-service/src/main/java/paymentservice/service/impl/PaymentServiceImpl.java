package paymentservice.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.paymentservice.event.PaymentEvent;
import org.service.paymentservice.service.PaymentService;
import org.service.paymentservice.service.ProducerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${api.stripe.key}")
    String stripeApiKey;

    private final ProducerService producerService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public Session createCheckoutSession(double amount, String email, String orderNumber, List<String> skuCodes) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(createPaymentUrl("http://localhost:9000/api/payments/payment/success?session_id={CHECKOUT_SESSION_ID}", skuCodes))
                .setCancelUrl(createPaymentUrl("http://localhost:9000/api/payments/payment/fail?session_id={CHECKOUT_SESSION_ID}", skuCodes))
                .setCustomerEmail(email)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((long) (amount * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + orderNumber)
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("order_number", orderNumber)
                .putMetadata("user_email", email)
                .build();

        Session session = Session.create(params);

        return session;
    }

    private String createPaymentUrl(String url, List<String> skuCodes) {
        String skuCodesParam = skuCodes.stream()
                .map(sku -> URLEncoder.encode(sku, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));

        return url + "&sku=" + skuCodesParam;
    }

    public void handlePaymentStatus(String status, String email, String orderNumber, String skuCodes) throws ExecutionException, InterruptedException {
        List<String> skuCodesList = Arrays.stream(skuCodes.split(","))
                .map(String::trim)
                .toList();

        PaymentEvent paymentEvent = new PaymentEvent(status, orderNumber, email, skuCodesList);

        producerService.sendEventToKafka(paymentEvent);
    }
}
