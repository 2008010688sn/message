package com.wp.casino.messageserver.event;


import java.util.EventListener;

public class EventListenerDemo implements EventListener {


    /**
     * 执行监听器
     * @param eventObject
     */
    public void execute(EventObjectDemo eventObject){
        System.err.println(eventObject.getSource());
    }
}
