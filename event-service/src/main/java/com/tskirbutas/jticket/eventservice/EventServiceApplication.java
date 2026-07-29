package com.tskirbutas.jticket.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

//	@Bean
//	@Profile("dev")
//    CommandLineRunner seedDatabase(DataSource dataSource, EventRepository repo) {
//		if (repo.count() > 0) {
//			return args -> {};
//		}
//		return args -> {
//			ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
//					new ClassPathResource("db/seed/dev_data.sql")
//			);
//
//			populator.execute(dataSource);
//		};
//	}

}
