package nl.asrr.microservice.webutils.exception.impl;

import lombok.Getter;
import nl.asrr.microservice.webutils.exception.ServiceException;

@Getter
public class InvalidStateException extends ServiceException {

    private final String property;

    private final String errorCode = "InvalidState";

    private final String errorMessage;

    public InvalidStateException(String property) {
        this.property = property;
        this.errorMessage = null;
    }

    public InvalidStateException(String property, String errorMessage) {
        this.property = property;
        this.errorMessage = errorMessage;
    }

}
