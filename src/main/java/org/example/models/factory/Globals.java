package org.example.models.factory;

import simudyne.core.abm.GlobalState;
import simudyne.core.annotations.Input;

public final class Globals extends GlobalState {
    // globals inputs and constants here

    @Input(name = "Initial products(per machine)")
    public int numInitialProducts = 1000;

    @Input(name = "Arrival rate (products/min)")
    public double rateNewProducts = 1000;

    @Input(name = "min cycle time (ms)")
    public double cycleTimeMin_ticks = 10; // mean CT is about 47ms

    @Input(name = "max cycle time (ms)")
    public double cycleTimeMax_ticks = 80; // mean CT is about 47ms

}
