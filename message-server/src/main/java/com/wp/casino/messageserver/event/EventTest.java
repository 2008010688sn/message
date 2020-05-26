package com.wp.casino.messageserver.event;

import com.wp.casino.messageserver.utils.MessageDispatchTask;
import com.wp.casino.messageserver.utils.MessageQueue;

import javax.sound.midi.MetaMessage;

public class EventTest {
    public static void main(String args[]) {
        // 创建 eventListener
//        EventListenerDemo eventListener = new EventListenerDemo();
//        // 创建 eventSource
//        EventSourceDemo eventSource = new EventSourceDemo();
//        // 添加 eventListener
//        eventSource.addMyEventListener(eventListener);
//        // 创建事件源
//        Event event = new Event();
//        event.setName("sn--event");
//        EventObjectDemo eventObjectDemo = new EventObjectDemo(event);
//        // 事件通知
//        eventSource.notifyMyEvent(eventObjectDemo);
//
//
        MessageQuueEventListener messageQuueEventListener=new MessageQuueEventListener();

        MessageEventSource messageEventSource=new MessageEventSource();

        messageEventSource.addEventListener(messageQuueEventListener);
        String channelId="login-server";
        MessageDispatchTask messageDispatchTask=new MessageDispatchTask();
        messageDispatchTask.setChannelId(channelId);

        MessageQueue messageQueue=new MessageQueue();
        messageQueue.addMessageLite(messageDispatchTask);
        MessageEventObject eventObject=new MessageEventObject(messageQueue);
        messageEventSource.notifyEvent(eventObject);






    }
}
