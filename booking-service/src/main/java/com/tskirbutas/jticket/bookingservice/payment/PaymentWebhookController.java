package com.tskirbutas.jticket.bookingservice.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentWebhookController {
    public static final String WEBHOOK_FAKE_PAYMENT_PROCESSOR = "/webhook/fake-payment-processor";

    final PaymentService paymentService;

    PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(WEBHOOK_FAKE_PAYMENT_PROCESSOR)
    ResponseEntity<Void> handle(@RequestBody PaymentProcessingCompletedRequest completedRequest) {
        paymentService.paymentCompleted(completedRequest.paymentId(), completedRequest.success(), completedRequest.failureReason());
        // For simplicity assumes that 200 OK is enough to act as ACK
        return ResponseEntity.ok().build();
    }
}
