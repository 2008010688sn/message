package com.wp.casino.messagenetty.utils;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息分发器，用于将消息分发到各个消息handler
 *
 */
@Slf4j
public class MessageDispatcher {

    /**
     * messageClass->handler
     * 消息可能较多，这里初始化1024空间
     */
    private final Map<Class<?>,MessageHandler<?>> handlerMapping=new HashMap<>(1024);

    /**
     * 注册一个消息处理器
     * @param messageClass
     * @param handler
     * @param <T>
     */
    public <T extends MessageLite> void registerHandler(Class<T> messageClass, MessageHandler<T> handler){
        handlerMapping.put(messageClass,handler);
    }

    public MessageHandler removeHandler(Class<?> messageClass){
        return  handlerMapping.remove(messageClass);
    }

    /**
     * 当收到一个消息
     * @param channel
     * @param messageLite
     */
    public void onMessage(Channel channel, MessageLite messageLite){
        try {
            MessageHandler<MessageLite> messageHandler = (MessageHandler<MessageLite>) handlerMapping.get(messageLite.getClass());
            if (null==messageHandler){
                log.error("message handler is not registered, msgName={}",messageLite.getClass().getSimpleName());
                return;
            }
            messageHandler.onMessage(channel, messageLite);
        } catch (Exception e) {
            log.info("handle msg failed, msgName={}",messageLite.getClass().getSimpleName(),e);
        }
    }
}
