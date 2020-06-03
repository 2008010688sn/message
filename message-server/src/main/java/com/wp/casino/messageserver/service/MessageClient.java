package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messagenetty.client.NettyTcpClient;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.common.MagicId;
import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.common.MsgContentType;
import com.wp.casino.messageserver.common.MsgType;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.domain.*;
import com.wp.casino.messageserver.domain.mysql.casino.PyqClubMembers;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import com.wp.casino.messageserver.utils.ClubDataUtil;
import com.wp.casino.messageserver.utils.SendMsgUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

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
            /*log.info("messageserver客户端端接收到proto_ww_system_chat_req.并回执给worldserver",channel.remoteAddress().toString());
            channel.writeAndFlush(WorldMessage.proto_wf_system_chat_req.newBuilder()
                    .setPlyGuid( message.getPlyGuid()).build());
            // 获取通道
            Channel ch = getChannel(message.getPlyGuid());
            if (ch != null) {
                ch.writeAndFlush(message);
            }*/
        });

        // wordServer的web通知20538
        messageDispatcher.registerHandler(WorldMessage.proto_wf_web_msg_noti.class, (channel, message) -> {
            Integer type = message.getMsgType();
            ByteString data  = message.getMsgData();
            Parser<?> parser= MessageMappingHolder.getParser(MessageEnum.WL_NOTI_MSG_DATA.getOpCode());
            if (parser==null){
                throw new IllegalArgumentException("illegal opCode " + MessageEnum.WL_NOTI_MSG_DATA.getOpCode());
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
            receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
            list.add(receiveObj);

            SystemMessage sm = save2Mongo (message.getSenderId(), list, message.getMsgType(),
                    message.getShowMsgType(), messageContext, MsgConstants.SENDED,
                    message.getClubId(), message.getMsgRstId(), -1);

            // 转发协议到login
            SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, message.getMsgType(),message.getShowMsgType(),
                    sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                    sm.getMagicId());
        });

        // 玩家请求加入房间通知-----opcode：20529
        messageDispatcher.registerHandler(WorldMessage.proto_wf_join_room_noti.class, (channel, message) -> {
            // 申请加入房间的message
            RoomMessageContext rmc = new RoomMessageContext();
            rmc.setTableId(message.getTableId());
            rmc.setTableName(message.getTableName());
            rmc.setPlyguid(message.getPlyGuid());
            rmc.setPlynickname(message.getPlyNickname());
            rmc.setGameid(message.getGameId());
            rmc.setServerId(message.getServerId());
            rmc.setText("join room req");
            rmc.setContent("");
            rmc.setCode(0);
            rmc.setMagic_id(MagicId.APPLY_JOIN_ROOM_MSG.getMagicId());
            rmc.setTableCreateTime(message.getTableCreateTime());
            rmc.setInvitecode(message.getInviteCode());

            List<ReceiveObj> list = new ArrayList<>();
            ReceiveObj receiveObj = new ReceiveObj();
            receiveObj.setId(message.getOwnerGuid());
            receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
            list.add(receiveObj);

            SystemMessage sm = save2Mongo (message.getPlyGuid(), list, MsgType.GAME_NOTI_MSG.getMsgType(),
                    MsgContentType.ACK_MSG.getMsgContentType(), rmc, MsgConstants.SENDED,
                    0, MagicId.APPLY_JOIN_ROOM_MSG.getMagicId(), -1);

            SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, sm.getMessageType(),sm.getShowMessageType(),
                    sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                    sm.getMagicId());
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
                receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
                list.add(receiveObj);
            }
            clubMessageContext.setPlyId(message.getPlyGuid());

            // 落表
            SystemMessage sm = save2Mongo(message.getPlyGuid(), list, MsgType.PLAYER_NOTI_MSG.getMsgType(),
                    MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, MsgConstants.SENDED,
                    message.getClubId(), MagicId.DISMISS_CLUB_MSG.getMagicId(), -1);
            // 发送
            SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, sm.getMessageType(),sm.getShowMessageType(),
                    sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                    sm.getMagicId());
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
                    receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
                    list.add(receiveObj);

                    sm = save2Mongo(clubMemberUpdateInfo.getWhoGuid(), list,MsgType.PLAYER_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, MsgConstants.SENDED, clubMemberUpdateInfo.getClubId(),
                            result == 1 ? MagicId.AGREE_JOIN_CLUB_MSG.getMagicId() : MagicId.REFUSE_JOIN_CLUB_MSG.getMagicId(),
                            -1);

                    SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, sm.getMessageType(),sm.getShowMessageType(),
                            sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                            sm.getMagicId());

                    //修改 根据result修改请求的消息体的code值
                    systemMessageDao.updateMsgCode(clubMemberUpdateInfo.getMessageId(),
                            result == 0 ? 0 : 1, clubMemberUpdateInfo.getWhoGuid());

                    break;
                case WorldMessage.ClubMemberUpdateInfo.TYPE.LeaveClub_VALUE:
                    //退出俱乐部 通知所有管理员
                    List<PyqClubMembers> members = ClubDataUtil.getClubAdminList(clubMemberUpdateInfo.getClubId());
                    for (PyqClubMembers pcm: members) {
                        receiveObj = new ReceiveObj();
                        receiveObj.setId(clubMemberUpdateInfo.getPlyGuid());
                        receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
                        list.add(receiveObj);
                    }
                    sm = save2Mongo(clubMemberUpdateInfo.getPlyGuid(), list ,MsgType.CLUB_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, MsgConstants.SENDED, clubMemberUpdateInfo.getClubId(),
                            MagicId.DROP_OUT_CLUB_MSG.getMagicId(), -1);
                    SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, sm.getMessageType(),sm.getShowMessageType(),
                            sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                            sm.getMagicId());
                    break;
                case WorldMessage.ClubMemberUpdateInfo.TYPE.KickOut_VALUE:
                    //被踢出俱乐部
                    // 接收人
                    receiveObj = new ReceiveObj();
                    receiveObj.setId(clubMemberUpdateInfo.getPlyGuid());
                    receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
                    list.add(receiveObj);
                    // 数据落地
                    sm = save2Mongo(clubMemberUpdateInfo.getWhoGuid(), list,MsgType.PLAYER_NOTI_MSG.getMsgType(),
                            MsgContentType.TEXT_MSG.getMsgContentType(), clubMessageContext, MsgConstants.SENDED, clubMemberUpdateInfo.getClubId(),
                            MagicId.KICK_OUT_CLUB_MSG.getMagicId(), -1);
                    SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list, sm.getMessageType(),sm.getShowMessageType(),
                            sm.getMessageStatus(),sm.getClubId(),JSON.toJSONString(sm.getMessageContext()),sm.getExpireTime(),
                            sm.getMagicId());
                    break;
            }
        });


    }


    /**
     *  @param sendId 发送人
     * @param receiveObjList 接收人集合
     * @param messageType 消息类型
     * @param showMessageType 消息操作类型
     * @param messageContext 消息内容
     * @param messageStatus 状态
     * @param clubId 俱乐部
     * @param magicId 标识字符串
     * @param expireTime  过期时间
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


    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        //入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
//        pipeline.addLast(new IdleStateHandler(0, 1000, 0, TimeUnit.MICROSECONDS));
        super.initPipeline(pipeline);
    }
}
