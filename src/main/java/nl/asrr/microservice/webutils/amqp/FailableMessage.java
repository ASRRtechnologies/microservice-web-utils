package nl.asrr.microservice.webutils.amqp;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FailableMessage<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private T message;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private RuntimeException exception;

    public FailableMessage(T message) {
        this.message = message;
    }

}
