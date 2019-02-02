package nl.asrr.microservice.webutils.exception.impl;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {

    private final String property;

    public DuplicateException(String property) {
        this.property = property;
    }

}
