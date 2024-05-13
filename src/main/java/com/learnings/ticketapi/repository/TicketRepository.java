package com.learnings.ticketapi.repository;

import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findWithFilters(
            List<Status> statuses,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String assignedAgent
    );

}
