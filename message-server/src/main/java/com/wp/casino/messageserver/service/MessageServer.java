package com.wp.casino.messageserver.service;

import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author sn
 * @date 2020/5/15 16:38
 */
@Slf4j
@Component
public class MessageServer extends NettyTcpServer {

    private  ChannelHandler channelHandler;

    private  MessageDispatcher messageDispatcher;

    public MessageServer() {

        super(9876);
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageServerHandler(messageDispatcher);
    }

    @Override
    public void start(Listener listener) {
        super.start(listener);
    }

    @Override
    public void init() {
        super.init();
        //proto_ww_user_data_change_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_user_data_change_req.class, (channel, message) -> {
            log.info("rcv user_data_change_req message.");
            channel.writeAndFlush(PBCSMessage.proto_ww_user_data_change_noti.newBuilder().build());
        });

        //proto_ww_friend_msg_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_friend_msg_req.class, (channel, message) -> {
            PBCSMessage.proto_ww_friend_msg_noti respose = PBCSMessage.proto_ww_friend_msg_noti.newBuilder()
                    .build();
            channel.writeAndFlush(respose);
        });
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        pipeline.addLast(getChannelHandler());
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
