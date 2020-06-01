package com.wp.casino.messageserver.service;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.common.MagicId;
import com.wp.casino.messageserver.common.MsgContentType;
import com.wp.casino.messageserver.common.MsgType;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.domain.ClubMessageContext;
import com.wp.casino.messageserver.domain.ReceiveObj;
import com.wp.casino.messageserver.domain.SystemMessage;
import com.wp.casino.messageserver.domain.mysql.casino.PyqClubMembers;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import com.wp.casino.messageserver.utils.ClubDataUtil;
import com.wp.casino.messageserver.utils.HandlerContext;
import com.wp.casino.messageserver.utils.HandlerServerContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        //消息拉取
        messageDispatcher.registerHandler(LoginMessage.proto_cl_load_noti_msg_req.class,
                (channel, message) -> {
            //拉取消息记录
            long plyguid = message.getPlyGuid();
            int type = message.getType();
            long autoid = message.getAutoId();
            int clubId = message.getClubId();
            int limit = message.getMaxCount();
            Query query = new Query();



            // 根据条件查询所有消息
            List<SystemMessage> list = systemMessageDao.find(query);


            // 封装公共回执
            LoginMessage.proto_fc_message_wrap_sync.Builder commRes = LoginMessage.
                    proto_fc_message_wrap_sync.newBuilder();
            // 消息包装
            LoginMessage.proto_lc_load_noti_msg_ack.Builder response = LoginMessage
                    .proto_lc_load_noti_msg_ack.newBuilder();
            // 消息体
            LoginMessage.proto_NotiMsgInfo.Builder notiMsg = LoginMessage.proto_NotiMsgInfo.newBuilder();
            //response.setNotiMsgInfo(notiMsg);
            //response.setAutoId();

            commRes.setOpcode(MessageEnum.CL_LOAD_NOTI_MSG_REQ.getOpCode());
            commRes.setData(response.build().toByteString());
            //commRes.setPlyGuid();

            channel.writeAndFlush(response.build());
        });

        //修改消息的状态
        messageDispatcher.registerHandler(LoginMessage.proto_cl_update_msg_status_req.class,
                (channel, message) -> {
            List<Long> autoList = message.getAutoIdListList();

            //修改mongo状态
            Query query = new Query();
            Criteria criteria = Criteria.where("_auto_id").in(autoList)
                    .and("cm_reciver_ids.$.id").is(111)
                    .and("cm_reciver_ids.$.status").lt(message.getStatusValue());
            query.addCriteria(criteria);

            Update update = new Update().set("cm_reciver_ids.$.status", message.getStatusValue());
            systemMessageDao.update(query, update);

            // 回执
            LoginMessage.proto_lc_update_msg_status_ack response = LoginMessage
                    .proto_lc_update_msg_status_ack.newBuilder()
                    .setRet(0).setErrMsg("").build();
            sendProtoPack(MessageEnum.LC_UPDATE_MSG_STATUS_ACK.getOpCode(), response, 1);

        });

        // 获取未读消息数目
        messageDispatcher.registerHandler(LoginMessage.proto_cl_get_msg_count_req.class,
            (channel, message) -> {
            // 查找数目

            // 回执
            LoginMessage.proto_lc_get_msg_count_ack response = LoginMessage
                    .proto_lc_get_msg_count_ack.newBuilder()
                    .build();
            sendProtoPack(MessageEnum.LC_GET_MSG_COUNT_ACK.getOpCode(), response, 1);
        });

        //Login去Message注册,
        // message回复login注册结果
        messageDispatcher.registerHandler(LoginMessage.proto_lf_register_req.class,
                (channel, message)->{
            //维护channel
            String channelId=channel.remoteAddress().toString();
            HandlerContext.getInstance().addChannel(channelId,channel);
            //Message回复Login注册结果
            LoginMessage.proto_fl_register_ack response=LoginMessage.proto_fl_register_ack
                    .newBuilder().setRet(1).build();
            channel.writeAndFlush(response);
        });

        //Login所有连接到它的玩家信息告知Message;
        messageDispatcher.registerHandler(LoginMessage.proto_lf_update_ply_login_status_not.class,
                (channel,message)->{
            // 用户注册，维护用户和地址
            String channelId=channel.remoteAddress().toString();
            HandlerServerContext.getInstance().addChannel(message.getPlyGuid(), channelId);
            //mesage的回复
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

            //一天最多发送三次申请加入俱乐部的请求

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
            for (ReceiveObj receiveObj: list) {
                sendNotiMsg(sm.getAutoId(), sm.getSendId(), receiveObj.getId(),
                        MsgType.PLAYER_NOTI_MSG.getMsgType(), MsgContentType.TEXT_MSG.getMsgContentType(), 0, 0,
                        JSONObject.toJSONString(cmc), Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000 + (24 * 60 * 60))),
                        sm.getMagicId());
            }
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
            sendNotiMsg(0, message.getOwnerGuid(), message.getPlyGuid(),
                    MsgType.PLAYER_NOTI_MSG.getMsgType(), MsgContentType.TEXT_MSG.getMsgContentType(), 0, 0,
                    jsonObject.toJSONString(), -1 , magicId);

            // 修改申请加入房间消息的code
            updateMsgCode(message.getMessageId(), (message.getRet() == 1 ? 1 : 0), message.getOwnerGuid());
        });


        //获取俱乐部聊天记录-
        messageDispatcher.registerHandler(LoginMessage.proto_cf_add_club_chat_record_req.class,
                (channel, message) -> {
            // 拉取聊天记录

            // 封装回执
            LoginMessage.proto_fc_add_club_chat_record_ack response = LoginMessage
                    .proto_fc_add_club_chat_record_ack.newBuilder()
                    .build();
            channel.writeAndFlush(response);
        });

        // 俱乐部聊天
        messageDispatcher.registerHandler(LoginMessage.proto_cf_sync_club_chat_record_req.class,
                (channel, message) -> {


            // 回执
            LoginMessage.proto_fc_sync_club_chat_record_ack response = LoginMessage
                    .proto_fc_sync_club_chat_record_ack
                    .newBuilder().build();
            channel.writeAndFlush(response);

        });







//        Message回给Login，让Login转发给客户端的
        messageDispatcher.registerHandler(LoginMessage.proto_fl_noti_msg.class,
                (channel,message)->{



        });

        //proto_ww_user_data_change_req协议
        messageDispatcher.registerHandler(WorldMessage.proto_wl_noti_msg_data.class,
                (channel, message) -> {
            log.info("服务端处理 proto_ww_user_data_change_req proto .start ");
            log.info("处理时的channelid------",channel.id().asLongText());
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
//            SystemMessage re=ApplicationContextProvider.getApplicationContext()
//            .getBean(SystemMessageDao.class).save(sm);
//            log.info("----re:"+re.getId());
            //Message回给Login，让Login转发给客户端的


//            Long size= MessageQueue.getSize();
//            if (size!=null&&size>0){
//                for (MessageLite msg: MessageQueue.getAll()) {
//
//                }
//            }
        });
    }
    /**
     * 处理拉取消息
     */
    private void handleLoadNotiMsg(long plyGuid, LoginMessage.proto_cl_load_noti_msg_req message) {

        //拉取消息记录
        int type = message.getType();
        long autoid = message.getAutoId();
        int clubId = message.getClubId();
        int limit = message.getMaxCount();
        Query query = new Query();



        // 根据条件查询所有消息
        List<SystemMessage> list = systemMessageDao.find(query);


        // 封装公共回执
        LoginMessage.proto_fc_message_wrap_sync.Builder commRes = LoginMessage.
                proto_fc_message_wrap_sync.newBuilder();
        // 消息包装
        LoginMessage.proto_lc_load_noti_msg_ack.Builder response = LoginMessage
                .proto_lc_load_noti_msg_ack.newBuilder();
        // 消息体
        LoginMessage.proto_NotiMsgInfo.Builder notiMsg = LoginMessage.proto_NotiMsgInfo.newBuilder();
        //response.setNotiMsgInfo(notiMsg);
        //response.setAutoId();

        commRes.setOpcode(MessageEnum.CL_LOAD_NOTI_MSG_REQ.getOpCode());
        commRes.setData(response.build().toByteString());
        //commRes.setPlyGuid();

        // channel.writeAndFlush(response.build());
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
     * TODO
     * @param messageId
     * @param code
     * @param ownerGuid
     */
    private void updateMsgCode(long messageId, int code, long ownerGuid) {

    }

    /**
     *
     * @param autoId
     * @param sendId
     * @param recieverId
     * @param msgType
     * @param msgShowType
     * @param msgStatus
     * @param clubId
     * @param jsonStr
     * @param expireTime
     * @param magicId
     */
    private void sendNotiMsg(long autoId, long sendId, long recieverId, int msgType, int msgShowType,
                                                      int msgStatus, int clubId, String jsonStr, int expireTime, String magicId) {
        LoginMessage.proto_fl_noti_msg.Builder fnm = LoginMessage.proto_fl_noti_msg.newBuilder();

        LoginMessage.proto_NotiMsgInfo.Builder msgInfo = LoginMessage.proto_NotiMsgInfo.newBuilder();
        msgInfo.setAutoid(autoId);
        msgInfo.setSenderId(sendId);
        msgInfo.setRecieverId(recieverId);
        msgInfo.setMsgType(msgType);
        msgInfo.setMsgShowType(msgShowType);
        msgInfo.setMsgStatus(msgStatus);

        msgInfo.setSendTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
        msgInfo.setClubId(clubId);
        msgInfo.setMsg(jsonStr);
        msgInfo.setExpireTime(expireTime);
        msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);
        msgInfo.setTitle(String.format("title_%s", magicId));

        List<LoginMessage.proto_NotiMsgInfo> list = new ArrayList<>();
        list.add(msgInfo.build());
        fnm.addAllNotiMsgInfo(list);

        // 第二次封装 统一回执
        sendProtoPack(MessageEnum.FL_NOTI_MSG.getOpCode(), fnm.build(), recieverId);
    }



    /**
     * 处理修改消息状态
     * @param plyGuid
     * @param message
     */
    private void handleUpdateMsg(long plyGuid, LoginMessage.proto_cl_update_msg_status_req message) {
        List<Long> autoList = message.getAutoIdListList();

        //修改mongo状态
        Query query = new Query();
        Criteria criteria = Criteria.where("_auto_id").in(autoList)
                .and("cm_reciver_ids.$.id").is(111)
                .and("cm_reciver_ids.$.status").lt(message.getStatusValue());
        query.addCriteria(criteria);

        Update update = new Update().set("cm_reciver_ids.$.status", message.getStatusValue());
        systemMessageDao.update(query, update);

        // 回执
        LoginMessage.proto_lc_update_msg_status_ack response = LoginMessage
                .proto_lc_update_msg_status_ack.newBuilder()
                .setRet(0).setErrMsg("").build();
        sendProtoPack(MessageEnum.LC_UPDATE_MSG_STATUS_ACK.getOpCode(), response, 1);

    }

    /**
     *
     * @param uid
     * @param message
     */
     private void handleGetMsgCount(long uid, LoginMessage.proto_cl_get_msg_count_req message) {
        // 查找数目

        // 回执
        LoginMessage.proto_lc_get_msg_count_ack response = LoginMessage
                .proto_lc_get_msg_count_ack.newBuilder()
                .build();
        sendProtoPack(MessageEnum.LC_GET_MSG_COUNT_ACK.getOpCode(), response, 1);

    }

    /**
     * 最终结果封装
     * @param opcode
     * @param messageLite
     * @param plyGuid
     */
    private void sendProtoPack(int opcode, MessageLite messageLite, long plyGuid) {
        LoginMessage.proto_fc_message_wrap_sync.Builder resultResp = LoginMessage.proto_fc_message_wrap_sync.newBuilder();
        resultResp.setData(messageLite.toByteString());
        resultResp.setOpcode(opcode);
        resultResp.setPlyGuid(plyGuid);

        // 发送最终协议  TODO

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
