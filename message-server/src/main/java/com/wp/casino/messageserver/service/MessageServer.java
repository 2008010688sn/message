package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
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
import com.wp.casino.messageserver.domain.mysql.casino.MessageUserData;
import com.wp.casino.messageserver.domain.mysql.casino.PyqClubMembers;
import com.wp.casino.messageserver.utils.*;
import com.wp.casino.messagetools.monitor.ThreadPoolManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.World;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

    private ThreadPoolManager threadPoolManager;


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
        this.threadPoolManager=new ThreadPoolManager();
    }

    public MessageServer(String host,int port) {
        super(port, host);
        this.host=host;
        this.port=port;
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new MessageServerHandler(messageDispatcher);
        this.systemMessageDao=ApplicationContextProvider.getApplicationContext()
                .getBean(SystemMessageDao.class);
        this.threadPoolManager=new ThreadPoolManager();

    }

    @Override
    public void start(Listener listener) {
        super.start(listener);
        if (this.workerGroup != null) {// 增加线程池监控
            this.threadPoolManager.register("server-conn-worker", this.workerGroup);
        }
    }

    @Override
    public void init() {
        super.init();

        //客户端发送给消息服务器的消息包装(login原封不动的转发)
        //proto_cl_load_noti_msg_req
        //proto_cl_update_msg_status_req
        //proto_cl_get_msg_count_req
        messageDispatcher.registerHandler(WorldMessage.prt_ping.class,(channel,message)->{
            log.info("接收login的ping------");
            LocalDate localDate = LocalDate.now();
            Timestamp timestamp= Timestamp.valueOf(LocalDateTime.now());
            int nowTime=(int)(timestamp.getTime()/1000);
            WorldMessage.prt_pong pong=WorldMessage.prt_pong
                    .newBuilder().setNowTime(nowTime).build();
            channel.writeAndFlush(pong);
        });
        messageDispatcher.registerHandler(LoginMessage.proto_cf_message_wrap_sync.class,
                (channel, message) -> {
                    log.info("proto_cf_message_wrap_sync----callback");
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
                    } else if (opcode == MessageEnum.CF_ADD_CLUB_CHAT_RECORD_REQ.getOpCode()) {
                        handleAddChart(message.getPlyGuid(), (LoginMessage.proto_cf_add_club_chat_record_req)parser.parseFrom(data.toByteArray()));
                    } else if (opcode == MessageEnum.CF_SYNC_CLUB_CHAT_RECORD_REQ.getOpCode()) {
                        handleSyncChart(message.getPlyGuid(), (LoginMessage.proto_cf_sync_club_chat_record_req)parser.parseFrom(data.toByteArray()));
                    }
                });

        //Login去Message注册,
        // message回复login注册结果
        messageDispatcher.registerHandler(LoginMessage.proto_lf_register_req.class,
                (channel, message)->{
                    log.info("proto_lf_register_req------:{}", message.toString());
                    //维护channel
                    String serverId = String.valueOf(message.getServerId());
                    HandlerContext.getInstance().addChannel(serverId,channel);

                    //Message回复Login注册结果
                    LoginMessage.proto_fl_register_ack response=LoginMessage.proto_fl_register_ack
                            .newBuilder().setRet(0).build();
                    channel.writeAndFlush(response);
                });

        //Login所有连接到它的玩家信息告知Message;
        messageDispatcher.registerHandler(LoginMessage.proto_lf_update_ply_login_status_not.class,
                (channel,message)->{
                    log.info("proto_lf_update_ply_login_status_not------:{}", message.toString());
                    //用户注册,登录用户信息入表
                    // 修改用户
                    Integer result = ClubDataUtil.updateMessageUserDataLating(message.getNickName(), message.getPlyVip(),
                            message.getPlyLevel(), Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)), message.getPlyGuid());
                    if (result == 0) {
                        // 没有更新到，则就插入
                        MessageUserData messageUserData = new MessageUserData();
                        messageUserData.setMdPlyGuid(message.getPlyGuid());
                        messageUserData.setMdApproveNoti(0);
                        messageUserData.setMdFriendLimit(20);
                        messageUserData.setMdLevel(message.getPlyLevel());
                        messageUserData.setMdLoginTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
                        messageUserData.setMdNickname(message.getNickName());
                        messageUserData.setMdSystemAutoId(0);
                        messageUserData.setMdVip(message.getPlyVip());
                        ClubDataUtil.saveMessageUserData(messageUserData);
                    }
                    log.info("Login注册用户信息协议---修改用户result:{}",result);
                    //维护用户和地址
                    Map<String, Object> map = ClubDataUtil.loadPlyData(message.getPlyGuid());
                    String face=map.get("face").toString();
                    int approveNoti=(Integer) map.get("approveNoti");
                    int friendLimit=(Integer)map.get("friendLimit");
                    int friendNum=(Integer)map.get("friendNum");

                    LoginPlayer loginPlayer = new LoginPlayer();
                    loginPlayer.setHeadImg(face);
                    loginPlayer.setNickName(message.getNickName());
                    loginPlayer.setPlyGuid(message.getPlyGuid());
                    loginPlayer.setPlyLevel(message.getPlyLevel());
                    loginPlayer.setUserLanguage(message.getUserLanguage());
                    loginPlayer.setPlyVip(message.getPlyVip());
                    loginPlayer.setServerId("1");
                    loginPlayer.setApproveNoti(approveNoti);
                    loginPlayer.setFriendLimit(friendLimit);
                    loginPlayer.setFriendNum(friendNum);
                    log.info("Login注册用户信息协议---loginPlayer:{}",loginPlayer);
                    HandlerServerContext.getInstance().addChannel(message.getPlyGuid(), loginPlayer);
                });

        // 玩家下线移除关系
        messageDispatcher.registerHandler(LoginMessage.proto_lf_update_ply_logout_status_not.class,
                (channel, message) -> {
                    HandlerServerContext.getInstance().removeChannel(message.getPlyGuid());
                });

        //申请加入俱乐部
        messageDispatcher.registerHandler(LoginMessage.proto_lf_club_apply_join_noti.class,(channel, message) -> {
            log.info("proto_lf_club_apply_join_noti------:{}", message.toString());

            // 查找改天申请俱乐部次数是否达到三次
            long nowTime =System.currentTimeMillis();
            // 当前0点时间戳
            long todayStartTime =nowTime - ((nowTime + TimeZone.getDefault().getRawOffset()) % (24 * 60 * 60 * 1000L));
            Integer count  = systemMessageDao.findApplyClubCount(message.getApplyPlyGuid(), message.getClubId(),
                    MsgType.CLUB_NOTI_MSG.getMsgType(), MsgContentType.ACK_MSG.getMsgContentType(),
                    Integer.valueOf(String.valueOf(todayStartTime/1000)));

            LoginPlayer loginPlayer = HandlerServerContext.getInstance().getChannel(message.getApplyPlyGuid());
            if (count >= 3) {
                if (loginPlayer == null || StringUtils.isBlank(loginPlayer.getServerId())) {
                    log.info("ply_guid:{} offline", message.getApplyPlyGuid());
                    return;
                }
                // 错误信息
                LoginMessage.proto_lc_club_apply_join_ack.Builder builder = LoginMessage.proto_lc_club_apply_join_ack.newBuilder();
                builder.setRet(100401);
                builder.setErrMsg(SendMsgUtil.getErrorMsg(loginPlayer.getUserLanguage(), 100401));
                SendMsgUtil.sendProtoPack(MessageEnum.LC_CLUB_APPLY_JOIN_ACK.getOpCode(), builder.build(), message.getApplyPlyGuid());
                return;
            }

            // 入表
            ClubMessageContext cmc = new ClubMessageContext();
            cmc.setClubid(message.getClubId());
            cmc.setNickname(message.getApplyPlyName());
            cmc.setClubname(message.getClubName());
            cmc.setPlyid(message.getApplyPlyGuid());
            cmc.setText("join req");
            cmc.setCode(0);
            cmc.setContent("");

            //所有管理员
            List<PyqClubMembers> members = ClubDataUtil.getClubAdminList(message.getClubId());
            List<ReceiveObj> list = new ArrayList<>();
            int ret = 100400; // 推荐人无加入权限
            // 如果指定了推荐人，则判断是否有同意加入俱乐部权限
            if (message.getReferrerGuid() > 0 ) {
                for (PyqClubMembers pcm: members) {
                    if (pcm.getCmPlyGuid() == message.getReferrerGuid() ) {
                        //推荐人拥有该权限
                        ret = 0;
                        ReceiveObj receiveObj = new ReceiveObj();
                        receiveObj.setId(pcm.getCmPlyGuid());
                        receiveObj.setStatus(0);
                        list.add(receiveObj);
                        break;
                    }
                }
            } else {
                ret = 0;
                for (PyqClubMembers pcm: members) {
                    ReceiveObj receiveObj = new ReceiveObj();
                    receiveObj.setId(pcm.getCmPlyGuid());
                    receiveObj.setStatus(0);
                    list.add(receiveObj);
                }
            }

            if (!list.isEmpty()) {
                Integer expireTime = Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000))
                        + MsgConstants.CLUB_REQUEST_APPLY_EXPIRE_TIME;
                SystemMessage sm = save2Mongo (message.getApplyPlyGuid(), list, MsgType.CLUB_NOTI_MSG.getMsgType(),
                        MsgContentType.ACK_MSG.getMsgContentType(), cmc, MsgConstants.SENDED,
                        message.getClubId(), MagicId.APPLY_JOIN_CLUB_MSG.getMagicId(), expireTime);

                // 转发协议到login
                SendMsgUtil.sendNotiMsg(sm.getAutoId(), sm.getSendId(), list,MsgType.CLUB_NOTI_MSG.getMsgType(),
                        MsgContentType.ACK_MSG.getMsgContentType(), MsgConstants.SENDED, message.getClubId(),
                        JSONObject.toJSONString(cmc),
                        Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000 + (24 * 60 * 60))),
                        sm.getMagicId());
            }

            // 返回信息给申请人
            LoginMessage.proto_lc_club_apply_join_ack.Builder builder = LoginMessage.proto_lc_club_apply_join_ack.newBuilder();
            builder.setRet(ret);
            String errMsg = ret == 0 ? "" : SendMsgUtil.getErrorMsg(loginPlayer.getUserLanguage(), ret);
            builder.setErrMsg(errMsg);
            SendMsgUtil.sendProtoPack(MessageEnum.LC_CLUB_APPLY_JOIN_ACK.getOpCode(), builder.build(), message.getApplyPlyGuid());
        });

        // 回复加入房间通知
        messageDispatcher.registerHandler(LoginMessage.proto_lf_join_room_reply_noti.class,(channel, message) -> {
            log.info("proto_lf_join_room_reply_noti------:{}", message.toString());

            LoginPlayer player = HandlerServerContext.getInstance().getChannel(message.getPlyGuid());
            if (player == null || StringUtils.isBlank(player.getServerId())) {
                log.error("warning AddRoomAckNoti ply_guid:{} offline", message.getPlyGuid());
                return;
            }

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
            jsonObject.put("code", message.getRet() == 1 ? MsgConstants.MSG_REPLY_CODE_OK : MsgConstants.MSG_REPLY_CODE_NO);
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
            systemMessageDao.updateMsgCode(message.getMessageId(), message.getRet() == 1 ?
                    MsgConstants.MSG_REPLY_CODE_OK : MsgConstants.MSG_REPLY_CODE_NO, message.getOwnerGuid());
        });
    }

    /**
     *
     * @param plyGuid
     * @param message
     */
    private void handleSyncChart(long plyGuid, LoginMessage.proto_cf_sync_club_chat_record_req message) {

        log.info("proto_cf_sync_club_chat_record_req------:{}", message.toString());

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

        //PYQ_SYNC_CLUB_CHAT_RECORD
        LoginMessage.proto_fc_sync_club_chat_record_ack.Builder ackBuilder = LoginMessage.
                proto_fc_sync_club_chat_record_ack.newBuilder();
        ackBuilder.setRet(0);
        ackBuilder.setErrMsg("");
        ackBuilder.setPlyGuid(message.getPlyGuid());

        for (ClubChatInfo clubChatInfo: syncClubChatRecords) {
            LoginMessage.proto_ClubChatRecordInfoStruct.Builder recordInfo = LoginMessage.proto_ClubChatRecordInfoStruct.newBuilder();
            recordInfo.setAutoId(clubChatInfo.getClAutoId());
            recordInfo.setPlyId(clubChatInfo.getClMemberUid());
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
    }

    /**
     *
     * @param plyGuid
     * @param message
     */
    private void handleAddChart(long plyGuid, LoginMessage.proto_cf_add_club_chat_record_req message) {
        log.info("proto_cf_add_club_chat_record_req------:{}", message.toString());
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
        recordInfo.setPlyId(clubChatInfo.getClMemberUid());
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
    }

    /**
     * 处理拉取消息
     */
    private void handleLoadNotiMsg(long plyGuid, LoginMessage.proto_cl_load_noti_msg_req message) {
        log.info("handleLoadNotiMsg------proto_cl_load_noti_msg_req:{}", message.toString());
        //拉取消息记录
        List<SystemMessage> list = systemMessageDao.findNotiMsg(message.getPlyGuid(), message.getType(),
                message.getClubId(), message.getAutoId(), message.getMaxCount());
        // 封装消息回执
        LoginMessage.proto_lc_load_noti_msg_ack.Builder response = LoginMessage
                .proto_lc_load_noti_msg_ack.newBuilder();
        List<LoginMessage.proto_NotiMsgInfo> msgInfoList = new ArrayList<>();

        LoginPlayer player = HandlerServerContext.getInstance().getChannel(message.getPlyGuid());
        if (player == null || StringUtils.isBlank(player.getServerId())) {
            log.info("ply_guid:{} offline", message.getPlyGuid());
            return;
        }

        for (SystemMessage sm: list) {
            LoginMessage.proto_NotiMsgInfo.Builder msgInfo = LoginMessage.proto_NotiMsgInfo.newBuilder();
            msgInfo.setAutoid(sm.getAutoId());
            msgInfo.setSenderId(sm.getSendId());
            msgInfo.setMsgType(sm.getMessageType());
            msgInfo.setMsgShowType(sm.getShowMessageType());
            msgInfo.setMsgStatus(sm.getMessageStatus());

            msgInfo.setSendTime(sm.getSendTime());
            msgInfo.setClubId(sm.getClubId());

            msgInfo.setExpireTime(sm.getExpireTime());

            for (Object obj : sm.getReceiveObjList()) {
                ReceiveObj ro = (ReceiveObj) obj;
                if (plyGuid != ro.getId()) {
                    continue;
                }
                if (ro.getStatus() == LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD_VALUE) {
                    msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
                } else if (ro.getStatus() == LoginMessage.proto_NotiMsgInfo.STATUS.READ_VALUE) {
                    msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.READ);
                }
            }
            // 多语言处理
            String jsonMsg = SendMsgUtil.parseMsgContent(sm.getMagicId(),JSON.toJSONString(sm.getMessageContext()),
                    player.getUserLanguage());
            msgInfo.setMsg(jsonMsg);
            msgInfo.setTitle(SendMsgUtil.getConfigString(sm.getTitle(),
                    player.getUserLanguage()));
            msgInfo.setRecieverId(message.getPlyGuid());
            msgInfoList.add(msgInfo.build());
        }
        log.info("list:{}", JSONArray.toJSONString(list));
        log.info("msgInfoList--size:{}", msgInfoList.size());
        response.addAllNotiMsgInfo(msgInfoList);
        SendMsgUtil.sendProtoPack(MessageEnum.LC_LOAD_NOTI_MSG_ACK.getOpCode(), response.build(), message.getPlyGuid());
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

        log.info("handleUpdateMsg------proto_cl_update_msg_status_req:{}", message.toString());
        List<Long> autoList = message.getAutoIdListList();
        //修改mongo状态
        systemMessageDao.updateReceiveStatus(autoList,plyGuid, message.getStatus().getNumber());

        LoginPlayer player = HandlerServerContext.getInstance().getChannel(plyGuid);
        if (player == null || StringUtils.isBlank(player.getServerId())) {
        log.error("plyGuid:{}, offline,proto_lc_update_msg_status_ack fail", plyGuid);
            return;
        }

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

        log.info("handleGetMsgCount------proto_cl_get_msg_count_req:{}", message.toString());

        int clubId = -1;
        if (message.getClubId() > 0) {
            clubId = message.getClubId();
        }

        LoginPlayer player = HandlerServerContext.getInstance().getChannel(plyGuid);
        if (player == null || StringUtils.isBlank(player.getServerId())) {
            log.error("plyGuid:{}, offline,proto_lc_get_msg_count_ack fail", plyGuid);
            return;
        }

        // 查找数目
        List<ClubMsgCount> list = systemMessageDao.getMsgCount(plyGuid, clubId);
        if (list == null || list.size() <= 0) {
            log.error("ClubMsgCount list is null");
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
