package com.wp.casino.messageserver.service;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sn
 * @date 2020/5/15 17:25
 */
@ChannelHandler.Sharable//该注解Sharable主要是为了多个handler可以被多个channel安全地共享，也就是保证线程安全
@Slf4j
public class MessageClientHandler extends SimpleChannelInboundHandler<MessageLite> {



    public   static volatile boolean isBegin ;

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
        log.info("心跳请求：" + new Date() + "，次数" + fcount.get());
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                //发送ping
              sendPingMsg(ctx);
            } else {
                super.userEventTriggered(ctx, obj);
            }
        }
    }

    /**
     * 发送ping消息
     * @param context
     */
    protected void sendPingMsg(ChannelHandlerContext context) {
        Timestamp timestamp= Timestamp.valueOf(LocalDateTime.now());
        int time = (int)(timestamp.getTime()/1000);
        WorldMessage.prt_ping ping=WorldMessage.prt_ping.newBuilder().setNowTime(time).build();
        context.channel().writeAndFlush(ping);
        int i = fcount.incrementAndGet();
        log.info("心跳请求次数{}",i);

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

        /*MessageQueue.addMessageLite(messageLite);
        isBegin=true;*/

    }
}
