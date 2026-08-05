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
        // Call is blocking
        var response = paymentProcessorClient.initProcessPayment(paymentDetails);
        var paymentId = response.paymentId();
        // Assumes non-null response is success for simplicity
        if (paymentId != null) {
            paymentRepository.save(new Payment(paymentId, bookingId, PaymentStatus.IN_PROGRESS));
        }

        return new InitProcessPaymentResult(paymentId, response.failureReason());
    }

    /**
     * Typically polled by the client app to get payment status
     */
    public Payment findById(long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(String.format("Payment %s not found", paymentId)));
    }

    @Transactional
    public void paymentCompleted(Long paymentId, boolean success, String failureReason) {
        if (paymentId == null) {
            throw new BadRequestException("paymentId is null");
        }

        // Locking payment row
        var payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new NotFoundException(String.format("Payment %s not found", paymentId)));

        // Check if processed already
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
