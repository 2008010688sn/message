package com.wp.casino.messageclient.service;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
    }


    /** 循环次数 */
    private AtomicInteger fcount = new AtomicInteger(1);

    /**
     * 建立连接时
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立连接时：" + new Date());
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

    /**
     * 业务逻辑处理
     */
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        // 如果不是protobuf类型的数据
//        if (!(msg instanceof MsgEntity.Msg)) {
//            log.info("未知数据!" + msg);
//            return;
//        }
//        try {
//
//            // 得到protobuf的数据
//            MsgEntity.Msg msg1 = (MsgEntity.Msg) msg;
//            // 进行相应的业务处理。。。
//            // 这里就从简了，只是打印而已
//            log.info(
//                    "客户端接受到的信息。编号:" + msg1.getId() + ",name:" + msg1.getName() + ",content:" + msg1.getContent());
//
//            // 这里返回一个已经接受到数据的状态
//            MsgEntity.Msg.Builder userState = MsgEntity.Msg.newBuilder();
//            ctx.writeAndFlush(userState);
//            log.info("成功发送给服务端!");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        log.info("客户端接受到的信息");
        messageDispatcher.onMessage(ctx.channel(),messageLite);
    }
}
