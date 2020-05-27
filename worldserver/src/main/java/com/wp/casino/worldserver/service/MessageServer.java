package com.wp.casino.worldserver.service;

import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.PBCSMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.util.locale.provider.LocaleServiceProviderPool;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_user_data_change_req.class, (channel, message) -> {
            log.info("worldserver服务端处理 proto_ww_user_data_change_req proto .start ");
            log.info("处理时的channelid------",channel.id().asLongText());
            log.info("message---guid-{}--type:{}",message.getPlyGuid(),message.getType());
//            SystemMessage sm=new SystemMessage();
//            MessageContext mc=new MessageContext();
//            mc.setContent("这是测试消息");
//            mc.setText("web--test");
//            ReceiveObj receiveObj=new ReceiveObj();
//            receiveObj.setId(111l);
//            receiveObj.setStatus(1);
//            sm.setMessageContext(mc);
//            List array= Arrays.asList(receiveObj);
//            sm.setReceiveObjList(array);
            //将客户端的消息写入mongo
//           SystemMessage re= systemMessageDao.save(sm);
//            SystemMessage re=ApplicationContextProvider.getApplicationContext().getBean(SystemMessageDao.class).save(sm);
//            log.info("----re:"+re.getId());
            //Message回给Login，让Login转发给客户端的
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    log.info("world定时转给message---");
                    PBCSMessage.proto_ww_user_data_change_req respose = PBCSMessage.proto_ww_user_data_change_req.newBuilder().setPlyGuid(333)
                            .build();
                    channel.writeAndFlush(respose);
                }
            },60,60, TimeUnit.SECONDS);

        });

        //proto_ww_friend_msg_req协议
        messageDispatcher.registerHandler(PBCSMessage.proto_ww_friend_msg_req.class, (channel, message) -> {
            log.info("channelid {} rcv user_data_change_req message.",channel.id().asLongText());
            PBCSMessage.proto_ww_friend_msg_noti respose = PBCSMessage.proto_ww_friend_msg_noti.newBuilder()
                    .build();
            channel.writeAndFlush(respose);
        });
//Login去Message注册,
// message回复login注册结果
        messageDispatcher.registerHandler(PBCSMessage.proto_lm_register_req.class, (channel, message) -> {
            log.info("channelid {} rcv proto_ml_register_ack message.",channel.id().asLongText());
            PBCSMessage.proto_ml_register_ack respose = PBCSMessage.proto_ml_register_ack.newBuilder()
                    .build();
            channel.writeAndFlush(respose);
        });

        //Login所有连接到它的玩家信息告知Message;
        // Message回给Login，让Login转发给客户端的
        messageDispatcher.registerHandler(PBCSMessage.proto_lm_update_ply_login_status_not.class, (channel, message) -> {
            log.info("channelid {} rcv proto_lm_update_ply_login_status_not message.",channel.id().asLongText());

            //Message回给Login，让Login转发给客户端的
            PBCSMessage.proto_lm_noti_msg respose = PBCSMessage.proto_lm_noti_msg.newBuilder()
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