package paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PaymentService {

    Session createCheckoutSession(double amount, String email, String orderNumber, List<String> skuCodes) throws StripeException;

    void handlePaymentStatus(String status, String email, String orderNumber, String skuCodes) throws ExecutionException, InterruptedException;
}
