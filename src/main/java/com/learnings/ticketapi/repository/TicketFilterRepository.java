package com.learnings.ticketapi.repository;

import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketFilterRepository {
    List<Ticket> findWithFilters(
            List<Status> statuses,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String assignedAgent
    );
}
