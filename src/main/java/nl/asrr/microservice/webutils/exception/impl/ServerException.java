package nl.asrr.microservice.webutils.exception.impl;

import nl.asrr.microservice.webutils.exception.ServiceException;

public class ServerException extends ServiceException {

    public ServerException(String message) {
        super(message);
    }

}
