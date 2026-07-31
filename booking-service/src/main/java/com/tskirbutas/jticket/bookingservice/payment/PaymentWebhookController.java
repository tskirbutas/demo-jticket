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
        //TODO: #paymentCompleted should say if everything is ok so that we could send ACK or not
        paymentService.paymentCompleted(completedRequest.paymentId(), completedRequest.success(), completedRequest.failureReason());
        return ResponseEntity.ok().build();
    }
}
