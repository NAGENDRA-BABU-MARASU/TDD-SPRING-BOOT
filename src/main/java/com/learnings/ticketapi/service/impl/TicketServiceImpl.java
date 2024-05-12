package com.learnings.ticketapi.service.impl;

import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.dto.TicketFilterDto;
import com.learnings.ticketapi.exception.MissingDescriptionException;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import com.learnings.ticketapi.repository.TicketRepository;
import com.learnings.ticketapi.service.TicketService;
import com.learnings.ticketapi.util.ErrorMessages;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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

        return new TicketDto(
                savedTicket.getId(),
                savedTicket.getDescription(),
                savedTicket.getStatus(),
                savedTicket.getCreatedTime(),
                null,
                null,
                null
        );
    }

    @Override
    public TicketDto assignAgentToTicket(Long ticketId, Long agentId) {
        return null;
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
