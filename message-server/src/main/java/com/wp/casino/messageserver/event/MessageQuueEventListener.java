package com.wp.casino.messageserver.event;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.MessageDispatchTask;
import com.wp.casino.messageserver.utils.MessageQueue;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.EventListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sn
 * @date 2020/5/26 11:00
 */
@Slf4j
public class MessageQuueEventListener implements EventListener {

    private volatile boolean stoped = false;

    /**
     * 执行监听器
     * @param eventObject
     */
    public void execute(MessageEventObject eventObject){
        Object source = eventObject.getSource();
        MessageQueue messageQueue = (MessageQueue) source;
        processTask(messageQueue);

        log.info("执行监听器内容--------"+source);
    }

    private void processTask(MessageQueue messageQueue)
    {
        ExecutorService executorService= Executors.newSingleThreadExecutor();

        executorService.submit(new Callable() {
            @Override
            public MessageDispatchTask call() throws Exception {

                log.info("将消息分发至login---satrt");
                //处理消息转发
                MessageDispatchTask messageDispatchTask=messageQueue.get();
                MessageLite messageLite=messageDispatchTask.getMessageLite();
                String channelId=messageDispatchTask.getChannelId();
                MessageDispatcher messageDispatcher=messageDispatchTask.getMessageDispatcher();
                ChannelHandlerContext channel = HandlerContext.getInstance().getChannel(channelId);
                if (channel!=null&&messageLite!=null){
                    messageDispatcher.onMessage(channel.channel(),messageLite);
                    //处理完一个从对列中移除一个
                    MessageQueue.removeMessageLite(messageDispatchTask);
                }

                return null;
            }
        });
    }
}
