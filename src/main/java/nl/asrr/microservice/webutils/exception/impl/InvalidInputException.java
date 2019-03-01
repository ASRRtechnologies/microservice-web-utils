package nl.asrr.microservice.webutils.exception.impl;

import lombok.Data;

@Data
public class InvalidInputException extends RuntimeException {

    private final String property;

    private final String errorCode = "Invalid";

    private final String errorMessage;

    public InvalidInputException(String property) {
        this.property = property;
        this.errorMessage = String.format("input for %s is invalid", property);
    }

    public InvalidInputException(String property, String errorMessage) {
        this.property = property;
        this.errorMessage = errorMessage;
    }

}