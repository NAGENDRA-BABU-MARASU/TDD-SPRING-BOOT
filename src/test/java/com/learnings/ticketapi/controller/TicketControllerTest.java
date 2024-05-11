package com.learnings.ticketapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnings.ticketapi.dto.TicketDto;
import com.learnings.ticketapi.dto.TicketFilterDto;
import com.learnings.ticketapi.exception.*;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.service.TicketService;
import com.learnings.ticketapi.util.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenTicketIsSaved() throws Exception {
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(null, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.createTicket(any(TicketDto.class))).thenReturn(ticketDto);

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is(ticketDescription)))
                .andExpect(jsonPath("$.status", is(Status.NEW.name())))
        ;
    }

    @Test
    void givenNewTicket_whenAssigningAgent_thenStatusIsInProgress() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Ticket Description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.IN_PROGRESS, null, null, agentName, null);

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/agent/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.IN_PROGRESS.name())))
                .andExpect(jsonPath("$.assignedAgent", is(agentName)));
    }

    @Test
    public void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenThrow(new InvalidTicketStateException(ErrorMessages.ONLY_NEW_TICKETS_CAN_BE_ASSIGNED_TO_AN_AGENT));

        mockMvc.perform(put("/tickets/{id}/agent/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())

                .andExpect(content().string(ErrorMessages.ONLY_NEW_TICKETS_CAN_BE_ASSIGNED_TO_AN_AGENT));
    }

    @Test
    public void givenNonExistingAgent_whenAssigningAgent_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long nonExistentAgentId = 999L;

        when(ticketService.assignAgentToTicket(ticketId, nonExistentAgentId)).thenThrow(
                new AgentNotFoundException(ErrorMessages.AGENT_NOT_FOUND)
        );

        mockMvc.perform(put("/tickets/{id}/agent/{agentId}", ticketId, nonExistentAgentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.AGENT_NOT_FOUND));
    }

    @Test
    public void givenNonExistentTicket_whenAssigningTicket_thenThrowException() throws Exception {
        Long nonExistingTicketId = 999L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(nonExistingTicketId, agentId)).thenThrow(
                new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND)
        );

        mockMvc.perform(put("/tickets/{id}/agent/{agentId}", nonExistingTicketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    void givenNewTicketInProgress_whenResolving_thenStatusIsResolved() throws Exception {
        Long ticketId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Description";
        TicketDto ticketDto = new TicketDto(ticketId,
                ticketDescription,
                Status.RESOLVED,
                null,
                null,
                agentName,
                null);

        when(ticketService.resolveTicket(ticketId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/resolve", ticketId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.RESOLVED.name())));
    }

    @Test
    void givenResolvedTicketWithSummary_whenClosing_thenStatusIsClosed() throws Exception {
        Long ticketId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Description";
        TicketDto ticketDto = new TicketDto(ticketId,
                ticketDescription,
                Status.CLOSED,
                null,
                null,
                agentName,
                "Issue resolved.");

        when(ticketService.closeTicket(ticketId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.CLOSED.name())));
    }

    @Test
    public void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() throws Exception {
        Long ticketId = 1L;
        when(ticketService.closeTicket(ticketId)).thenThrow(
                new MissingResolutionSummaryException(ErrorMessages.RESOLUTION_SUMMARY_REQUIRED)
        );

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.RESOLUTION_SUMMARY_REQUIRED));
    }

    @Test
    public void givenClosedTicket_whenResolving_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.resolveTicket(ticketId)).thenThrow(
                new InvalidTicketStateException(ErrorMessages.ONLY_TICKETS_IN_PROGRESS_CAN_BE_RESOLVED)
        );

        mockMvc.perform(put("/tickets/{id}/resolve", ticketId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_TICKETS_IN_PROGRESS_CAN_BE_RESOLVED));
    }

    @Test
    void givenTicketDetails_whenTicketIsUpdated_thenDetailsAreUpdated() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.updateTicket(eq(ticketId), any(TicketDto.class))).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(ticketDescription)));
        ;
    }

    @Test
    public void givenClosedTicket_whenUpdating_thenThrowException() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Updated ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.CLOSED, null, null, null, null);

        when(ticketService.updateTicket(eq(ticketId), any(TicketDto.class)))
                .thenThrow(new InvalidTicketStateException(ErrorMessages.CLOSED_TICKETS_CANNOT_BE_UPDATED));

        mockMvc.perform(put("/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.CLOSED_TICKETS_CANNOT_BE_UPDATED));
    }

    @Test
    void givenValidTicketId_whenGettingTicket_thenReturnTicketDetails() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(
                ticketId,
                ticketDescription,
                Status.NEW,
                null,
                null,
                null,
                null
        );

        when(ticketService.getTicketById(eq(ticketId))).thenReturn(ticketDto);

        mockMvc.perform(get("/tickets/{id}", ticketId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())));
    }

    @Test
    void givenFilterCriteria_whenGettingTickets_thenReturnFilteredTickets() throws Exception {
        String agentName = "Agent001";
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto1 = new TicketDto(
                1L,
                ticketDescription,
                Status.NEW,
                LocalDateTime.now(),
                null,
                agentName,
                null
        );
        TicketDto ticketDto2 = new TicketDto(
                2L,
                ticketDescription,
                Status.NEW,
                LocalDateTime.now().minusDays(2),
                null,
                agentName,
                null
        );

        List<TicketDto> filteredTickets = List.of(ticketDto1, ticketDto2);

        when(ticketService.getTickets(any(TicketFilterDto.class))).thenReturn(filteredTickets);

        mockMvc.perform(get("/tickets")
                        .param("status", "NEW,IN_PROGRESS")
                        .param("startDate", LocalDateTime.now().minusDays(3).toString())
                        .param("endDate", LocalDateTime.now().toString())
                        .param("assignedAgent", agentName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(filteredTickets.size())))
                .andExpect(jsonPath("$[0].id", is(ticketDto1.id().intValue())))
                .andExpect(jsonPath("$[1].id", is(ticketDto2.id().intValue())));
    }

    @Test
    @DisplayName("Given a non-existent ticket, when resolving the ticket, then a TicketNotFoundException is thrown")
    void givenNonExistingTicket_whenResolving_thenThrowException() throws Exception {
        Long nonExistentTicketId = 999L;

        when(ticketService.resolveTicket(nonExistentTicketId)).thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/resolve", nonExistentTicketId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a non-existent ticket, when closing the ticket, then a TicketNotFoundException is thrown")
    void givenNonExistingTicket_whenClosing_thenThrowException() throws Exception {
        Long nonExistentTicketId = 999L;

        when(ticketService.closeTicket(nonExistentTicketId)).thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/close", nonExistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a ticket not in resolved state, when closing the ticket, then an InvalidStateException is thrown")
    void givenTicketNotInResolvedState_whenClosing_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.closeTicket(ticketId)).thenThrow(
                new InvalidTicketStateException(ErrorMessages.ONLY_RESOLVED_TICKETS_CAN_BE_CLOSED)
        );

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_RESOLVED_TICKETS_CAN_BE_CLOSED));
    }

    @Test
    @DisplayName("Given a non-existing ticket, when updating the ticket, then a TicketNotFoundException is thrown")
    void givenNonExistingTicket_whenUpdating_thenThrowException() throws Exception {
        Long nonExistentTicketId = 999L;
        String ticketDescription = "Updated ticket description";
        TicketDto ticketDto = new TicketDto(
                nonExistentTicketId,
                ticketDescription,
                Status.NEW,
                null,
                null,
                null,
                null
        );

        when(ticketService.updateTicket(nonExistentTicketId, ticketDto)).thenThrow(
                new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND)
        );

        mockMvc.perform(put("/tickets/{id}", nonExistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a non-existing ticket ID, when getting the ticket, then a TicketNotFoundException is thrown")
    void givenNonExistingTicket_whenGettingTicket_thenThrowException() throws Exception {
        Long nonExistentTicketId = 999L;

        when(ticketService.getTicketById(nonExistentTicketId)).thenThrow(
                new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND)
        );

        mockMvc.perform(get("/tickets/{id}", nonExistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given an invalid date range, when getting tickets, then an InvalidDateRangeException is thrown")
    void givenInvalidDateRange_whenGettingTickets_thenThrowException() throws Exception {
        when(ticketService.getTickets(any(TicketFilterDto.class)))
                .thenThrow(new InvalidDateRangeException(ErrorMessages.INVALID_DATE_RANGE));

        mockMvc.perform(get("/tickets")
                .param("startDate", LocalDateTime.now().toString())
                .param("endDate", LocalDateTime.now().minusDays(3).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.INVALID_DATE_RANGE));
    }

    @Test
    @DisplayName("Given a ticket without a description, when a new ticket is created, then a MissingDescriptionException is thrown")
    void givenTicketWithoutDescription_whenTicketIsCreated_thenThrowException() throws Exception {
        TicketDto ticketDto = new TicketDto(null, null, Status.NEW, null, null, null, null);

        when(ticketService.createTicket(any(TicketDto.class))).thenThrow(
                new MissingDescriptionException(ErrorMessages.DESCRIPTION_REQUIRED)
        );

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.DESCRIPTION_REQUIRED));
        ;
    }

}
