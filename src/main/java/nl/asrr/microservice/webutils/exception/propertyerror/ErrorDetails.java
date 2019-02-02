package nl.asrr.microservice.webutils.exception.propertyerror;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the details of a property error.
 */
@Data
@Builder
public class ErrorDetails {

    /**
     * The error code of the error.
     */
    private String errorCode;

    /**
     * The message of the error.
     */
    private String errorMessage;

    /**
     * Extra details of the error.
     */
    private final Map<String, Object> details = new HashMap<>();

    /**
     * Adds an extra detail to the error.
     *
     * @param property the property of the detail to add
     * @param detail   the detail to add
     */
    public void addDetail(String property, Object detail) {
        details.put(property, detail);
    }

}
