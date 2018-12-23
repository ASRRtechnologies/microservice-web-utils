package nl.asrr.microservice.webutils.exception;

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
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@ResponseBody
@ControllerAdvice
public class DefaultGlobalExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidInputException.class)
    public PropertyError invalidInputException(InvalidInputException e) {
        return PropertyError.of(e.getProperty(), e.getErrorCode());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public PropertyError notFoundException(NotFoundException e) {
        return PropertyError.of(e.getProperty(), "NotFound");
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConversionFailedException.class)
    public PropertyError conversionFailedException(ConversionFailedException e) {
        return PropertyError.of(e);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({HttpMessageNotReadableException.class, JsonParseException.class})
    public PropertyError jsonParseException() {
        return PropertyError.of("json", "Invalid");
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public PropertyError methodArgumentValidationException(MethodArgumentNotValidException e) {
        return PropertyError.of(e);
    }

    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public PropertyError httpMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return PropertyError.of(e);
    }

    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public PropertyError unsupportedMedia(HttpMediaTypeNotSupportedException e) {
        return PropertyError.of(e);
    }

}
