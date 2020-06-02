package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.common.MagicId;
import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.common.MsgContentType;
import com.wp.casino.messageserver.common.MsgType;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.domain.*;
import com.wp.casino.messageserver.domain.mysql.casino.ClubChatInfo;
import com.wp.casino.messageserver.domain.mysql.casino.PyqClubMembers;
import com.wp.casino.messageserver.utils.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sn
 * @date 2020/5/15 16:38
 */
@Slf4j
@Component
public class MessageServer extends NettyTcpServer {

    private  ChannelHandler channelHandler;

    private  MessageDispatcher messageDispatcher;

    @Autowired
    private SystemMessageDao systemMessageDao;


    private String host;

    private int port;


    public MessageServer() {
    }

    public MessageServer(int port) {
        super(port);
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageServerHandler(messageDispatcher);
        this.systemMessageDao=ApplicationContextProvider.getApplicationContext()
                .getBean(SystemMessageDao.class);
    }

    public MessageServer(String host,int port) {
        super(port, host);
        this.host=host;
        this.port=port;
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageServerHandler(messageDispatcher);
        this.systemMessageDao=ApplicationContextProvider.getApplicationContext()
                .getBean(SystemMessageDao.class);

    }

    @Override
    public void start(Listener listener) {
        super.start(listener);
    }

    @Override
    public void init() {
        super.init();

        //客户端发送给消息服务器的消息包装(login原封不动的转发)
        //proto_cl_load_noti_msg_req
        //proto_cl_update_msg_status_req
        //proto_cl_get_msg_count_req
        messageDispatcher.registerHandler(LoginMessage.proto_cf_message_wrap_sync.class,
                (channel, message) -> {
            int opcode = message.getOpcode();
            ByteString data =  message.getData();
            Parser<?> parser= MessageMappingHolder.getParser(opcode);
            if (parser==null){
                throw new IllegalArgumentException("illegal opCode " + opcode);
            }

            if (opcode == MessageEnum.CL_LOAD_NOTI_MSG_REQ.getOpCode()) {
                handleLoadNotiMsg(message.getPlyGuid(), (LoginMessage.proto_cl_load_noti_msg_req)parser.parseFrom(data.toByteArray()));
            } else if (opcode == MessageEnum.CL_UPDATE_MSG_STATUS_REQ.getOpCode()) {
                handleUpdateMsg(message.getPlyGuid(), (LoginMessage.proto_cl_update_msg_status_req)parser.parseFrom(data.toByteArray()));
            } else if (opcode == MessageEnum.CL_GET_MSG_COUNT_REQ.getOpCode()) {
                handleGetMsgCount(message.getPlyGuid(), (LoginMessage.proto_cl_get_msg_count_req)parser.parseFrom(data.toByteArray()));
            }
        });

        //Login去Message注册,
        // message回复login注册结果
        messageDispatcher.registerHandler(LoginMessage.proto_lf_register_req.class,
                (channel, message)->{
            //维护channel
            String channelId = channel.remoteAddress().toString();
            HandlerContext.getInstance().addChannel(channelId,channel);

            //Message回复Login注册结果
            LoginMessage.proto_fl_register_ack response=LoginMessage.proto_fl_register_ack
                    .newBuilder().setRet(1).build();

            LoginMessage.proto_fc_message_wrap_sync.Builder sync = LoginMessage.proto_fc_message_wrap_sync.newBuilder();
            sync.setPlyGuid(0);
            sync.setOpcode(MessageEnum.FL_REGISTER_ACK.getOpCode());
            sync.setData(response.toByteString());
            channel.writeAndFlush(sync.build());
        });

        //Login所有连接到它的玩家信息告知Message;
        messageDispatcher.registerHandler(LoginMessage.proto_lf_update_ply_login_status_not.class,
                (channel,message)->{
            // 用户注册，维护用户和地址
            String channelId=channel.remoteAddress().toString();

            LoginPlayer loginPlayer = new LoginPlayer();
            loginPlayer.setHeadImg(message.getHeadImg());
            loginPlayer.setNickName(message.getNickName());
            loginPlayer.setPlyGuid(message.getPlyGuid());
            loginPlayer.setPlyLevel(message.getPlyLevel());
            loginPlayer.setUserLanguage(message.getUserLanguage());
            loginPlayer.setPlyVip(message.getPlyVip());
            loginPlayer.setServerId(channelId);

            HandlerServerContext.getInstance().addChannel(message.getPlyGuid(), loginPlayer);
        });

        // 玩家下线移除关系
        messageDispatcher.registerHandler(LoginMessage.proto_lf_update_ply_logout_status_not.class,
                (channel, message) -> {
                HandlerServerContext.getInstance().removeChannel(message.getPlyGuid());
        });

        //申请加入俱乐部
        messageDispatcher.registerHandler(LoginMessage.proto_lf_club_apply_join_noti.class,(channel, message) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("club_id", message.getClubId());
            jsonObject.put("club_name", message.getClubName());
            jsonObject.put("ply_id", message.getApplyPlyGuid());
            jsonObject.put("nick_name", message.getApplyPlyName());
            jsonObject.put("who_do_id", 0);
            jsonObject.put("type", 1); // 1:申请 2：离开3：添加管理员 4：删除管理员
            jsonObject.put("referrer_guid", message.getReferrerGuid());

            // 入表
            ClubMessageContext cmc = new ClubMessageContext();
            cmc.setClubId(message.getClubId());
            cmc.setNickName(message.getApplyPlyName());
            cmc.setClubName(message.getClubName());
            cmc.setPlyId(message.getApplyPlyGuid());
            cmc.setText("join req");
            cmc.setCode(0);
            cmc.setContent("");

            //接收人
            List<PyqClubMembers> members = ClubDataUtil.getClubAdminList(message.getClubId());
            List<ReceiveObj> list = new ArrayList<>();
            for (PyqClubMembers pcm: members) {
                ReceiveObj receiveObj = new ReceiveObj();
                receiveObj.setId(pcm.getCmPlyGuid());
                receiveObj.setStatus(0);
                list.add(receiveObj);
            }

            SystemMessage sm = save2Mongo (message.getApplyPlyGuid(), list, MsgType.CLUB_NOTI_MSG.getMsgType(),
                    MsgContentType.ACK_MSG.getMsgContentType(), cmc, 0,
                    message.getClubId(), MagicId.APPLY_JOIN_CLUB_MSG.getMagicId(), -1);

            // 转发协议到login
            SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list,
                    MsgType.PLAYER_NOTI_MSG.getMsgType(), MsgContentType.TEXT_MSG.getMsgContentType(), 0, 0,
                    JSONObject.toJSONString(cmc), Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000 + (24 * 60 * 60))),
                    sm.getMagicId());
        });

        // 回复加入房间通知
        messageDispatcher.registerHandler(LoginMessage.proto_lf_join_room_reply_noti.class,(channel, message) -> {
            String magicId = "";
            JSONObject jsonObject = new JSONObject();
            if (message.getRet() == 1) {
                // 同意加入房间
                magicId = MagicId.AGREE_JOIN_TABLE_MSG.getMagicId();
                jsonObject.put("text", "join room ack agree");
            } else {
                magicId = MagicId.REFUSE_JOIN_TABLE_MSG.getMagicId();
                jsonObject.put("text", "join room ack refuse");
            }
            jsonObject.put("code", message.getRet() == 1 ? 1 : 0);
            jsonObject.put("content", "");
            jsonObject.put("tip", "1");
            jsonObject.put("tablename", message.getTableName());

            // 协议封装并发送
            List<ReceiveObj> list = new ArrayList<>();
            ReceiveObj receiveObj = new ReceiveObj();
            receiveObj.setId(message.getPlyGuid());
            receiveObj.setStatus(MsgConstants.MSG_STATUS_UNREAD);
            list.add(receiveObj);
            SendMsgUtil.sendNotiMsg(0, message.getOwnerGuid(),list,
                    MsgType.PLAYER_NOTI_MSG.getMsgType(), MsgContentType.TEXT_MSG.getMsgContentType(), MsgConstants.SENDED, 0,
                    jsonObject.toJSONString(), -1 , magicId);

            // 修改申请加入房间消息的code
            systemMessageDao.updateMsgCode(message.getMessageId(), message.getRet() == 1 ? 1 : 0, message.getOwnerGuid());
        });


        //获取俱乐部聊天记录-
        messageDispatcher.registerHandler(LoginMessage.proto_cf_add_club_chat_record_req.class,
                (channel, message) -> {
            // 拉取聊天记录 存储过程：PYQ_ADD_CLUB_CHAT_RECORD
            LoginMessage.proto_fc_add_club_chat_record_ack.Builder ackBuilder = LoginMessage.
                    proto_fc_add_club_chat_record_ack.newBuilder();
            ackBuilder.setRet(0);
            ackBuilder.setErrMsg("");
            List<LoginMessage.proto_ClubChatRecordInfoStruct> recordInfoList = new ArrayList<>();

            // 聊天数据插入mysql
            Integer count = ClubDataUtil.findClubChatInfoCountByClubId(message.getClubUid());
            Integer recordNum = 0;
            if (count > 0) {
                recordNum = ClubDataUtil.findMaxClubMessageIdByClubId(message.getClubUid());
            }
            ClubChatInfo cci = new ClubChatInfo();
            cci.setClClubMessageId(recordNum + 1);
            cci.setClMemberUid(message.getPlyGuid());
            cci.setClGameId(message.getGameId());
            cci.setClClubId(message.getClubUid());
            cci.setClChatMessage(message.getChatMsg());
            cci.setClMsgType(message.getType());
            cci.setClMessageSendTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
            ClubChatInfo clubChatInfo = ClubDataUtil.saveClubChatInfo(cci);

            //协议转发
            LoginMessage.proto_ClubChatRecordInfoStruct.Builder recordInfo = LoginMessage.proto_ClubChatRecordInfoStruct.newBuilder();
            recordInfo.setAutoId(clubChatInfo.getClAutoId());
            recordInfo.setPlyId(message.getPlyGuid());
            recordInfo.setClubUid(clubChatInfo.getClClubId());
            recordInfo.setGameId(clubChatInfo.getClGameId());
            recordInfo.setChatMsg(clubChatInfo.getClChatMessage());
            recordInfo.setSendMsgTime(clubChatInfo.getClMessageSendTime());
            recordInfo.setType(clubChatInfo.getClMsgType());
            recordInfo.setClubMessageId(clubChatInfo.getClClubMessageId());
            recordInfoList.add(recordInfo.build());

            ackBuilder.addAllClubChatRecordInfo(recordInfoList);
            // 封装记录回执
            SendMsgUtil.sendProtoPack(MessageEnum.FC_ADD_CLUB_CHAT_RECORD_ACK.getOpCode(), ackBuilder.build(), message.getPlyGuid());

            //fc_add_club_chat_record_noti广播
            LoginMessage.proto_fl_club_notify.Builder notify = LoginMessage.proto_fl_club_notify.newBuilder();

            LoginMessage.proto_fc_add_club_chat_record_noti recordNotiMsg =
                    LoginMessage.proto_fc_add_club_chat_record_noti.newBuilder().
                            addAllClubChatRecordInfo(recordInfoList).build();

            notify.setClubId(clubChatInfo.getClClubId());
            notify.setData(recordNotiMsg.toByteString());
            notify.setOpcode(MessageEnum.FC_ADD_CLUB_CHAT_RECORD_NOTI.getOpCode());
            notify.setExceptPlyGuid(-1);
            // 全部发送
            for (Map.Entry<String, Channel> entry : HandlerContext.getInstance().getMaps().entrySet()) {
                entry.getValue().writeAndFlush(notify.build());
            }
        });

        // 俱乐部聊天
        messageDispatcher.registerHandler(LoginMessage.proto_cf_sync_club_chat_record_req.class,
                (channel, message) -> {
            //PYQ_SYNC_CLUB_CHAT_RECORD
            LoginMessage.proto_fc_sync_club_chat_record_ack.Builder ackBuilder = LoginMessage.
                    proto_fc_sync_club_chat_record_ack.newBuilder();
            ackBuilder.setRet(0);
            ackBuilder.setErrMsg("");
            ackBuilder.setPlyGuid(message.getPlyGuid());

            List<LoginMessage.proto_ClubChatRecordInfoStruct> recordInfoList = new ArrayList<>();

            PyqClubMembers pyqClubMembers = ClubDataUtil.findClubMemberByClubIdAndUid( message.getClubUid(), message.getPlyGuid());
            if (pyqClubMembers == null) {
                log.error("pyqClubMembers is null");
                return;
            }
            List<ClubChatInfo> syncClubChatRecords = ClubDataUtil.findClubChatInfos(message.getClubUid(),
                    message.getAutoid(),pyqClubMembers.getCmJoinTime(), message.getReqNum());
            if (syncClubChatRecords == null || syncClubChatRecords.size() == 0) {
                log.error("syncClubChatRecords list is null");
                return;
            }

            for (ClubChatInfo clubChatInfo: syncClubChatRecords) {
                LoginMessage.proto_ClubChatRecordInfoStruct.Builder recordInfo = LoginMessage.proto_ClubChatRecordInfoStruct.newBuilder();
                recordInfo.setAutoId(clubChatInfo.getClAutoId());
                recordInfo.setPlyId(message.getPlyGuid());
                recordInfo.setGameId(clubChatInfo.getClGameId());
                recordInfo.setClubUid(clubChatInfo.getClClubId());
                recordInfo.setSendMsgTime(clubChatInfo.getClMessageSendTime());
                recordInfo.setChatMsg(clubChatInfo.getClChatMessage());
                recordInfo.setType(clubChatInfo.getClMsgType());
                recordInfo.setClubMessageId(clubChatInfo.getClClubMessageId());
                recordInfoList.add(recordInfo.build());
            }
            ackBuilder.addAllClubChatRecordInfo(recordInfoList);
            // 回执
            SendMsgUtil.sendProtoPack(MessageEnum.FC_SYNC_CLUB_CHAT_RECORD_ACK.getOpCode(), ackBuilder.build(), message.getPlyGuid());
        });
    }
    /**
     * 处理拉取消息
     */
    private void handleLoadNotiMsg(long plyGuid, LoginMessage.proto_cl_load_noti_msg_req message) {

        //拉取消息记录
        List<SystemMessage> list = systemMessageDao.findNotiMsg(message.getPlyGuid(), message.getType(),
                message.getClubId(), message.getAutoId(), message.getMaxCount());
        // 封装消息回执
        LoginMessage.proto_lc_load_noti_msg_ack.Builder response = LoginMessage
                .proto_lc_load_noti_msg_ack.newBuilder();
        List<LoginMessage.proto_NotiMsgInfo> msgInfoList = new ArrayList<>();
        for (SystemMessage sm: list) {
            for (Object obj : sm.getReceiveObjList()) {
                ReceiveObj ro = (ReceiveObj) obj;

                LoginPlayer player = HandlerServerContext.getInstance().getChannel(ro.getId());
                if (player == null || StringUtils.isBlank(player.getServerId())) {
                    log.info("ply_guid:%lld offline, jsonStr:%s", ro.getId(), ro);
                    continue;
                }

                LoginMessage.proto_NotiMsgInfo.Builder msgInfo = LoginMessage.proto_NotiMsgInfo.newBuilder();
                msgInfo.setAutoid(sm.getAutoId());
                msgInfo.setSenderId(sm.getSendId());
                msgInfo.setMsgType(sm.getMessageType());
                msgInfo.setMsgShowType(sm.getShowMessageType());
                msgInfo.setMsgStatus(sm.getMessageStatus());

                msgInfo.setSendTime(sm.getSendTime());
                msgInfo.setClubId(sm.getClubId());

                msgInfo.setExpireTime(sm.getExpireTime());
                if (ro.getStatus() == LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD_VALUE) {
                    msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
                } else if (ro.getStatus() == LoginMessage.proto_NotiMsgInfo.STATUS.READ_VALUE) {
                    msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.READ);
                } else if (ro.getStatus() == LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD_VALUE) {
                    msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
                }
                // 多语言处理
                String jsonMsg = SendMsgUtil.parseMsgContent(sm.getMagicId(),JSON.toJSONString(sm.getMessageContext()),
                        player.getUserLanguage());
                msgInfo.setMsg(jsonMsg);
                msgInfo.setTitle(SendMsgUtil.getConfigString(String.format("title_%s", sm.getMagicId()),
                        player.getUserLanguage()));
                msgInfo.setRecieverId(ro.getId());
                msgInfoList.add(msgInfo.build());
            }
            response.addAllNotiMsgInfo(msgInfoList);
            SendMsgUtil.sendProtoPack(MessageEnum.LC_LOAD_NOTI_MSG_ACK.getOpCode(), response.build(), message.getPlyGuid());
        }
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
     * 处理修改消息状态
     * @param plyGuid
     * @param message
     */
    private void handleUpdateMsg(long plyGuid, LoginMessage.proto_cl_update_msg_status_req message) {
        List<Long> autoList = message.getAutoIdListList();

        int cond_status = MsgConstants.MSG_STATUS_UNREAD;
        if (MsgConstants.MSG_STATUS_READ == message.getStatusValue()) {
            cond_status = MsgConstants.MSG_STATUS_UNREAD;
        } else if (MsgConstants.MSG_STATUS_DELETED == message.getStatusValue()) {
            cond_status = MsgConstants.MSG_STATUS_READ;
        } else {
            log.info("UpdateMsgStatus ply_guid:%lld invalid status:%d", plyGuid, message.getStatus());
            return;
        }

        //修改mongo状态
        systemMessageDao.updateReceiveStatus(autoList,plyGuid, message.getStatusValue());

        // 回执
        LoginMessage.proto_lc_update_msg_status_ack response = LoginMessage
                .proto_lc_update_msg_status_ack.newBuilder()
                .setRet(0).setErrMsg("").build();
        SendMsgUtil.sendProtoPack(MessageEnum.LC_UPDATE_MSG_STATUS_ACK.getOpCode(), response, plyGuid);
    }

    /**
     *
     * @param plyGuid
     * @param message
     */
    private void handleGetMsgCount(long plyGuid, LoginMessage.proto_cl_get_msg_count_req message) {
        int clubId = -1;
        if (message.getClubId() > 0) {
            clubId = message.getClubId();
        }
            
        // 查找数目
        List<ClubMsgCount> list = systemMessageDao.getMsgCount(plyGuid, clubId);
        if (list == null || list.size() <= 0) {
            return;
        }

        // 回执
        LoginMessage.proto_lc_get_msg_count_ack.Builder response = LoginMessage
                .proto_lc_get_msg_count_ack.newBuilder();

        List<LoginMessage.proto_lc_get_msg_count_ack.Result> results = new ArrayList<>();

         for (ClubMsgCount clubMsgCount: list) {
             LoginMessage.proto_lc_get_msg_count_ack.Result.Builder result= LoginMessage
                     .proto_lc_get_msg_count_ack.Result.newBuilder();
             result.setClubId(clubMsgCount.getClubId());
             result.setCount(clubMsgCount.getCount());
             results.add(result.build());
         }

        response.addAllResultSet(results);
        // 发送协议
        SendMsgUtil.sendProtoPack(MessageEnum.LC_GET_MSG_COUNT_ACK.getOpCode(), response.build(), plyGuid);
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
