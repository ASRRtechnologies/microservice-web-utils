package nl.asrr.microservice.webutils.exception.impl;

import lombok.Data;

@Data
public class InvalidInputException extends RuntimeException {

    private final String property;

    private final String errorCode;

    private final String errorMessage;

}
