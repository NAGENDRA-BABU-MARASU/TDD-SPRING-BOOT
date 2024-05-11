package com.learnings.ticketapi.util;

public class ErrorMessages {
    public static final String AGENT_NOT_FOUND = "Agent not found.";
    public static final String TICKET_NOT_FOUND = "Ticket not found.";
    public static final String RESOLUTION_SUMMARY_REQUIRED = "The Resolution summary is required to close a ticket.";
    public static final String ONLY_TICKETS_IN_PROGRESS_CAN_BE_RESOLVED = "Only tickets IN PROGRESS can be resolved.";
    public static final String CLOSED_TICKETS_CANNOT_BE_UPDATED = "Closed tickets cannot be updated";
    public static final String ONLY_RESOLVED_TICKETS_CAN_BE_CLOSED = "Only resolved tickets can be closed";
    public static final String INVALID_DATE_RANGE = "Invalid date range";
    public static final String DESCRIPTION_REQUIRED = "Description is required to create a ticket.";

    private ErrorMessages() {}
    public static final String ONLY_NEW_TICKETS_CAN_BE_ASSIGNED_TO_AN_AGENT = "Only NEW tickets can be assigned to an agent.";
}
