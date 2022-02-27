import org.example.models.factory.Factory;
import simudyne.nexus.Server;

public class Main {
  public static void main(String[] args) {
    Server.register("Factory Simulation", Factory.class);

    Server.run(args);

  }
}
