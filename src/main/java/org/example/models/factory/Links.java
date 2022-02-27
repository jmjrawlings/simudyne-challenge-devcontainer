package org.example.models.factory;

import simudyne.core.graph.Link;

public class Links {
    // public static class NormalLink extends Link.Empty{}
    public static class Link_MachineToDownstreamConveyor extends Link.Empty{}
    public static class Link_ConveyorToDownstreamMachine extends Link.Empty{}
    public static class Link_MachineToUpstreamConveyor extends Link.Empty{}
    public static class Link_ConveyorToUpstreamMachine extends Link.Empty{}
}
