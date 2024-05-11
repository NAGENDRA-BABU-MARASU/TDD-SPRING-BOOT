package com.learnings.ticketapi.exception;

public class MissingResolutionSummaryException extends RuntimeException {
    public MissingResolutionSummaryException(String message) {
        super(message);
    }
}
