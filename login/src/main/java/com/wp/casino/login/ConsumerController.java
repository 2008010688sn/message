package com.wp.casino.login;

import com.wp.casino.login.utils.HandlerLoginContext;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.proto.WorldMessage;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pjmike
 * @create 2018-10-24 16:47
 */
@RestController
public class ConsumerController {

    @GetMapping("/join/apply")
    public String joinRoomNotify() {
        LoginMessage.proto_lf_join_room_reply_noti.Builder msgBody = LoginMessage
                .proto_lf_join_room_reply_noti.newBuilder();

        msgBody.setRet(1);
        msgBody.setOwnerGuid(777);
        msgBody.setMessageId(28);
        msgBody.setPlyGuid(10000353);
        msgBody.setTableName("测试桌号");


        ChannelHandlerContext context = HandlerLoginContext.getInstance().getChannel("1111");
        context.writeAndFlush(msgBody.build());
        return "send ok";
    }

    /**
     * 申请加入俱乐部
     * @return
     */
    @GetMapping("/join/club")
    public String joinClubNotify() {
        LoginMessage.proto_lf_club_apply_join_noti.Builder msgBody = LoginMessage
                .proto_lf_club_apply_join_noti.newBuilder();

        msgBody.setClubId(123);
        msgBody.setClubName("天天向上");
        msgBody.setApplyPlyGuid(777);
        msgBody.setApplyPlyName("测试777");
        msgBody.setReferrerGuid(0);

        ChannelHandlerContext context = HandlerLoginContext.getInstance().getChannel("1111");
        context.writeAndFlush(msgBody.build());
        return "send ok";
    }

    @GetMapping("/load/msg")
    public String loadMsg() {

        LoginMessage.proto_cf_message_wrap_sync.Builder msg = LoginMessage
                .proto_cf_message_wrap_sync.newBuilder();

        LoginMessage.proto_cl_load_noti_msg_req.Builder msgBody = LoginMessage
                .proto_cl_load_noti_msg_req.newBuilder();

        msgBody.setAutoId(500);
        msgBody.setClubId(123);
        msgBody.setMaxCount(500);
        msgBody.setPlyGuid(10000025);
        msgBody.setType(2);

        msg.setData(msgBody.build().toByteString());
        msg.setOpcode(20163);
        msg.setPlyGuid(10000025);

        ChannelHandlerContext context = HandlerLoginContext.getInstance().getChannel("1111");
        context.writeAndFlush(msg.build());
        return "send ok";
    }

    @GetMapping("/get/msg/count")
    public String getMsgCount() {

        LoginMessage.proto_cf_message_wrap_sync.Builder msg = LoginMessage
                .proto_cf_message_wrap_sync.newBuilder();

        LoginMessage.proto_cl_get_msg_count_req.Builder msgBody = LoginMessage
                .proto_cl_get_msg_count_req.newBuilder();

        msgBody.setClubId(-1);

        msg.setData(msgBody.build().toByteString());
        msg.setOpcode(20181);
        msg.setPlyGuid(10000025);

        ChannelHandlerContext context = HandlerLoginContext.getInstance().getChannel("1111");
        context.writeAndFlush(msg.build());
        return "send ok";
    }

    @GetMapping("/update/msg")
    public String updateMsg() {

        LoginMessage.proto_cf_message_wrap_sync.Builder msg = LoginMessage
                .proto_cf_message_wrap_sync.newBuilder();

        LoginMessage.proto_cl_update_msg_status_req.Builder msgBody = LoginMessage
                .proto_cl_update_msg_status_req.newBuilder();

        msgBody.setStatus(LoginMessage.proto_cl_update_msg_status_req.STATUS.READ);
        List<Long> list = new ArrayList<>();
        list.add(32L);
        msgBody.addAllAutoIdList(list);

        msg.setData(msgBody.build().toByteString());
        msg.setOpcode(20179);
        msg.setPlyGuid(10000025);

        ChannelHandlerContext context = HandlerLoginContext.getInstance().getChannel("1111");
        context.writeAndFlush(msg.build());
        return "send ok";
    }
}
