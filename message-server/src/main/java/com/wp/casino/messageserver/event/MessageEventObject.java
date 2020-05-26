package com.wp.casino.messageserver.event;

import java.util.EventObject;

/**
 * @author sn
 * @date 2020/5/26 11:21
 */
public class MessageEventObject extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public MessageEventObject(Object source) {
        super(source);
    }
}
