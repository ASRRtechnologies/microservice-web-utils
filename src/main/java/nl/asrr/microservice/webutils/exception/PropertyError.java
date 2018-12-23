package nl.asrr.microservice.webutils.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Data;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PropertyError {

    private static ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private final Map<String, String> propertyErrors;

    public String toPrettyJson() throws JsonProcessingException {
        return objectWriter.writeValueAsString(this);
    }

    public static PropertyError of(String property, String errorCode) {
        return new PropertyError(Collections.singletonMap(property, errorCode));
    }

    public static PropertyError of(ConversionFailedException e) {
        return PropertyError.of(
                "conversion", "cannot convert " + e.getValue()
        );
    }

    public static PropertyError of(HttpMediaTypeNotSupportedException e) {
        return PropertyError.of(
                "server",
                "Content type " + e.getContentType() + " is not supported"
        );
    }

    public static PropertyError of(HttpRequestMethodNotSupportedException e) {
        return PropertyError.of(
                "server",
                "HTTP method " + e.getMethod() + " is not supported"
        );
    }

    public static PropertyError of(MethodArgumentNotValidException e) {
        return PropertyError.of(e.getBindingResult().getFieldErrors());
    }

    public static PropertyError of(List<FieldError> fieldErrors) {
        Map<String, String> map = new HashMap<>();
        fieldErrors.forEach(f -> map.put(f.getField(), f.getCode()));
        return new PropertyError(map);
    }

}
