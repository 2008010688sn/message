package com.wp.casino.messageserver.service;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messageserver.event.MessageEventObject;
import com.wp.casino.messageserver.event.MessageEventSource;
import com.wp.casino.messageserver.event.MessageQuueEventListener;
import com.wp.casino.messageserver.utils.DispatcherObj;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.MessageDispatchTask;
import com.wp.casino.messageserver.utils.MessageQueue;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author sn
 * @date 2020/5/15 17:25
 */
@ChannelHandler.Sharable//该注解Sharable主要是为了多个handler可以被多个channel安全地共享，也就是保证线程安全
@Slf4j
public class MessageClientHandler extends SimpleChannelInboundHandler<MessageLite> {



    public  static volatile boolean isBegin ;

    private  MessageDispatcher messageDispatcher;

    public MessageClientHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher=messageDispatcher;
    }


    /** 循环次数 */
    private AtomicInteger fcount = new AtomicInteger(1);

    /**
     * 建立连接时
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("和worldserver建立连接时：" + new Date());
        PBCSMessage.proto_ww_user_data_change_req msg = PBCSMessage.proto_ww_user_data_change_req.newBuilder().setPlyGuid(10).setType(2).build();

        ctx.writeAndFlush( msg);
        ctx.fireChannelActive();
    }

    /**
     * 关闭连接时
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("关闭连接时：" + new Date());
//        final EventLoop eventLoop = ctx.channel().eventLoop();
//        nettyClient.doConnect(new Bootstrap(), eventLoop);
        super.channelInactive(ctx);
    }

    /**
     * 心跳请求处理 每4秒发送一次心跳请求;
     *
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        log.info("客户端心跳处理，每4秒发送一次心跳请求;循环请求的时间：" + new Date() + "，次数" + fcount.get());
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {

//                MsgEntity.Msg.Builder msg = MsgEntity.Msg.newBuilder().setMsgId("client").setName("qqq");
//                ctx.channel().writeAndFlush(msg);
//                fcount.getAndIncrement();
            }
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        log.info("messageserver客户端接受到worldserver信息:{}",ctx.channel().remoteAddress().toString());
        messageDispatcher.onMessage(ctx.channel(),messageLite);
        //将消息转发至login
//        log.info("messageserver客户端将接受到worldserver的信息转发至loginserver");
//        ConcurrentHashMap<String, ChannelHandlerContext> maps= HandlerContext.getInstance().getMaps();
//        if (maps!=null){
//            for (Map.Entry<String,ChannelHandlerContext> entry: maps.entrySet()){
//               ChannelHandlerContext channelHandlerContext= entry.getValue();
//               log.info("channelHandlerContext--remoteAddress---",channelHandlerContext.channel().remoteAddress().toString());
//               messageDispatcher.onMessage(channelHandlerContext.channel(),messageLite);
//            }
//        }


//        int size = HandlerContext.getInstance().getSize();
//        if (size>0){
//            log.info("获取login的连接，将接收到的wordserver消息转发给login");
//            ChannelHandlerContext channelContext = HandlerContext.getInstance().getChannel("login-server");
//            log.info("loign的连接为--"+channelContext.channel().remoteAddress());
//            messageDispatcher.onMessage(channelContext.channel(),messageLite);
//        }else{
//            log.info("无login连接");
//        }
        //收到wordserver的消息，存放至对列
        String channelId="login-server";
        MessageDispatchTask messageDispatchTask=new MessageDispatchTask();
        messageDispatchTask.setChannelId(channelId);
        messageDispatchTask.setMessageLite(messageLite);
        //根据channelId查找messageDispatcher
        DispatcherObj dispatcherObj = HandlerContext.getInstance().getChannel(channelId);
        MessageDispatcher md = dispatcherObj.getMessageDispatcher();
        messageDispatchTask.setMessageDispatcher(md);

        MessageQuueEventListener messageQuueEventListener=new MessageQuueEventListener();
        MessageEventSource messageEventSource=new MessageEventSource();
        messageEventSource.addEventListener(messageQuueEventListener);

        MessageQueue messageQueue=new MessageQueue();
        messageQueue.addMessageLite(messageDispatchTask);
//        isBegin=true;


        MessageEventObject eventObject=new MessageEventObject(messageQueue);
        messageEventSource.notifyEvent(eventObject);




    }
}
