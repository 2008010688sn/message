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


    private static ConcurrentLinkedQueue<MessageLite> messageLiteList=new ConcurrentLinkedQueue<>();

    public static void addMessageLite(MessageLite messageLite) {
        messageLiteList.add(messageLite);
    }

    public static void removeMessageLite(MessageLite messageLite) {
        messageLiteList.remove(messageLite);
    }

    public static long getSize() {
        return messageLiteList.size();
    }

    public static ConcurrentLinkedQueue<MessageLite> getAll() {
        return messageLiteList;
    }



}
