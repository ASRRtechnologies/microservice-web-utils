package nl.asrr.microservice.webutils.exception;

import nl.asrr.microservice.webutils.exception.impl.DuplicateException;
import nl.asrr.microservice.webutils.exception.impl.InvalidInputException;
import nl.asrr.microservice.webutils.exception.impl.InvalidStateException;
import nl.asrr.microservice.webutils.exception.impl.JsonParseException;
import nl.asrr.microservice.webutils.exception.impl.NotFoundException;
import nl.asrr.microservice.webutils.exception.impl.ServerException;
import nl.asrr.microservice.webutils.exception.propertyerror.PropertyError;
import nl.asrr.microservice.webutils.exception.propertyerror.factory.FieldPropertyErrorFactory;
import nl.asrr.microservice.webutils.exception.propertyerror.factory.PropertyErrorFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@ResponseBody
@ControllerAdvice
public class DefaultGlobalExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidInputException.class)
    public PropertyError invalidInputException(InvalidInputException e) {
        return PropertyErrorFactory.of(e.getProperty(), e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidStateException.class)
    public PropertyError invalidStateException(InvalidStateException e) {
        return PropertyErrorFactory.of(e.getProperty(), e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public PropertyError notFoundException(NotFoundException e) {
        return PropertyErrorFactory.of(e.getProperty(), e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(DuplicateException.class)
    public PropertyError duplicateException(DuplicateException e) {
        return PropertyErrorFactory.of(e.getProperty(), e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConversionFailedException.class)
    public PropertyError conversionFailedException(ConversionFailedException e) {
        return PropertyErrorFactory.of(e);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({HttpMessageNotReadableException.class, JsonParseException.class})
    public PropertyError jsonParseException() {
        return PropertyErrorFactory.of(
                "json",
                "InvalidJson",
                "provided json is invalid"
        );
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public PropertyError methodArgumentValidationException(MethodArgumentNotValidException e) {
        return FieldPropertyErrorFactory.of(e);
    }

    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public PropertyError httpMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return PropertyErrorFactory.of(e);
    }

    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public PropertyError unsupportedMedia(HttpMediaTypeNotSupportedException e) {
        return PropertyErrorFactory.of(e);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServerException.class)
    public PropertyError unsupportedMedia(ServerException e) {
        return PropertyErrorFactory.of("server", "Server", e.getMessage());
    }

}
