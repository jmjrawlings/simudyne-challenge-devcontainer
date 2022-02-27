package org.example.models.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;

public class Machine extends Agent<Globals> {
    private static final Logger logger = LoggerFactory.getLogger("org.example.models.factory");
    /**
     * product currently processed at this machine. Null if none here
     */
    private Product currentProduct = null;
    
    @Constant
    public String name; // loaded from csv
    
    @Variable(name="Products done by machine")
    public int numProductsDone = 0;

    // ACTIONS

    /**
     * Initialize machine by filling its 1 slot with a product
     */
    public static  Action<Machine> initializeProduct() {
        return Action.create(Machine.class, currMachine  -> {
            double cycleTime_ticks = currMachine.getPrng().uniform(
                                        currMachine.getGlobals().cycleTimeMin_ticks,
                                        currMachine.getGlobals().cycleTimeMax_ticks).sample();
            currMachine.currentProduct = new Product(cycleTime_ticks);
            currMachine.currentProduct.startMachining(currMachine.getContext().getTick());
            //logger.info("Filled machine "+currMachine.getID()+" with 1 product: "+currMachine.currentProduct.toString());
        });
    }
    /**
     * Finish current product (if there is one and it spent enough time here) and try to pull next product from upstream conveyor
     */
    public static  Action<Machine> pushDownstreamAndFlagUpstream() {
        return Action.create(Machine.class, currMachine  -> {
            if (currMachine.currentProduct != null) { // actually has a product it works on
                if (currMachine.isProductFinished()) {
                    // send to downstream conveyor (if there is one)
                    if (currMachine.hasLinks(Links.Link_MachineToDownstreamConveyor.class)) {
                        // machine has downstream conveyor: send product there
                        currMachine.getLinks(Links.Link_MachineToDownstreamConveyor.class).
                                send(Messages.Msg_ProductForConveyor.class, (message, link) -> {
                                    message.product = currMachine.currentProduct;
                                });
                    } else { // this is the last machine, nothing downstream
                        // count global #products done
                        currMachine.getLongAccumulator("numProdsDone").add(1); // count globally
                    }
                    currMachine.currentProduct = null;
                    currMachine.numProductsDone++; // count locally
                    // flag upstream that you are empty now
                    if (currMachine.hasLinks(Links.Link_MachineToUpstreamConveyor.class)) {
                        currMachine.getLinks(Links.Link_MachineToUpstreamConveyor.class).
                                send(Messages.Msg_ReadyForProduct.class);
                    }
                } // else let machine continue for more ticks with current product
            } else{
                logger.debug("at tick "+ currMachine.getContext().getTick()+" found that machine "+currMachine.getID()+" has no prod");
            }

        });
    }

    /**
     * Add any products sent via message from upstream machine to your queue
     */
    public static Action<Machine> receiveProductForWork() {
        return Action.create(Machine.class, currMachine -> {
            // System.out.println("Machine "+currMachine.getID()+" starts receiveProductForWork on tick "+currMachine.getContext().getTick());
            if (currMachine.hasMessageOfType(Messages.Msg_ProductForMachine.class)) { // got a msg actually
                Product arrivingProduct = currMachine.getMessageOfType(Messages.Msg_ProductForMachine.class).product;
                currMachine.currentProduct = arrivingProduct;
                currMachine.currentProduct.startMachining(currMachine.getContext().getTick());
            }
        });
    }

    /**
     * @return true if this machine has a product and it has been processed as long as required for its cycle time. False otherwise
     */
    private boolean isProductFinished() {
        if (currentProduct != null) {
            long currTick = getContext().getTick();
            long startTick = currentProduct.startedAt_tick;
            long ticksSoFar = currTick - startTick;

            if (ticksSoFar >= currentProduct.cycleTime_ticks) {
                // logger.info("product done in machine "+getID()+" on tick "+getContext().getTick());
                return true;
            }
        }
        return false;
    }
}
