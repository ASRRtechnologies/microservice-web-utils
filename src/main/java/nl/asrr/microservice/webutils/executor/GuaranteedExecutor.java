package nl.asrr.microservice.webutils.executor;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GuaranteedExecutor {

    public static void execute(ExecutorRunnable runnable) {
        do {
            try {
                runnable.run();
                return;
            } catch (Exception e) {
                log.error(e);
            }
        } while (true);
    }

    public static <T> T execute(ExecutorSupplier<T> supplier) {
        do {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error(e);
            }
        } while (true);
    }

}
