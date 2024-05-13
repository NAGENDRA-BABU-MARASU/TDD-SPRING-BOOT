package com.learnings.ticketapi.service;

import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.exception.*;
import com.learnings.ticketapi.model.Agent;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import com.learnings.ticketapi.repository.AgentRepository;
import com.learnings.ticketapi.repository.TicketRepository;
import com.learnings.ticketapi.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AgentRepository agentRepository;

    @BeforeEach
    void setup() {
        ticketService = new TicketServiceImpl(ticketRepository, agentRepository);
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

    @Test
    void givenNewTicket_whenAssigningAgent_thenStatusIsInProgress() {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());

        Agent agent = new Agent(agentId, "Agent001");
        Ticket savedTicket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        savedTicket.setAssignedAgent(agent);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto updatedTicket = ticketService.assignAgentToTicket(ticketId, agentId);

        assertEquals(ticketId, updatedTicket.id());
        assertEquals(agentId, agent.getId());
        assertEquals(Status.IN_PROGRESS, updatedTicket.status());
    }

    @Test
    void givenNonExistentTicket_whenAssigningAgent_thenThrowException() throws Exception {
        Long nonExistentTicketId = 999L;
        Long agentId = 1L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.assignAgentToTicket(nonExistentTicketId, agentId));
    }

    @Test
    void givenNonExistentAgent_whenAssigningToTicket_thenThrowException() throws Exception {
        Long ticketId = 999L;
        Long nonExistentAgentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description,Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(agentRepository.findById(nonExistentAgentId)).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class,
                () -> ticketService.assignAgentToTicket(ticketId, nonExistentAgentId));
    }

    @Test
    void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.assignAgentToTicket(ticketId, agentId));
    }

    @Test
    void givenTicketInProgress_whenResolving_thenStatusIsResolved() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        Ticket savedTicket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto updatedTicket = ticketService.resolveTicket(ticketId);

        assertEquals(Status.RESOLVED, updatedTicket.status());
    }

    @Test
    void givenNonExistentTicket_whenResolving_thenThrowException() {
        Long nonExistentTicketId = 999L;

        when(ticketRepository.findById(nonExistentTicketId))
                .thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.resolveTicket(nonExistentTicketId));
    }

    @Test
    void givenTicketNotInProgressState_whenResolving_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.resolveTicket(ticketId));
    }

    @Test
    void givenResolvedWithSummary_whenClosing_thenStatusIsClosed() {
        Long ticketId = 1L;
        String description = "description";
        String resolutionSummary = "Summary.";
        Ticket ticket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);

        Ticket savedTicket = new Ticket(ticketId, description, Status.CLOSED, LocalDateTime.now());
        savedTicket.setResolutionSummary(resolutionSummary);
        savedTicket.setClosedDate(LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto updatedTicket = ticketService.closeTicket(ticketId);

        assertEquals(Status.CLOSED, updatedTicket.status());
        assertNotNull(updatedTicket.closedDate());
    }

    @Test
    void givenNonExistentTicket_whenClosing_thenThrowException() {
        Long nonExistentTicketId = 999L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.closeTicket(nonExistentTicketId));
    }

    @Test
    void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(MissingResolutionSummaryException.class, () -> ticketService.closeTicket(ticketId));
    }

    @Test
    void givenTicketNotInResolvedState_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        String resolutionSummary = "summary.";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.closeTicket(ticketId));
    }

    @Test
    void givenTicketDescriptionAndResolutionSummary_whenUpdating_thenDescriptionAndResolutionSummaryAreUpdated() {
        Long ticketId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TicketDto ticketDto = new TicketDto(ticketId, "description", Status.RESOLVED, LocalDateTime.now(), null, null, "resolutionSummary");
        Ticket originalTicket = new Ticket(ticketId, ticketDto.description(), Status.RESOLVED, LocalDateTime.now());
        originalTicket.setResolutionSummary(ticketDto.resolutionSummary());

        Ticket updatedTicketFromRepo = new Ticket(ticketId, "updated description", Status.RESOLVED, LocalDateTime.now());
        updatedTicketFromRepo.setResolutionSummary("updated resolution summary");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(originalTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(updatedTicketFromRepo);

        TicketDto updatedTicket = ticketService.updateTicket(ticketId, ticketDto);

        assertEquals(updatedTicketFromRepo.getDescription(), updatedTicket.description());
        assertEquals(updatedTicketFromRepo.getResolutionSummary(), updatedTicket.resolutionSummary());
    }

    @Test
    void givenNonExistentTicket_whenUpdating_thenThrowException() {
        Long nonExistentTicketId = 999L;
        String description = "description";
        String resolutionSummary = "summary";
        TicketDto ticketDto = new TicketDto(nonExistentTicketId, description, Status.RESOLVED, LocalDateTime.now(), null, null, resolutionSummary);

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.updateTicket(nonExistentTicketId, ticketDto));
    }

    @Test
    void givenClosedTicket_whenUpdating_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        String resolutionSummary = "summary.";
        TicketDto ticketDto = new TicketDto(ticketId, description, Status.CLOSED, LocalDateTime.now(), null, null, resolutionSummary);
        Ticket ticket = new Ticket(ticketId, description, Status.CLOSED, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.updateTicket(ticketId, ticketDto));

    }

}
