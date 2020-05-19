package com.wp.casino.messageserver.service;


import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sn
 * @date 2020/5/15 17:38
 * 业务处理
 */
@Slf4j
  public class MessageServerHandler extends SimpleChannelInboundHandler<MessageLite> {

    private final MessageDispatcher messageDispatcher;

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
        log.info("连接的客户端地址:" + ctx.channel().remoteAddress());
//        MsgEntity.Msg msg = MsgEntity.Msg.newBuilder().setMsgId("haha").setContent("内容content").setId("123").setName("test").build();
//       //从mq获取消息，发送
//
//
//        ctx.writeAndFlush(msg);
        super.channelActive(ctx);

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
        log.info("第" + count.get() + "次" + ",服务端接受的消息:"  );
        messageDispatcher.onMessage(ctx.channel(),messageLite);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
        ctx.close();
        log.info("exceptionCaught",cause);
    }
}