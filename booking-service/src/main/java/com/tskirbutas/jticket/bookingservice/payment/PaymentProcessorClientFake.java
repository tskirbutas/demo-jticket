package com.tskirbutas.jticket.bookingservice.payment;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple fake payment processor client that returns some random paymentId
 */
@Component
class PaymentProcessorClientFake implements PaymentProcessorClient {

    @Override
    public InitProcessPaymentResponse initProcessPayment(PaymentDetails paymentDetails) {
        var paymentId = ThreadLocalRandom.current().nextLong(); // fake some 3rd party response
        return new InitProcessPaymentResponse(paymentId, null);
    }
}

//TODO: consider invoking the webhook from here after some delay
// --  otherwise one has to call the webhook manually
//class PaymentProcessorClientFakeAutoWebhook implements PaymentProcessorClient {
//
//    final RestClient restClient;
//    int webhookInvocationDelay_millis;
//    @Value("${app.base-url}")
//    String baseUrl;
//
//    PaymentProcessorClientFake(
//            RestClient restClient,
//            @Value("${payment.client-fake.webhook-delay-millis:2000}") int webhookInvocationDelay_millis
//    ) {
//        this.restClient = restClient;
//        this.webhookInvocationDelay_millis = webhookInvocationDelay_millis;
//    }
//
//
//    @Override
//    public InitProcessPaymentResponse initProcessPayment(PaymentDetails paymentDetails) {
//        var paymentId = ThreadLocalRandom.current().nextLong(); // fake some 3rd party response
//
//        var completedRequest = new PaymentProcessingCompletedRequest(paymentId, true, null);
//        var url = String.format("%s%s", baseUrl, WEBHOOK_FAKE_PAYMENT_PROCESSOR);
//        CompletableFuture.runAsync(() -> {
//                    restClient.post()
//                            .uri(url)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .body(completedRequest)
//                            .retrieve()
//                            .toBodilessEntity();
//                },
//                CompletableFuture.delayedExecutor(webhookInvocationDelay_millis, TimeUnit.MILLISECONDS));
//
//        return new InitProcessPaymentResponse(paymentId, null);
//    }
//}