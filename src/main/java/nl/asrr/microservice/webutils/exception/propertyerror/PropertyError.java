package nl.asrr.microservice.webutils.exception.propertyerror;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Data;

import java.util.Map;

/**
 * Stores {@link ErrorDetails} for every property that triggered an error.
 */
@Data
public class PropertyError {

    /**
     * {@link ObjectWriter} to convert a {@link PropertyError} to a JSON string.
     */
    private static ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    /**
     * A map to store the {@link ErrorDetails} for every property that triggered an error.
     */
    private final Map<String, ErrorDetails> propertyErrors;

    /**
     * Converts the current {@link PropertyError} to a pretty printed JSON string.
     *
     * @return the current {@link PropertyError} as a pretty printed JSON string
     * @throws JsonProcessingException if the {@link PropertyError} could not be converted to a
     *                                 pretty printed JSON string
     */
    public String toPrettyJson() throws JsonProcessingException {
        return objectWriter.writeValueAsString(this);
    }

}
