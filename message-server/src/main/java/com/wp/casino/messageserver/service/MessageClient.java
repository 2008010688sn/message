package com.wp.casino.messageserver.service;

import com.wp.casino.messagenetty.client.NettyTcpClient;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author sn
 * @date 2020/5/15 17:20
 */
@Slf4j
public class MessageClient extends NettyTcpClient {

    private  ChannelHandler channelHandler;

    private MessageDispatcher messageDispatcher;

    public MessageClient() {
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageClientHandler(messageDispatcher);
    }

    @Override
    public void init() {
        log.info("MessageClient--init--");
        super.init();
        //proto_ww_user_data_change_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_user_data_change_req.class, (channel, message) -> {
            log.info("messageserver客户端端接收到proto_ww_user_data_change_req.并回执给worldserver",channel.remoteAddress().toString());
//            channel.writeAndFlush(PBCSMessage.proto_ww_user_data_change_req.newBuilder().setPlyGuid(777).build());
        });

        //proto_ww_friend_msg_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_friend_msg_req.class, (channel, message) -> {
            PBCSMessage.proto_ww_friend_msg_noti respose = PBCSMessage.proto_ww_friend_msg_noti.newBuilder()
                    .build();
            channel.writeAndFlush(respose);
        });
    }

    @Override
    public ChannelFuture connect(String host, int port) {
        return super.connect(host, port).addListener((ChannelFuture f)->{
            if (f.isSuccess()){
                log.info("start message client success,host={},port={}",host,port);
            }else{
                //断线重连机制
//                EventLoop eventLoop = f.channel().eventLoop();
//                eventLoop.schedule(() -> connect(host, port), 10, TimeUnit.SECONDS);//10s后重连
            }
        });
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        //入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
//        pipeline.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
        super.initPipeline(pipeline);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
