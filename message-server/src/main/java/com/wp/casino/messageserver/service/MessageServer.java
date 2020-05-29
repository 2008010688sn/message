package com.wp.casino.messageserver.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messageapi.service.Listener;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.server.NettyTcpServer;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import com.wp.casino.messageserver.dao.mongodb.message.SystemMessageDao;
import com.wp.casino.messageserver.dao.mysql.ClubRepository;
import com.wp.casino.messageserver.domain.PyqClubMembers;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
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
            MessageLite messageLite = (MessageLite) parser.parseFrom(data.toByteArray());
            messageDispatcher.onMessage(channel, messageLite);
        });

        //消息通知请求
        messageDispatcher.registerHandler(LoginMessage.proto_cl_load_noti_msg_req.class,
                (channel, message) -> {
            //拉取消息记录
            long plyguid = message.getPlyGuid();
            int type = message.getType();
            long autoid = message.getAutoId();
            int clubId = message.getClubId();
            int maxCount = message.getMaxCount();

            //List<SystemMessageDao> list = systemMessageDao.findByPage()


            // 回执
            LoginMessage.proto_lc_load_noti_msg_ack.Builder response = LoginMessage
                    .proto_lc_load_noti_msg_ack.newBuilder();
            LoginMessage.proto_NotiMsgInfo.Builder notiMsg = LoginMessage.proto_NotiMsgInfo.newBuilder();
            response.setNotiMsgInfo(notiMsg);

            channel.writeAndFlush(response.build());
        });

        //修改消息的状态
        messageDispatcher.registerHandler(LoginMessage.proto_cl_update_msg_status_req.class,
                (channel, message) -> {
            //修改mongo状态
            Query query = new Query(Criteria.where("id").is(message.getAutoIdList()));
            Update update = new Update();
            update.set("ReceiveObj.0.status", message.getStatusValue());
            systemMessageDao.update(query,update);

            // 回执
            LoginMessage.proto_lc_update_msg_status_ack response = LoginMessage
                    .proto_lc_update_msg_status_ack.newBuilder()
                    .setRet(0).build();
            channel.writeAndFlush(response);

        });

        // 获取未读消息数目
        messageDispatcher.registerHandler(LoginMessage.proto_cl_get_msg_count_req.class,
            (channel, message) -> {
            // 查找数目

            // 回执
            LoginMessage.proto_lc_get_msg_count_ack response = LoginMessage
                    .proto_lc_get_msg_count_ack.newBuilder()
                    .build();
            channel.writeAndFlush(response);
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

        //俱乐部管理者接收成员申请消息
        messageDispatcher.registerHandler(LoginMessage.proto_lf_club_apply_join_noti.class,
                (channel, message) -> {
            // 入表

            // 统一回执
            //通过message.clubId 查询管理员Id


        });

        // 回复加入房间通知
        messageDispatcher.registerHandler(LoginMessage.proto_lf_join_room_reply_noti.class,
                (channel, message) -> {
            // 入表

            // 统一回执
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

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        pipeline.addLast(getChannelHandler());
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    /**
     * 根据clubId获取俱乐部创建者和管理员信息
     * @param clubId
     * @return
     */
    public List<PyqClubMembers> getClubAdminList(Integer clubId){
        ClubRepository clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubRepository.class);
        List<PyqClubMembers> clubAdminLlist = clubRepository.findClubAdmin(clubId);
        return clubAdminLlist;
    }
}
