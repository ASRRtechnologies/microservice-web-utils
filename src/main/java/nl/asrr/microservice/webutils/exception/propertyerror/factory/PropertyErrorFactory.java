package nl.asrr.microservice.webutils.exception.propertyerror.factory;

import nl.asrr.microservice.webutils.exception.propertyerror.ErrorDetails;
import nl.asrr.microservice.webutils.exception.propertyerror.PropertyError;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Collections;

public class PropertyErrorFactory {

    public static PropertyError of(String property, String errorCode, String errorMessage) {
        ErrorDetails details = ErrorDetails.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
        return new PropertyError(Collections.singletonMap(property, details));
    }

    public static PropertyError of(HttpMediaTypeNotSupportedException e) {
        String errorMessage = String.format("content type %s is not supported", e.getContentType());
        return PropertyErrorFactory.of(
                "httpMediaType",
                "HttpMediaTypeNotSupported",
                errorMessage
        );
    }

    public static PropertyError of(HttpRequestMethodNotSupportedException e) {
        String errorMessage = String.format("HTTP method %s is not supported", e.getMethod());
        return PropertyErrorFactory.of(
                "httpRequestMethod",
                "HttpRequestMethodNotSupported",
                errorMessage
        );
    }

    public static PropertyError of(ConversionFailedException e) {
        String errorMessage = String.format(
                "cannot convert \"%s\" to %s",
                e.getValue(),
                e.getTargetType().getType().getSimpleName());
        return PropertyErrorFactory.of(
                "unknown", "Conversion", errorMessage
        );
    }

}
