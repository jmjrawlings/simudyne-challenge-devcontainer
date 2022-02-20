import org.example.models.factory.Factory;
import simudyne.core.exec.runner.LocalModelRunner;
import simudyne.core.exec.runner.ModelRunner;
import simudyne.core.exec.runner.RunnerBackend;
import simudyne.core.exec.runner.definition.BatchDefinitionsBuilder;
import simudyne.nexus.Server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main_headless {
    public static void main(String[] args) {
        try {
            RunnerBackend runnerBackend = RunnerBackend.create();
            ModelRunner modelRunner = runnerBackend.forModel(Factory.class);
            Map<String, Object> input = new HashMap<>();
            if (args.length > 0) {
                for (String s0 : args[0].split(",")) {
                    String[] s1 = s0.split("=");
                    if (s1.length != 2)
                        throw new IllegalArgumentException("Unknown input format, " + s0);

                    input.put(s1[0], s1[1]);
                }
            }
            long seed = ((LocalModelRunner) modelRunner).rootBuiltConfig().getIntOrElse("seed", 1234);
            int runs = ((LocalModelRunner) modelRunner).rootBuiltConfig().getIntOrElse("runs", 1);
            long ticks = ((LocalModelRunner) modelRunner).rootBuiltConfig().getIntOrElse("ticks", 60000);

            BatchDefinitionsBuilder runDefinitionBuilder =
                    BatchDefinitionsBuilder.create()
                            .forRuns(runs) // a required field, must be greater than 0.
                            .forTicks(ticks)// a required field, must be greater than 0.
                            .forSeeds(seed)
                            .withInputs(input);
            modelRunner.forRunDefinitionBuilder(runDefinitionBuilder);

            // To run the model and wait for it to complete
            modelRunner.run();

        } catch (RuntimeException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }
}
