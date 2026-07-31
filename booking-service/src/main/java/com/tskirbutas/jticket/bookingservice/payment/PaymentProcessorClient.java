package com.tskirbutas.jticket.bookingservice.payment;

/**
 * PaymentProcessorClient represents some external 3rd-party payment processor
 * */
interface PaymentProcessorClient {
    InitProcessPaymentResponse processPayment(PaymentDetails paymentDetails);
}
