package nl.asrr.microservice.webutils.exception.propertyerror.factory;

import nl.asrr.microservice.webutils.exception.propertyerror.ErrorDetails;
import nl.asrr.microservice.webutils.exception.propertyerror.PropertyError;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldPropertyErrorFactory {

    public static PropertyError of(MethodArgumentNotValidException e) {
        return FieldPropertyErrorFactory.of(e.getBindingResult().getFieldErrors());
    }

    public static PropertyError of(List<FieldError> fieldErrors) {
        Map<String, ErrorDetails> map = new HashMap<>();
        for (FieldError fieldError : fieldErrors) {
            ErrorDetails details = ErrorDetails.builder()
                    .errorCode(fieldError.getCode())
                    .errorMessage(fieldError.getDefaultMessage())
                    .build();
            populateErrorDetails(details, fieldError.getArguments());
            map.put(fieldError.getField(), details);
        }
        return new PropertyError(map);
    }

    private static void populateErrorDetails(ErrorDetails details, Object[] errorArguments) {
        if (errorArguments == null) {
            return;
        }

        switch (details.getErrorCode()) {
            case "Size":
                if (errorArguments.length >= 3) {
                    details.addDetail("min", errorArguments[2]);
                    details.addDetail("max", errorArguments[1]);
                }
                break;
        }
    }

}
