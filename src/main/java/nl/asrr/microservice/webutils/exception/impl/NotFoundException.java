package nl.asrr.microservice.webutils.exception.impl;

import lombok.Getter;
import nl.asrr.microservice.webutils.exception.ServiceException;

@Getter
public class NotFoundException extends ServiceException {

    private final String property;

    private final String errorCode = "NotFound";

    private final String errorMessage;

    public NotFoundException(String property) {
        this.property = property;
        this.errorMessage = String.format("%s has not been found", property);
    }

}
