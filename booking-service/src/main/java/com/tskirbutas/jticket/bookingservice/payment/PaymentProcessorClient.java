package com.tskirbutas.jticket.bookingservice.payment;

/**
 * PaymentProcessorClient represents some external 3rd-party payment processor
 * */
interface PaymentProcessorClient {
    InitProcessPaymentResponse initProcessPayment(PaymentDetails paymentDetails);
}
