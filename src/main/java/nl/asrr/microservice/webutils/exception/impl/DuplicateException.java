package nl.asrr.microservice.webutils.exception.impl;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {

    private final String property;

    private final String errorCode = "NotUnique";

    private final String errorMessage;

    public DuplicateException(String property) {
        this.property = property;
        this.errorMessage = String.format("%s is not unique", property);
    }

}
