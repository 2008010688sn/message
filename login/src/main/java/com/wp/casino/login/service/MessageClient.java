package com.wp.casino.login.service;

import com.wp.casino.messagenetty.client.NettyTcpClient;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

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
        log.info("login--init--");
        super.init();
        //proto_ww_user_data_change_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_user_data_change_req.class, (channel, message) -> {
            log.info("login客户端接收message的proto_ww_user_data_change_req.",channel.remoteAddress().toString());
//            channel.writeAndFlush(PBCSMessage.proto_ww_user_data_change_noti.newBuilder().build());
        });

        //proto_ww_friend_msg_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_friend_msg_req.class, (channel, message) -> {
            PBCSMessage.proto_ww_friend_msg_noti respose = PBCSMessage.proto_ww_friend_msg_noti.newBuilder()
                    .build();
            channel.writeAndFlush(respose);
        });
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
