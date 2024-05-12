package com.learnings.ticketapi.service.impl;

import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.dto.TicketFilterDto;
import com.learnings.ticketapi.exception.AgentNotFoundException;
import com.learnings.ticketapi.exception.InvalidTicketStateException;
import com.learnings.ticketapi.exception.MissingDescriptionException;
import com.learnings.ticketapi.exception.TicketNotFoundException;
import com.learnings.ticketapi.model.Agent;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import com.learnings.ticketapi.repository.AgentRepository;
import com.learnings.ticketapi.repository.TicketRepository;
import com.learnings.ticketapi.service.TicketService;
import com.learnings.ticketapi.util.ErrorMessages;
import org.springframework.jdbc.core.metadata.HsqlTableMetaDataProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, AgentRepository agentRepository) {
        this.ticketRepository = ticketRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        if(ticketDto.description() == null || ticketDto.description().isEmpty()){
            throw new MissingDescriptionException(ErrorMessages.DESCRIPTION_REQUIRED);
        }

        Ticket newTicket = new Ticket();
        newTicket.setDescription(ticketDto.description());
        newTicket.setStatus(Status.NEW);
        newTicket.setCreatedTime(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(newTicket);

        return convertToDto(savedTicket);
    }

    @Override
    public TicketDto assignAgentToTicket(Long ticketId, Long agentId) {
        Ticket existingTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        if(existingTicket.getStatus() != Status.NEW) {
            throw new InvalidTicketStateException(ErrorMessages.ONLY_NEW_TICKETS_CAN_BE_ASSIGNED_TO_AN_AGENT);
        }

        Agent assignedAgent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(ErrorMessages.AGENT_NOT_FOUND));

        existingTicket.setStatus(Status.IN_PROGRESS);
        existingTicket.setAssignedAgent(assignedAgent);

        Ticket savedTicket = ticketRepository.save(existingTicket);

        return convertToDto(savedTicket);
    }

    private TicketDto convertToDto(Ticket ticket) {
        return new TicketDto(
                ticket.getId(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedTime(),
                null,
                ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
                null
        );
    }

    @Override
    public TicketDto resolveTicket(Long ticketId) {
        return null;
    }

    @Override
    public TicketDto closeTicket(Long ticketId) {
        return null;
    }

    @Override
    public TicketDto updateTicket(Long ticketId, TicketDto ticketDto) {
        return null;
    }

    @Override
    public TicketDto getTicketById(Long ticketId) {
        return null;
    }

    @Override
    public List<TicketDto> getTickets(TicketFilterDto ticketFilterDto) {
        return List.of();
    }


}
