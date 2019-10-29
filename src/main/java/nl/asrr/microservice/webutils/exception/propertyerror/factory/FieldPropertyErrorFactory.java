package nl.asrr.microservice.webutils.exception.propertyerror.factory;

import nl.asrr.microservice.webutils.exception.propertyerror.ErrorDetails;
import nl.asrr.microservice.webutils.exception.propertyerror.PropertyError;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.HashMap;
import java.util.List;

public class FieldPropertyErrorFactory {

    public static PropertyError of(ConstraintViolationException e) {
        var propertyErrors = new HashMap<String, ErrorDetails>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            var property = getProperty(violation.getPropertyPath());
            if (property != null) {
                var details = ErrorDetails.builder()
                        .errorCode("Invalid")
                        .errorMessage(violation.getMessage())
                        .build();
                propertyErrors.put(property, details);
            }
        }
        return new PropertyError(propertyErrors);
    }

    /**
     * Gets the name of a property of a {@link Path}.
     *
     * @param propertyPath the {@link Path} to get the property name of
     * @return the name of the property
     */
    private static String getProperty(Path propertyPath) {
        if (propertyPath != null) {
            var propertyPathStr = propertyPath.toString();
            if (!propertyPathStr.isEmpty() && propertyPathStr.lastIndexOf(".") != -1) {
                return propertyPathStr.substring(propertyPathStr.lastIndexOf(".") + 1, propertyPath.toString().length());
            }
        }
        return null;
    }

    public static PropertyError of(MethodArgumentNotValidException e) {
        return FieldPropertyErrorFactory.of(e.getBindingResult().getFieldErrors());
    }

    public static PropertyError of(List<FieldError> fieldErrors) {
        var propertyErrors = new HashMap<String, ErrorDetails>();
        for (var fieldError : fieldErrors) {
            var property = fieldError.getField();
            var details = ErrorDetails.builder()
                    .errorCode(fieldError.getCode())
                    .errorMessage(fieldError.getDefaultMessage())
                    .build();

            populateErrorDetails(details, fieldError.getArguments());
            propertyErrors.put(property, details);
        }
        return new PropertyError(propertyErrors);
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
