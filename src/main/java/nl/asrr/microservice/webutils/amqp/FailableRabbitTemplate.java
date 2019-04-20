package nl.asrr.microservice.webutils.amqp;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

@RequiredArgsConstructor
public class FailableRabbitTemplate {

    private final RabbitTemplate rabbitTemplate;

    public void send(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(routingKey, message);
    }

    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public <T> T sendAndReceiveAsType(String routingKey, Object message) {
        return rabbitTemplate.convertSendAndReceiveAsType(
                rabbitTemplate.getExchange(), routingKey, message, new ParameterizedTypeReference<T>() {
                });
    }

    public <T> T sendAndReceiveAsType(String exchange, String routingKey, Object message) {
        return rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                routingKey,
                message,
                new ParameterizedTypeReference<T>() {
                }
        );
    }

    public <T> T sendFailableAndReceiveAsType(String routingKey, Object message) {
        return sendFailableAndReceiveAsType(
                rabbitTemplate.getExchange(),
                routingKey,
                message
        );
    }

    public <T> T sendFailableAndReceiveAsType(String exchange, String routingKey, Object message) {
        FailableMessage<T> response = rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                routingKey,
                message,
                new ParameterizedTypeReference<FailableMessage<T>>() {
                }
        );

        if (response == null) {
            return null;
        }

        RuntimeException exception = response.getException();
        if (exception != null) {
            throw exception;
        }
        return response.getMessage();
    }

}
