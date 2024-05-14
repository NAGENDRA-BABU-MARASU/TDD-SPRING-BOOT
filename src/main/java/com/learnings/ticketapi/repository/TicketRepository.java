package com.learnings.ticketapi.repository;

import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, TicketFilterRepository {

}
