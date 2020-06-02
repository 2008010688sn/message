package com.wp.casino.worldserver.controller;

import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.worldserver.utils.HandlerWorldContext;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pjmike
 * @create 2018-10-24 16:47
 */
@RestController
public class ConsumerController {

    @GetMapping("/web/notify")
    public String webNotify() {
        WorldMessage.proto_wf_web_msg_noti.Builder msg = WorldMessage
                .proto_wf_web_msg_noti.newBuilder();
        WorldMessage.proto_wl_noti_msg_data.Builder msgBody = WorldMessage
                .proto_wl_noti_msg_data.newBuilder();

        msgBody.setRecieverId(777);
        msgBody.setSenderId(222);
        msgBody.setMsgType(2);
        msgBody.setShowMsgType(1);
        msgBody.setMsgContent("好的");
        msgBody.setClubId(1);
        msgBody.setMsgRstId("club_give_fund_notice");

        msg.setMsgType(1);
        msg.setMsgData(msgBody.build().toByteString());
        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msg.build());
        return "send ok";
    }

    @GetMapping("/join/room")
    public String joinRoomNotify() {
        WorldMessage.proto_wf_join_room_noti.Builder msgBody = WorldMessage
                .proto_wf_join_room_noti.newBuilder();

        msgBody.setTableId(1);
        msgBody.setTableName("2");
        msgBody.setPlyGuid(777);
        msgBody.setServerId(1);
        msgBody.setGameId(0);
        msgBody.setPlyNickname("1");
        msgBody.setTableCreateTime(111);
        msgBody.setInviteCode(1);
        msgBody.setOwnerGuid(777);

        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msgBody.build());
        return "send ok";
    }
}
