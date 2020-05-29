package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messagenetty.client.NettyTcpClient;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.common.MagicId;
import com.wp.casino.messageserver.common.MsgContentType;
import com.wp.casino.messageserver.common.MsgType;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.domain.*;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import com.wp.casino.messageserver.utils.ClubDataUtil;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.HandlerServerContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public ChannelFuture connect(String host, int port, int heartbeat, int interval) {
        return super.connect(host, port).addListener((ChannelFuture f)->{
            if (heartbeat==1){//保持心跳
                EventLoop eventLoop = f.channel().eventLoop();
                if (!f.isSuccess()) {//断线重连
                    eventLoop.schedule(() -> connect(host, port), interval, TimeUnit.MILLISECONDS);//10ms后重连
                }
            }
        });
    }

    @Override
    public void init() {
        log.info("MessageClient--init--");
        super.init();

        //7181
        messageDispatcher.registerHandler(WorldMessage.proto_wf_register_ack.class,(channel, message) -> {
            if (message.getRet() == 0){
                log.info("register world success!");
            }
        });

        //7183  TODO
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

        // wordServer的web通知20538
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
            // 解析消息并入表数据落地
            MessageContext messageContext = new MessageContext();
            messageContext.setContent(message.getMsgContent());
            messageContext.setText("web noti msg");

            List<ReceiveObj> list = new ArrayList<>();
            ReceiveObj receiveObj = new ReceiveObj();
            receiveObj.setId(message.getRecieverId());
            receiveObj.setStatus(0);
            list.add(receiveObj);

            SystemMessage sm = save2Mongo (message.getSenderId(), list, message.getMsgType(),
                    message.getShowMsgType(), messageContext, 0,
                    message.getClubId(), message.getMsgRstId(), -1);

            // 转发协议到login
            trans2Login(sm);
        });

        // 玩家请求加入房间通知-----opcode：20529
        messageDispatcher.registerHandler(WorldMessage.proto_wf_join_room_noti.class, (channel, message) -> {
            // 申请加入房间的message
            RoomMessageContext rmc = new RoomMessageContext();
            rmc.setTableId(message.getTableId());
            rmc.setTableName(message.getTableName());
            rmc.setPlyguid(Integer.valueOf(String.valueOf(message.getPlyGuid())));
            rmc.setPlynickname(message.getPlyNickname());
            rmc.setGameid(message.getGameId());
            rmc.setServerId(message.getServerId());
            rmc.setText("join room req");
            rmc.setContent("");
            rmc.setCode(0);
            rmc.setMagic_id("apply_join_room_msg");
            rmc.setTableCreateTime(message.getTableCreateTime());
            rmc.setInvitecode(message.getInviteCode());

            List<ReceiveObj> list = new ArrayList<>();
            ReceiveObj receiveObj = new ReceiveObj();
            receiveObj.setId(message.getOwnerGuid());
            receiveObj.setStatus(0);
            list.add(receiveObj);

            SystemMessage sm = save2Mongo (message.getPlyGuid(), list, MsgType.GAME_NOTI_MSG.getMsgType(),
                    MsgContentType.ACK_MSG.getMsgContentType(), rmc, 0,
                    0, MagicId.APPLY_JOIN_ROOM_MSG.getMagicId(), -1);

            trans2Login(sm);
        });

        // 俱乐部解散---------------opcode:20540
        messageDispatcher.registerHandler(WorldMessage.proto_wf_break_up_club_noti.class, (channel, message) -> {
            ClubMessageContext clubMessageContext = new ClubMessageContext();
            clubMessageContext.setClubId(message.getClubId());
            clubMessageContext.setClubName(message.getClubName());
            clubMessageContext.setContent("");
            clubMessageContext.setText("");
            clubMessageContext.setNickName("");
            List<PyqClubMembers> members = ClubDataUtil.getClubAdminList(message.getClubId());

            List<ReceiveObj> list = new ArrayList<>();
            for (PyqClubMembers pcm : members) {
                ReceiveObj receiveObj = new ReceiveObj();
                receiveObj.setId(pcm.getCmPlyGuid());
                receiveObj.setStatus(0);
                list.add(receiveObj);
            }
            clubMessageContext.setPlyId(message.getPlyGuid());

            SystemMessage sm = save2Mongo(message.getPlyGuid(), list, MsgType.PLAYER_NOTI_MSG.getMsgType(),
                    MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, 0,
                    message.getClubId(), MagicId.DISMISS_CLUB_MSG.getMagicId(), -1);

            trans2Login(sm);

        });

        //俱乐部成员变更---opcode:20541
        messageDispatcher.registerHandler(WorldMessage.proto_wf_club_member_update_noti.class,
                (channel, message) -> {

            WorldMessage.ClubMemberUpdateInfo clubMemberUpdateInfo = message.getInfo();
            int reason = clubMemberUpdateInfo.getReason().getNumber();

            ClubMessageContext clubMessageContext = new ClubMessageContext();
            clubMessageContext.setClubId(clubMemberUpdateInfo.getClubId());
            clubMessageContext.setClubName(clubMemberUpdateInfo.getClubName());
            clubMessageContext.setContent("");
            clubMessageContext.setText("");
            clubMessageContext.setPlyId(clubMemberUpdateInfo.getPlyGuid());
            clubMessageContext.setNickName(clubMemberUpdateInfo.getPlyNickname());
            SystemMessage sm = null;
            List<ReceiveObj> list = new ArrayList<>();
            ReceiveObj receiveObj = null;
            switch (reason) {
                case WorldMessage.ClubMemberUpdateInfo.TYPE.JoinClub_VALUE:
                case WorldMessage.ClubMemberUpdateInfo.TYPE.RefuseJoin_VALUE:
                    // 申请通过或拒绝
                    clubMessageContext.setText("reply join club");
                    // 申请结果
                    int result = reason == WorldMessage.ClubMemberUpdateInfo.TYPE.JoinClub_VALUE ? 1 : 0;
                    // 接收人
                    receiveObj = new ReceiveObj();
                    receiveObj.setId(clubMemberUpdateInfo.getPlyGuid());
                    receiveObj.setStatus(0);
                    list.add(receiveObj);

                    sm = save2Mongo(clubMemberUpdateInfo.getWhoGuid(), list,MsgType.PLAYER_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, 0, clubMemberUpdateInfo.getClubId(),
                            result == 1 ? MagicId.AGREE_JOIN_CLUB_MSG.getMagicId() : MagicId.REFUSE_JOIN_CLUB_MSG.getMagicId(),
                            -1);
                    trans2Login(sm);
                    //修改 根据result修改请求的消息体的code值   TODO

                    break;
                case WorldMessage.ClubMemberUpdateInfo.TYPE.LeaveClub_VALUE:
                    //退出俱乐部 通知所有管理员
                    List<PyqClubMembers> members = ClubDataUtil.getClubAdminList(clubMemberUpdateInfo.getClubId());
                    for (PyqClubMembers pcm: members) {
                        receiveObj = new ReceiveObj();
                        receiveObj.setId(clubMemberUpdateInfo.getPlyGuid());
                        receiveObj.setStatus(0);
                        list.add(receiveObj);
                    }
                    sm = save2Mongo(clubMemberUpdateInfo.getPlyGuid(), list ,MsgType.CLUB_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, 0, clubMemberUpdateInfo.getClubId(),
                            MagicId.DROP_OUT_CLUB_MSG.getMagicId(), -1);
                    trans2Login(sm);
                    break;
                case WorldMessage.ClubMemberUpdateInfo.TYPE.KickOut_VALUE:
                    //被踢出俱乐部
                    // 接收人
                    receiveObj = new ReceiveObj();
                    receiveObj.setId(clubMemberUpdateInfo.getPlyGuid());
                    receiveObj.setStatus(0);
                    list.add(receiveObj);
                    // 数据落地
                    sm = save2Mongo(clubMemberUpdateInfo.getWhoGuid(), list,MsgType.PLAYER_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, 0, clubMemberUpdateInfo.getClubId(),
                            MagicId.KICK_OUT_CLUB_MSG.getMagicId(), -1);
                    trans2Login(sm);
                    break;
            }
        });


    }

    /**
     *  @param sendId
     * @param receiveObjList
     * @param messageType
     * @param showMessageType
     * @param messageContext
     * @param messageStatus
     * @param clubId
     * @param magicId
     * @param expireTime
     * @return
     */
    private SystemMessage save2Mongo (long sendId, List<ReceiveObj> receiveObjList, Integer messageType,
                                Integer showMessageType, Object messageContext, Integer messageStatus,
                                Integer clubId, String magicId, Integer expireTime) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setSendId(sendId);

        systemMessage.setReceiveObjList(receiveObjList);
        systemMessage.setMessageType(messageType);
        systemMessage.setShowMessageType(showMessageType);
        systemMessage.setMessageContext(messageContext);
        systemMessage.setMessageStatus(messageStatus);
        systemMessage.setClubId(clubId);
        systemMessage.setMagicId(magicId);
        systemMessage.setExpireTime(expireTime);
        systemMessage.setTitle(String.format("title_%s", magicId));

        systemMessage.setGlobalStatus(0);
        systemMessage.setOperator(0);

        systemMessage.setSendTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
        systemMessage.setReceiveTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
        // 数据落地
        SystemMessage sm = systemMessageDao.save(systemMessage);

        return sm;
    }

    /**
     *
     * @param sm
     */
    private void trans2Login(SystemMessage sm) {
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

            List<LoginMessage.proto_NotiMsgInfo> noti = new ArrayList<>();
            noti.add(msgBody.build());
            msg.addAllNotiMsgInfo(noti);

            LoginMessage.proto_fc_message_wrap_sync.Builder builder = LoginMessage.proto_fc_message_wrap_sync.newBuilder();
            builder.setPlyGuid(receiveObj1.getId());
            builder.setOpcode(MessageEnum.FL_NOTI_MSG.getOpCode());
            builder.setData(msg.build().toByteString());
            transferMessage(receiveObj1.getId(), builder.build());
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

    private void transferMessage(long recieverId, LoginMessage.proto_fc_message_wrap_sync message) {
        String channelId = HandlerServerContext.getInstance().getChannel(recieverId);
        if (StringUtils.isNotBlank(channelId)) {
            Channel ch = HandlerContext.getInstance().getChannel(channelId);
            if (ch != null) {
                ch.writeAndFlush(message);
            }
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
