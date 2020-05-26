package com.wp.casino.messageserver.event;


import java.util.EventObject;

public class EventObjectDemo extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EventObjectDemo(Object source) {
        super(source);
    }

}
