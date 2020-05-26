package com.wp.casino.messageserver.event;


import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventSourceDemo {

    private ConcurrentLinkedQueue<EventListenerDemo> queue = new ConcurrentLinkedQueue<>();

    public EventSourceDemo() {
        super();
    }

    public void addMyEventListener(EventListenerDemo eventListener) {
        queue.offer(eventListener);
    }

    public void deleteMyEventListener(EventListenerDemo eventListener) {
        queue.remove(eventListener);
    }

    public void notifyMyEvent(EventObjectDemo eventObject) {
        Iterator<EventListenerDemo> iterator = queue.iterator();
        while (iterator.hasNext()) {
            //在类中实例化自定义的监听器对象,并调用监听器方法
            iterator.next().execute(eventObject);
        }
    }
}
