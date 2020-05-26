package com.wp.casino.messageserver.event;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author sn
 * @date 2020/5/26 11:18
 */
public class MessageEventSource {

    private ConcurrentLinkedQueue<MessageQuueEventListener> queue=new ConcurrentLinkedQueue<>();


    public MessageEventSource() {
        super();
    }

    public void addEventListener(MessageQuueEventListener eventListener) {
        queue.offer(eventListener);
    }

    public void deleteEventListener(MessageQuueEventListener eventListener) {
        queue.remove(eventListener);
    }

    public void notifyEvent(MessageEventObject eventObject) {
        Iterator<MessageQuueEventListener> iterator = queue.iterator();
        while (iterator.hasNext()) {
            //在类中实例化自定义的监听器对象,并调用监听器方法
            iterator.next().execute(eventObject);
        }
    }

}
