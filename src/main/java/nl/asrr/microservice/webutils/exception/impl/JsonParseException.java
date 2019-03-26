package nl.asrr.microservice.webutils.exception.impl;

import nl.asrr.microservice.webutils.exception.ServiceException;

public class JsonParseException extends ServiceException {

    public JsonParseException(String message) {
        super(message);
    }

}
