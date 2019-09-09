package nl.asrr.microservice.webutils.amqp;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

public class MessageQueueConfig {

    private final AmqpAdmin mq;

    public MessageQueueConfig(AmqpAdmin mq) {
        this.mq = mq;
    }

    public AmqpAdmin mq() {
        return mq;
    }

    public void bindQueue(TopicExchange exchange, String queueName) {
        bindQueue(exchange, new Queue(queueName));
    }

    public void bindQueue(TopicExchange exchange, Queue queue) {
        var routingKey = getRoutingKey(queue.getName());

        mq.declareQueue(queue);
        mq.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey));
    }

    private String getRoutingKey(String queue) {
        return queue.replaceAll("-", ".");
    }

}
