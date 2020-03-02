package nl.asrr.microservice.webutils.clr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Deprecated
public class CommandLineWorkerManager implements CommandLineRunner {

    private List<CommandLineWorker> commandLineWorkers;

    @Autowired
    public void setCommandLineWorkers(List<CommandLineWorker> commandLineWorkers) {
        this.commandLineWorkers = commandLineWorkers;
    }

    @Override
    public void run(String... args) {
        List<String> argumentList = Arrays.asList(args);
        commandLineWorkers.forEach(worker -> worker.run(argumentList));
    }

}
