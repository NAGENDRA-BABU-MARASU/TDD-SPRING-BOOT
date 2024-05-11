package com.learnings.ticketapi.exception;

public class InvalidDateRangeException extends RuntimeException{
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
