package nl.asrr.microservice.webutils.amqp;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class FailableListenerAspect {

    @Around("@annotation(FailableListener)")
    public FailableMessage failableMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        FailableMessage<Object> message = new FailableMessage<>();
        try {
            Object returnValue = joinPoint.proceed();
            if (returnValue instanceof FailableMessage) {
                return (FailableMessage) returnValue;
            }
            throw new IllegalReturnValueException(returnValue.getClass().getName());
        } catch (RuntimeException e) {
            message.setException(e);
        }
        return message;
    }

}
