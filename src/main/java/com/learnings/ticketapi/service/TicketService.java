package com.learnings.ticketapi.service;

import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.dto.TicketFilterDto;

import java.util.List;

public interface TicketService {
     TicketDto createTicket(TicketDto ticketDto);

     TicketDto assignAgentToTicket(Long ticketId, Long agentId);

     TicketDto resolveTicket(Long ticketId);

     TicketDto closeTicket(Long ticketId);

     TicketDto updateTicket(Long ticketId, TicketDto ticketDto);

     TicketDto getTicketById(Long ticketId);

     List<TicketDto> getTickets(TicketFilterDto ticketFilterDto);
}
