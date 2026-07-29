package com.tskirbutas.jticket.eventservice;


import org.springframework.data.jpa.repository.JpaRepository;

interface EventRepository extends JpaRepository<Event,Long> {

}
