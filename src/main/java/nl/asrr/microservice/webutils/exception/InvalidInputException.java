package nl.asrr.microservice.webutils.exception;

import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {

    private final String property;

    private final String errorCode;

    public InvalidInputException(String property) {
        this.property = property;
        this.errorCode = "Invalid";
    }

    public InvalidInputException(String property, String errorCode) {
        this.property = property;
        this.errorCode = errorCode;
    }

}
