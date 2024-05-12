package com.learnings.ticketapi.service;

import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.exception.MissingDescriptionException;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import com.learnings.ticketapi.repository.TicketRepository;
import com.learnings.ticketapi.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketService = new TicketServiceImpl(ticketRepository);
    }

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenCallsRepositorySave() {
        TicketDto ticketDto = new TicketDto(null, "description", null, null, null ,null, null);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(new Ticket());

        ticketService.createTicket(ticketDto);

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenSetsNewStatusAndCreationDate() {
        String ticketDescription = "description";
        TicketDto ticketDto = new TicketDto(null, ticketDescription, null, null, null ,null, null);
        Ticket savedTicket = new Ticket(1L, ticketDescription, Status.NEW, LocalDateTime.now());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto createdTicket = ticketService.createTicket(ticketDto);

        assertNotNull(createdTicket);
        assertEquals(Status.NEW , createdTicket.status());
        assertNotNull(createdTicket.createdDate());
    }

    @Test
    void givenTicketWithoutDescription_whenTicketIsCreated_thenThrowException() {
        TicketDto ticketDto = new TicketDto(null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(MissingDescriptionException.class, () -> ticketService.createTicket(ticketDto));
    }
}
