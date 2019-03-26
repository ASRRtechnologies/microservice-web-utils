package nl.asrr.microservice.webutils.amqp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FailableMessage<T> {

    private T message;

    private RuntimeException exception;

    public FailableMessage(T message) {
        this.message = message;
    }

}
