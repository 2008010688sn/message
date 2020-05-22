package com.wp.casino.worldserver.service;


import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.worldserver.utils.HandlerContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sn
 * @date 2020/5/15 17:38
 * 业务处理
 */
@Slf4j
@ChannelHandler.Sharable//该注解Sharable主要是为了多个handler可以被多个channel安全地共享，也就是保证线程安全
  public class MessageServerHandler extends SimpleChannelInboundHandler<MessageLite> {

    private  MessageDispatcher messageDispatcher;

    public MessageServerHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * 空闲次数
     */
    private AtomicInteger idle_count = new AtomicInteger(1);
    /**
     * 发送次数
     */
    private AtomicInteger count = new AtomicInteger(1);
    /**
     * 建立连接时，发送一条消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("worldserver接收到连接的客户端地址:" + ctx.channel().remoteAddress());
        String channelId=ctx.channel().remoteAddress().toString();
        HandlerContext.getInstance().addChannel(channelId,ctx);
        //message建立连接后模拟向message发送一条信息
        PBCSMessage.proto_ww_user_data_change_req msg = PBCSMessage.proto_ww_user_data_change_req.newBuilder().setPlyGuid(10).setType(2).build();
        ctx.writeAndFlush( msg);

        log.info("WorldServerHandler---active---");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel通道注销-"+ctx.channel());
        String channelId=ctx.channel().id().asLongText();
        channelId=ctx.channel().remoteAddress().toString();
        HandlerContext.getInstance().removeChannel(channelId);
        log.info("WorldServerHandler---channelInactive---");
    }

    /**
     * 超时处理 如果5秒没有接受客户端的心跳，就触发; 如果超过两次，则直接关闭;
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果读通道处于空闲状态，说明没有接收到心跳命令
            if (IdleState.READER_IDLE.equals(event.state())) {
                log.info("已经5秒没有接收到客户端的信息了");
                if (idle_count.get() > 1) {
                    log.info("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
                idle_count.getAndIncrement();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        log.info("worldserver第" + count.get() + "次接受message客户端的消息，客户端地址："+ctx.channel().remoteAddress().toString()+"进行消息处理..."  );
        messageDispatcher.onMessage(ctx.channel(),messageLite);

    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String remoteAddress=ctx.channel().remoteAddress().toString();
        HandlerContext.getInstance().removeChannel(remoteAddress);
        ctx.close();
        log.info("exceptionCaught",cause);
    }
}