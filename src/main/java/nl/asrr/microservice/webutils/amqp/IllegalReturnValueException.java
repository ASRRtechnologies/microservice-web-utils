package nl.asrr.microservice.webutils.amqp;

public class IllegalReturnValueException extends RuntimeException {

    public IllegalReturnValueException(String className) {
        super("return type should be "
                + FailableMessage.class.getSimpleName()
                + " and not " + className
        );
    }

}
