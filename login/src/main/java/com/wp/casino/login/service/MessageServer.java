package com.wp.casino.login.service;

import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/5/15 16:38
 */
@Slf4j
@Component
public class MessageServer extends NettyTcpServer {

    private  ChannelHandler channelHandler;

    private  MessageDispatcher messageDispatcher;



    private String host;

    private int port;


    public MessageServer() {
    }

    public MessageServer(int port) {
        super(port);
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageServerHandler(messageDispatcher);
    }

    public MessageServer(String host,int port) {
        super(port, host);
        this.host=host;
        this.port=port;
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
