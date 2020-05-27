package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messagenetty.client.NettyTcpClient;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.domain.MessageContext;
import com.wp.casino.messageserver.domain.ReceiveObj;
import com.wp.casino.messageserver.domain.SystemMessage;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.HandlerServerContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sn
 * @date 2020/5/15 17:20
 */
@Slf4j
public class MessageClient extends NettyTcpClient {

    private  ChannelHandler channelHandler;

    private MessageDispatcher messageDispatcher;

    private SystemMessageDao systemMessageDao;

    public MessageClient() {
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageClientHandler(messageDispatcher);
        this.systemMessageDao= ApplicationContextProvider.getApplicationContext()
                .getBean(SystemMessageDao.class);
    }

    @Override
    public void init() {
        log.info("MessageClient--init--");
        super.init();

        //7136
        messageDispatcher.registerHandler(WorldMessage.proto_ww_trumpet_req.class, (channel, message) -> {
            log.info("messageserver客户端端接收到proto_ww_trumpet_req.并回执给worldserver",channel.remoteAddress().toString());
            channel.writeAndFlush(WorldMessage.proto_ww_trumpet_req.newBuilder()
                    .setPlyGuid(message.getPlyGuid()).build());
            // 获取通道
            Channel ch = getChannel(message.getPlyGuid());
            if (ch != null) {
                ch.writeAndFlush(message);
            }
        });

        //7183
        messageDispatcher.registerHandler(WorldMessage.proto_wf_system_chat_req.class, (channel, message) -> {
            log.info("messageserver客户端端接收到proto_ww_system_chat_req.并回执给worldserver",channel.remoteAddress().toString());
            channel.writeAndFlush(WorldMessage.proto_wf_system_chat_req.newBuilder()
                    .setPlyGuid(message.getPlyGuid()).build());
            // 获取通道
            Channel ch = getChannel(message.getPlyGuid());
            if (ch != null) {
                ch.writeAndFlush(message);
            }
        });

        // wordServer的web通知20539
        messageDispatcher.registerHandler(WorldMessage.proto_wf_web_msg_noti.class, (channel, message) -> {
            Integer type = message.getMsgType();
            ByteString data  = message.getMsgData();
            Parser<?> parser= MessageMappingHolder.getParser(20539);
            if (parser==null){
                throw new IllegalArgumentException("illegal opCode " + 20539);
            }
            MessageLite messageLite = (MessageLite) parser.parseFrom(data.toByteArray());
            messageDispatcher.onMessage(channel, messageLite);
        });

        // wordServer的web通知20539
        messageDispatcher.registerHandler(WorldMessage.proto_wl_noti_msg_data.class, (channel, message) -> {
            // 解析消息并入表
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setSendTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
            systemMessage.setExpireTime(-1);
            systemMessage.setClubId(message.getClubId());
            systemMessage.setGlobalStatus(0);
            systemMessage.setMagicId(message.getMsgRstId());
            systemMessage.setMessageType(message.getMsgType());
            systemMessage.setMessageStatus(0);
            systemMessage.setReceiveTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
            systemMessage.setOperator(0);
            systemMessage.setTitle(String.format("title_%s", message.getMsgRstId()));
            systemMessage.setSendId(message.getSenderId());
            systemMessage.setShowMessageType(message.getShowMsgType());
            // 消息体
            MessageContext messageContext = new MessageContext();
            messageContext.setContent(message.getMsgContent());
            messageContext.setText("web noti msg");
            systemMessage.setMessageContext(messageContext);
            // 接收人
            List receiveList = new ArrayList();
            ReceiveObj receiveObj = new ReceiveObj();
            receiveObj.setId(message.getRecieverId());
            receiveObj.setStatus(0);
            receiveList.add(receiveObj);
            systemMessage.setReceiveObjList(receiveList);
            // 数据落地
            SystemMessage sm = systemMessageDao.save(systemMessage);

            // 转发消息至login
            LoginMessage.proto_fl_noti_msg.Builder msg = LoginMessage.proto_fl_noti_msg.newBuilder();
            LoginMessage.proto_NotiMsgInfo.Builder msgBody = LoginMessage.proto_NotiMsgInfo.newBuilder();
            msgBody.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
            msgBody.setAutoid(sm.getAutoId());
            msgBody.setSenderId(sm.getSendId());
            msgBody.setMsgType(sm.getMessageType());
            msgBody.setMsgShowType(sm.getShowMessageType());
            msgBody.setMsg(JSON.toJSONString(sm.getMessageContext()));
            msgBody.setMsgStatus(sm.getMessageStatus());
            msgBody.setSendTime(sm.getSendTime());
            msgBody.setClubId(sm.getClubId());
            msgBody.setExpireTime(sm.getExpireTime());
            msgBody.setTitle(sm.getTitle());

            for (Object obj: sm.getReceiveObjList()
                    // 根据接收人列表循环发送
                 ) {
                ReceiveObj receiveObj1 = (ReceiveObj) obj;
                msgBody.setRecieverId(receiveObj1.getId());
                msgBody.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
                msg.setNotiMsgInfo(msgBody);
                transferMessage(message.getRecieverId(), msg.build());
            }
        });
    }

    private void transferMessage(long recieverId, LoginMessage.proto_fl_noti_msg message) {
        String channelId = HandlerServerContext.getInstance().getChannel(recieverId);
        if (StringUtils.isNotBlank(channelId)) {
            Channel ch = HandlerContext.getInstance().getChannel(channelId);
            if (ch != null) {
                ch.writeAndFlush(message);
            }
        }
    }

    // 获取通道
    private Channel getChannel(Long plyGuid) {
        String channelId = HandlerServerContext.getInstance().getChannel(plyGuid);
        if (StringUtils.isBlank(channelId)) {
            return null;
        }
        Channel ch = HandlerContext.getInstance().getChannel(channelId);
        return  ch;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
