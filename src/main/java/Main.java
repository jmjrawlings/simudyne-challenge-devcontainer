import org.example.models.factory.Factory;
import org.example.models.factory.Globals;
import simudyne.core.exec.runner.ModelRunner;
import simudyne.core.exec.runner.RunnerBackend;
import simudyne.core.exec.runner.definition.BatchDefinitionsBuilder;
import simudyne.nexus.Server;

import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    Server.register("Factory Simulation", Factory.class);

    Server.run(args);

  }
}
