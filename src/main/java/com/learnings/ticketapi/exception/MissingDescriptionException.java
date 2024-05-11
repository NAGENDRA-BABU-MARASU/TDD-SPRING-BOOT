package com.learnings.ticketapi.exception;

public class MissingDescriptionException extends RuntimeException {
    public MissingDescriptionException(String message) {
        super(message);
    }
}
