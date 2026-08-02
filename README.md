# JTicket - purchase tickets to your favorite events!

## Overview
This is a personal project which simulates an online ticket booking platform. 

The goal of this project is to practice and demonstrate common backend engineering concepts used in modern Java applications, including:

- Spring Boot
- REST APIs
- PostgreSQL
- Database migrations with Flyway
- JPA/Hibernate
- Transactions and concurrency handling
- Kafka-based asynchronous communication
- Integration testing with Testcontainers
- Docker-based local development

## High level design diagram
![Architecture](jticket-4.jpg)

## Modules

### Event Service

Responsible for retrieving events.

Technologies:

- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway

---

### Booking Service

Responsible for the booking lifecycle.

Responsibilities:

- Creating bookings
- Reserving tickets
- Handling payment flow via 3rd party payment processor (simulated)
- Expiring unfinished reservations
- Ensuring concurrent booking safety

Technologies:

- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Kafka

---

### Email Service

Responsible for asynchronous email processing.

The service consumes Kafka events produced by the Booking Service.

Responsibilities:

- Listening for successful payment events
- Sending booking confirmation emails (simulated)

Technologies:

- Spring Boot
- Kafka

---

### JTicket core

Simple Java library that currently act as a single source of truth for kafka related messaging:

- Topic ids
- Payload definitions

---

## Postgres containers

Currently, there's only one postgres container which creates two databases:

```
CREATE DATABASE jticket_eventdb;
CREATE DATABASE jticket_bookingdb;
```

It's only a matter of config to actually use separate containers for each as the code was written with such intention in mind.


## Testing

The project uses integration tests to verify the complete application flow.

Tests cover scenarios such as:

- Creating bookings
- Concurrent booking
- Payment confirmation
- Kafka event processing

Tests use:

- JUnit
- Spring Boot Test
- Testcontainers

## What's missing / Future improvements

The following features are intentionally not implemented yet:

### API Gateway

Potential improvements:

- Centralized routing
- Authentication and authorization
- Rate limiting
- Request filtering

### Event and ticket admin console 

Currently, it is assumed that an admin will insert those directly via sql

### Search

Elasticsearch-based event search

### Observability

Potential improvements:

- Centralized logging
- Metrics
- Distributed tracing
- Monitoring dashboards

### Deployment

Potential improvements:

- Kubernetes deployment
- CI/CD pipeline
- Cloud deployment

## Requirements

Required:

- Docker
- Docker Compose

Optional for development:

- Java 21+

## Running

Clone the repository:

```bash
git clone https://github.com/tskirbutas/demo-jticket
cd demo-jticket
```

The project is currently primarily developed using IntelliJ IDEA with supporting infrastructure (PostgreSQL, Kafka) running through Docker Compose.
```bash
docker compose up
```

A full Docker Compose environment is planned as a future improvement.

## Configuration

Configuration is handled through Spring properties, profiles and environment variables.
TODO: expand...
