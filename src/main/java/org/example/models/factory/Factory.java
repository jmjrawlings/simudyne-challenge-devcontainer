package org.example.models.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.Group;
import simudyne.core.abm.Split;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.data.CSVSource;

import java.io.File;

@ModelSettings(timeUnit = "MILLIS")
public class Factory extends AgentBasedModel<Globals> {
    private static final Logger logger = LoggerFactory.getLogger("factory");
    
    public long startTime;
    public long runStartTime;

    @Override
    public void init() {
         startTime = System.currentTimeMillis();
        // create global outputs
        createLongAccumulator("numProdsDone", "Products exited system");

        // load all agents
        registerAgentTypes(Conveyor.class, Machine.class);
        // load all links
        registerLinkTypes(Links.Link_MachineToDownstreamConveyor.class,
                Links.Link_MachineToUpstreamConveyor.class,
                Links.Link_ConveyorToDownstreamMachine.class,
                Links.Link_ConveyorToUpstreamMachine.class);
    }

    @Override
    public void setup() {
        // create agents: do conveyors first so their Simudyne IDs are same as in csv files
        CSVSource conveyorSource = new CSVSource("data" + File.separator + "conveyors.csv");
        Group<Conveyor> conveyors = loadGroup(Conveyor.class, conveyorSource);
        CSVSource machinesSource = new CSVSource("data" + File.separator + "machines.csv");
        Group<Machine> machines = loadGroup(Machine.class, machinesSource);

        // load link data
        CSVSource source_MachineToDownstreamConveyors = new CSVSource("data" + File.separator + "links_machines_to_downstream_conveyors.csv");
        CSVSource source_MachineToUpstreamConveyors = new CSVSource("data" + File.separator + "links_machines_to_upstream_conveyors.csv");
        CSVSource source_ConveyorToDownstreamMachines = new CSVSource("data" + File.separator + "links_conveyors_to_downstream_machines.csv");
        CSVSource source_ConveyorToUpstreamMachines = new CSVSource("data" + File.separator + "links_conveyors_to_upstream_machines.csv");
        // link machines
        machines.loadConnections(conveyors, Links.Link_MachineToDownstreamConveyor.class, source_MachineToDownstreamConveyors);
        machines.loadConnections(conveyors, Links.Link_MachineToUpstreamConveyor.class, source_MachineToUpstreamConveyors);
        conveyors.loadConnections(machines, Links.Link_ConveyorToDownstreamMachine.class, source_ConveyorToDownstreamMachines);
        conveyors.loadConnections(machines, Links.Link_ConveyorToUpstreamMachine.class, source_ConveyorToUpstreamMachines);

        // MUST BE LAST
        super.setup(); // final Simudyne setup
    }

    @Override
    public void step() { // Each step is 1ms
        super.step(); // FIRST: do Simudyne stepping
        // seed model with initial products (only on first step)

        long curTick = getContext().getTick();

        if (curTick==0)
        {
            runStartTime = System.currentTimeMillis();
        }

        firstStep(
                Split.create(
                        Machine.initializeProduct(),
                        Conveyor.initializeProducts(getGlobals().numInitialProducts)
                )
        );


        if (getContext().getTick() % 1000 == 0) {
            logger.info("curr tick " + getContext().getTick());
        }
        // sequence is crucial here, consider Splits
        run(
                // 1. machine finishes product -> push downstream AND flag upstream that you have space (must be in 1 func)
                Machine.pushDownstreamAndFlagUpstream(),

                // 2. conveyors: get products from upstream machine. Push out oldest if downstream is free (must be in 1 func)
                Conveyor.receiveProductAndPushOutOldest(),
                // 3. machine receives product from upstream for next tick
                Split.create(Machine.receiveProductForWork(),
                Conveyor.advanceAllProducts()),
                Conveyor.addNewProducts()
        );

        if (curTick == Math.ceil(9999/getGlobals().discreteStep)) {
                logger.info("Total Run time = " + (System.currentTimeMillis() - runStartTime));
        }
    }

    @Override
    public void done() {
        super.done();
        long endTime = System.currentTimeMillis();
        System.out.println("time elapsed = " + (endTime - startTime));
        logger.info("finished");
    }

}
