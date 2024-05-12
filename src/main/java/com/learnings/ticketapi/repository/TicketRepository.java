package com.learnings.ticketapi.repository;

import com.learnings.ticketapi.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
