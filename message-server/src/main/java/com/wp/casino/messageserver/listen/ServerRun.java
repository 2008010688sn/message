package com.wp.casino.messageserver.listen;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messageserver.service.MessageServer;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.MessageDispatchTask;
import com.wp.casino.messageserver.utils.MessageQueue;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @author sn
 * @date 2020/5/19 15:11
 */
@Component
@Slf4j
@Order(2)
public class ServerRun implements ApplicationRunner {

    @Value("${server.bind_port}")
    private int port;

    @Value("${server.bind_address}")
    private String host;

    private volatile boolean stoped = false;

    public void stop() {
        stoped = true;
    }

    public boolean isStoped() {
        return stoped;
    }

    private final Timer timer = new Timer("SendMessageTaskMonitor", true);

    private AtomicBoolean flushTask = new AtomicBoolean(false);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageServer messageServer=new MessageServer(host,port);
        messageServer.init();
        messageServer.start();
        addHook(messageServer);
        //处理消息分发
//        processTask();

    }

    private  void addHook(MessageServer server) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {

                    try {
                        server.stop();
                    } catch (Exception e) {
                        log.error(" server stop ex", e);
                    }
                    log.info("jvm exit, all service stopped.");

                }, "messageserver-shutdown-hook-thread")
        );
    }

    private void processTask()
    {
        ExecutorService executorService= Executors.newSingleThreadExecutor();

        executorService.submit(new Callable() {
            @Override
            public MessageDispatchTask call() throws Exception {
                ConcurrentLinkedQueue<MessageDispatchTask> queue=MessageQueue.getAll();

                while (!stoped){
                    if (queue.size()>0){
                        log.info("将消息分发至login---satrt");
                        //处理消息转发
                        MessageDispatchTask messageDispatchTask=MessageQueue.get();
                        MessageLite messageLite=messageDispatchTask.getMessageLite();
                        String channelId=messageDispatchTask.getChannelId();
                        MessageDispatcher messageDispatcher=messageDispatchTask.getMessageDispatcher();
                        ChannelHandlerContext channel = HandlerContext.getInstance().getChannel(channelId);
                        if (channel!=null&&messageLite!=null){
                            messageDispatcher.onMessage(channel.channel(),messageLite);
                            //处理完一个从对列中移除一个
                            MessageQueue.removeMessageLite(messageDispatchTask);
                        }


                    }


                }
                return null;
            }
        });
    }


}
