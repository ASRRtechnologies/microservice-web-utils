package nl.asrr.microservice.webutils;

import lombok.extern.log4j.Log4j2;

import java.util.function.Supplier;

@Log4j2
public class GuaranteedExecutor {

    public static void execute(Runnable runnable) {
        do {
            try {
                runnable.run();
                return;
            } catch (Exception e) {
                log.error(e);
            }
        } while (true);
    }

    public static  <T> T execute(Supplier<T> supplier) {
        do {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error(e);
            }
        } while (true);
    }

}
