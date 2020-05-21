package com.wp.casino.messageclient.service;

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
        log.info("MessageClient--init--");
        super.init();
        //proto_ww_user_data_change_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_user_data_change_req.class, (channel, message) -> {
            log.info("worldserver客户端收到messageserver服务端消息后进行处理.....");
//            channel.writeAndFlush(message);
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
