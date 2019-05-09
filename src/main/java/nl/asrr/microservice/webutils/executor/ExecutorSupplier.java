package nl.asrr.microservice.webutils.executor;

@FunctionalInterface
public interface ExecutorSupplier<T> {

    T get() throws Exception;

}
