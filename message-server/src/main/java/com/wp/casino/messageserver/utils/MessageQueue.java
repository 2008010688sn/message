package com.wp.casino.messageserver.utils;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author sn
 * @date 2020/5/22 12:48
 */
public class MessageQueue {


    private static ConcurrentLinkedQueue<MessageDispatchTask> messageDispatchTasks=new ConcurrentLinkedQueue<>();

    public static void addMessageLite(MessageDispatchTask messageDispatchTask) {
        messageDispatchTasks.add(messageDispatchTask);
    }

    public static void removeMessageLite(MessageDispatchTask messageDispatchTask) {
        messageDispatchTasks.remove(messageDispatchTask);
    }

    public static long getSize() {
        return messageDispatchTasks.size();
    }

    public static ConcurrentLinkedQueue<MessageDispatchTask> getAll() {
        return messageDispatchTasks;
    }

    public static MessageDispatchTask getAndRemove(){
        return messageDispatchTasks.poll();
    }

    public static MessageDispatchTask get(){
       return messageDispatchTasks.peek();
    }



}
