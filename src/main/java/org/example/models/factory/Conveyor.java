package org.example.models.factory;

import com.google.errorprone.annotations.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;

import javax.crypto.Mac;
import java.util.LinkedList;
import java.util.List;

public class Conveyor extends Agent<Globals> {
    private static final Logger logger = LoggerFactory.getLogger("org.example.models.factory");
    /**
     * FIFO queue of all products currently queuing in this conveyor
     */
    private LinkedList<Product> queue = new LinkedList<>();
    @Constant
    String name; // loaded from csv
    double speed_mperms = 0.002223; // meter per millisecond (i.e. m/tick)
    double length_m = 9000; // physical length of conveyor
    @Variable
    public int queueLength = 0; // log how many in queue currently, for UI outputs


    /**
     * Initialize conveyor queue with given number of products. Only call on first step
     *
     * @param numProducts how many products to put into conveyor
     */
    public static Action<Conveyor> initializeProducts(int numProducts) {
        return Action.create(Conveyor.class, currConveyor -> {
            for (int i = 0; i < numProducts; i++) {
                double cycleTime_ticks = currConveyor.getPrng().uniform(
                        currConveyor.getGlobals().cycleTimeMin_ticks,
                        currConveyor.getGlobals().cycleTimeMax_ticks).sample();
                // NOTE: Do not call enterQueue() as this would put products at the entry
                Product newProduct = new Product(cycleTime_ticks);
                currConveyor.queue.addLast(newProduct);
                currConveyor.queueLength++;
                // position them correctly
                newProduct.distanceToConveyorEnd_m = i * newProduct.width_m;
            }
            //logger.info("created "+numProducts+" in queue for conveyor "+currConveyor.getID()+": it has "+currConveyor.queue.size());
        });
    }

    /**
     * Load new products into conveyor queue
     * Call each tick to decide if new product(s) should enter the system
     * Only executed if this is the first conveyor of the system
     */
    public static Action<Conveyor> addNewProducts() {
        return Action.create(Conveyor.class, currConveyor -> {
            if (currConveyor.getID() == 0) { // only add at 1st conveyor
                double rateNewProductsPerMS = currConveyor.getGlobals().rateNewProducts / (60. * 1000.); // from "per min" to "per ms"
                int numNewProductsThisTick = (int) Math.floor(rateNewProductsPerMS);
                double remainder = (rateNewProductsPerMS - numNewProductsThisTick);
                if (remainder >= 1 || currConveyor.getPrng().uniform(0, 1).sample() < remainder) {
                    // add remainder only if prob checked (so "1.25" would be 1 each tick and 2 each 4 ticks)
                    numNewProductsThisTick++;
                    //logger.info("Conveyor "+currConveyor.getID()+" created "+numNewProductsThisTick+" new products on tick "+currConveyor.getContext().getTick());
                }
                for (int i = 0; i < numNewProductsThisTick; i++) {
                    double cycleTime_ticks = currConveyor.getPrng().uniform(
                            currConveyor.getGlobals().cycleTimeMin_ticks,
                            currConveyor.getGlobals().cycleTimeMax_ticks).sample();
                    currConveyor.enterQueue(new Product(cycleTime_ticks));
                }
            }
        });
    }

    /**
     * Add any products sent via message from upstream machine to your queue
     */
    public static Action<Conveyor> receiveProductAndPushOutOldest() {
        return Action.create(Conveyor.class, currConveyor -> {
            // System.out.println("Conveyor "+currConveyor.getID()+" starts receiveProductForQueue on tick "+currConveyor.getContext().getTick());
            // FIRST: Push out oldest product, if requested from downstream machine
            if (currConveyor.hasMessageOfType(Messages.Msg_ReadyForProduct.class)) { // got a msg actually
                // List<Messages.Msg_ReadyForProduct> lsit =  currConveyor.getMessagesOfType(Messages.Msg_ReadyForProduct.class);
                if (currConveyor.queue.size() > 0) { // got more
                    // oldest product is actually near the edge of the conveyor ready to leave
                    Product oldestProduct = currConveyor.queue.removeFirst();
                    currConveyor.queueLength--;
                    // send oldest product to downstream machine
                    currConveyor.getLinks(Links.Link_ConveyorToDownstreamMachine.class).
                            send(Messages.Msg_ProductForMachine.class, (message, link) -> {
                                message.product = oldestProduct;
                            });
                } else { // no more products to send, do nothing
                }

            }
            // add new product from upstream if upstream machine sent some
            if (currConveyor.hasMessageOfType(Messages.Msg_ProductForConveyor.class)) { // got a msg actually
                Product arrivingProduct = currConveyor.getMessageOfType(Messages.Msg_ProductForConveyor.class).product;
                currConveyor.enterQueue(arrivingProduct);
            }
        });
    }

    /**
     * move all products forward by the conveyor speed each tick
     * Products accumulate at the end of the conveyor if not pushed out fast enough
     * Products reaching the accumulation end only advance if conveyor pushed out some product
     */
    public static Action<Conveyor> advanceAllProducts() {
        return Action.create(Conveyor.class, currConveyor -> {
            int index = 0;
            Product previousProduct = null;
            for (Product currProd : currConveyor.queue) { // from oldest to newest (assumes you already pushed out any old products downstream)
                // move forward but do not fall conveyor edge or bump into preceeding product
                double distToMoveForward = currConveyor.getDistanceMovable(currProd, previousProduct);
                currProd.distanceToConveyorEnd_m -= distToMoveForward;
                previousProduct = currProd; // flag for next loops
            }
        });
    }


    // LOCAL FUNCTIONS
    private double getDistanceMovable(Product product, Product preceedingProduct) {
        if (preceedingProduct == null) { // is oldest product
            if (product.distanceToConveyorEnd_m < speed_mperms) { // would it move beyond edge now?
                return product.distanceToConveyorEnd_m; // can only move until edge of conveyor
            } else { // move forward by 1 tick's distance
                return speed_mperms;
            }
        } else { // got a preceeding product, check its position to not bump into it
            double edgePreceeding = preceedingProduct.distanceToConveyorEnd_m + preceedingProduct.width_m;
            double myNewPosAtFullSpeed = product.distanceToConveyorEnd_m - speed_mperms;
            if (myNewPosAtFullSpeed < edgePreceeding) { // Would bump into preceeding product now?
                double distToPreceeedingProd = product.distanceToConveyorEnd_m - edgePreceeding;
                return distToPreceeedingProd; // only move until preceeding prod edge
            } else { // move normally forward by 1 tick's distance
                return speed_mperms;
            }
        }
    }

    /**
     * Call when product enters conveyor at the far end
     *
     * @param product the product entering
     */
    private void enterQueue(Product product) {
        queue.addLast(product);
        queueLength++;
        product.distanceToConveyorEnd_m = length_m; // make product enter at the far end always
    }
}
