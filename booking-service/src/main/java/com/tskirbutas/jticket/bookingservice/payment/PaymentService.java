package com.tskirbutas.jticket.bookingservice.payment;

import com.tskirbutas.jticket.bookingservice.BadRequestException;
import com.tskirbutas.jticket.bookingservice.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    final PaymentProcessorClient paymentProcessorClient;
    final PaymentRepository paymentRepository;
    final ApplicationEventPublisher eventPublisher;

    PaymentService(PaymentProcessorClient paymentProcessorClient, PaymentRepository paymentRepository, ApplicationEventPublisher eventPublisher) {
        this.paymentProcessorClient = paymentProcessorClient;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    public InitProcessPaymentResult initPaymentProcessing(long bookingId, PaymentDetails paymentDetails) {
        var response = paymentProcessorClient.processPayment(paymentDetails);
        var paymentId = response.paymentId();
        if (paymentId != null) {
            paymentRepository.save(new Payment(paymentId, bookingId, PaymentStatus.IN_PROGRESS));
        }

        return new InitProcessPaymentResult(paymentId, response.failureReason());
    }

    @Transactional
    public void paymentCompleted(Long paymentId, boolean success, String failureReason) {
        if (paymentId == null) {
            throw new BadRequestException("paymentId is null");
        }

        var payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new NotFoundException(String.format("Payment %s not found", paymentId)));

        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            return;
        }

        if (success) {
            payment.successful();
        } else {
            payment.failed();
        }

        eventPublisher.publishEvent(new PaymentCompletedEvent(paymentId, payment.getBookingId(), success, failureReason));
    }
}
