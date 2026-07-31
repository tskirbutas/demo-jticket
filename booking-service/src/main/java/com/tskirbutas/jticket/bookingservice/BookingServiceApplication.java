package com.tskirbutas.jticket.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

//    @Bean
//    RestClient restClient(RestClient.Builder builder) {
//        //Is used only for the PaymentProcessorClientFake, so more robust configuration is skipped
//        return builder.build();
//    }
}
