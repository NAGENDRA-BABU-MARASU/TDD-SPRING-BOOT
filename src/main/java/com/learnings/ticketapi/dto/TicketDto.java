package com.learnings.ticketapi.dto;

import com.learnings.ticketapi.model.Status;

import java.time.LocalDateTime;

public record TicketDto(Long id,
                        String description,
                        Status status,
                        LocalDateTime createdDate,
                        LocalDateTime closedDate,
                        String assignedAgent,
                        String resolutionSummary) {
}
