package com.tskirbutas.jticket.bookingservice.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {
    public static final String WEBHOOK_FAKE_PAYMENT_PROCESSOR = "/webhook/fake-payment-processor";

    final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment/{id}")
    Payment findPaymentById(@PathVariable long id) {
        return paymentService.findById(id);
    }

    @PostMapping(WEBHOOK_FAKE_PAYMENT_PROCESSOR)
    ResponseEntity<Void> handle(@RequestBody PaymentProcessingCompletedRequest completedRequest) {
        paymentService.paymentCompleted(completedRequest.paymentId(), completedRequest.success(), completedRequest.failureReason());
        // For simplicity assumes that 200 OK is enough to act as ACK
        return ResponseEntity.ok().build();
    }
}
