package com.wp.casino.login.service;

import com.google.protobuf.MessageLite;
import com.wp.casino.login.utils.HandlerContext;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sn
 * @date 2020/5/15 17:25
 */
@ChannelHandler.Sharable//该注解Sharable主要是为了多个handler可以被多个channel安全地共享，也就是保证线程安全
@Slf4j
public class MessageClientHandler extends SimpleChannelInboundHandler<MessageLite> {

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
        log.info("login和message建立连接时：" + new Date());
//        PBCSMessage.proto_ww_user_data_change_req msg = PBCSMessage.proto_ww_user_data_change_req.newBuilder().setPlyGuid(10).setType(2).build();
//
//        ctx.writeAndFlush( msg);
//        ctx.fireChannelActive();
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
        log.info("循环请求的时间：" + new Date() + "，次数" + fcount.get());
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
        log.info("login客户端接受到msgserver的信息1:{}",ctx.channel().remoteAddress().toString());
        //获取login的channel
//        ChannelHandlerContext loginCtx= HandlerContext.getInstance().getChannel("login-serverid");
        messageDispatcher.onMessage(ctx.channel(),messageLite);
    }
}
