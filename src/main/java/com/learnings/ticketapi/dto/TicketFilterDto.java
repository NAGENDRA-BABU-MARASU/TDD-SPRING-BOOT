package com.learnings.ticketapi.dto;

import com.learnings.ticketapi.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record TicketFilterDto(List<Status> status,
                                LocalDateTime startDate,
                                LocalDateTime endDate,
                                String assignedAgent) {
}
