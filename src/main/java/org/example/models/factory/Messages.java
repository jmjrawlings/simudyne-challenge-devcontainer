package org.example.models.factory;

import simudyne.core.graph.Message;

public class Messages {
    public static class Msg_ReadyForProduct extends Message {
        // sent by machine when it has an empty slot and tells upstream conveyor about it
    }
    public static class Msg_ProductForConveyor extends Message {
        public Product product;
    }
    public static class Msg_ProductForMachine extends Message {
        public Product product;
    }
}
