package com.wp.casino.messagenetty.utils;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息映射管理
 *
 * 要在应用启动的最开始的时候加载该类
 */
@Slf4j
public class MessageMappingHolder {


    private static final Map<Integer,Parser<?>> opCode2ParserMap=new HashMap<>(1024);

    private static final Map<Class<?>,Integer> messageClass2IdMap=new HashMap<>(1024);

    static {
        for (MessageEnum messageEnum:MessageEnum.values()){
            try {
                Class<? extends MessageLite> messageClass=ProtoBufUtils.findMessageClass(messageEnum.getJavaPackageName(),
                        messageEnum.getJavaOuterClassName(),messageEnum.getMessageName());
                Parser<? extends MessageLite> parser=ProtoBufUtils.findMessageParser(messageClass);
                messageClass2IdMap.put(messageClass,messageEnum.getOpCode());
                opCode2ParserMap.put(messageEnum.getOpCode(),parser);
            }catch (ReflectiveOperationException e){
                // 内部异常捕获，是为了记录更详细的信息
                log.error("can't find class or parser,messageInfo={}",messageEnum.toString(),e);
            }
        }
    }

    /**
     * 根据消息id 获取对应的parser
     * @param opCode
     * @param <T>
     * @return
     */
    public static <T> Parser<T> getParser(int opCode){
        return (Parser<T>) opCode2ParserMap.get(opCode);
    }

    /**
     * 获取对象对应的opCode
     * @param message
     * @param <T>
     * @return
     */
    public static <T extends MessageLite> int getopCode(T message){
        Class<T> messageLiteClass= (Class<T>) message.getClass();
        return getopCode(messageLiteClass);
    }

    /**
     * 获取消息类对应的opCode
     * @param messageClass
     * @param <T>
     * @return
     */
    public static <T extends MessageLite> int getopCode(Class<T> messageClass){
        return messageClass2IdMap.get(messageClass);
    }

}
