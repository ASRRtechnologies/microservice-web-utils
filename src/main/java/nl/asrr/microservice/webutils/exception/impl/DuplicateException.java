package nl.asrr.microservice.webutils.exception.impl;

import lombok.Getter;
import nl.asrr.microservice.webutils.exception.ServiceException;

@Getter
public class DuplicateException extends ServiceException {

    private final String property;

    private final String errorCode = "NotUnique";

    private final String errorMessage;

    public DuplicateException(String property) {
        this.property = property;
        this.errorMessage = String.format("%s is not unique", property);
    }

}
