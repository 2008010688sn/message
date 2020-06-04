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

    @GetMapping("/break/club")
    public String breakClub() {
        WorldMessage.proto_wf_break_up_club_noti.Builder msg = WorldMessage
                .proto_wf_break_up_club_noti.newBuilder();

        msg.setClubId(123);
        msg.setClubName("天天向上");
        msg.setPlyGuid(10000353);
        msg.setPlyNickname("测试人员");

        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msg.build());
        return "send ok";
    }

    @GetMapping("/update/club/noti")
    public String updateClubNotify() {
        WorldMessage.proto_wf_club_member_update_noti.Builder msg = WorldMessage
                .proto_wf_club_member_update_noti.newBuilder();
        WorldMessage.ClubMemberUpdateInfo.Builder msgBody = WorldMessage
                .ClubMemberUpdateInfo.newBuilder();

        msgBody.setReason(WorldMessage.ClubMemberUpdateInfo.TYPE.KickOut);
        msgBody.setClubId(123);
        msgBody.setClubName("天天向上");
        msgBody.setPlyGuid(10000353);
        msgBody.setPlyNickname("我是测试");
        msgBody.setWhoGuid(888);
        msgBody.setMessageId(1);


        msg.setInfo(msgBody.build());
        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msg.build());
        return "send ok";
    }


    @GetMapping("/web/notify")
    public String webNotify() {
        WorldMessage.proto_wf_web_msg_noti.Builder msg = WorldMessage
                .proto_wf_web_msg_noti.newBuilder();
        WorldMessage.proto_wl_noti_msg_data.Builder msgBody = WorldMessage
                .proto_wl_noti_msg_data.newBuilder();

        msgBody.setRecieverId(10000353);
        msgBody.setSenderId(777);
        msgBody.setMsgType(2);
        msgBody.setShowMsgType(1);
        msgBody.setMsgContent("这是一条通知");
        msgBody.setClubId(123);
        msgBody.setMsgRstId("zuanshi_exchange_money2");

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
        msgBody.setTableName("测试桌");
        msgBody.setPlyGuid(10000353);
        msgBody.setServerId(111001);
        msgBody.setGameId(55);
        msgBody.setPlyNickname("我是测试");
        msgBody.setTableCreateTime(152152152);
        msgBody.setInviteCode(1);
        msgBody.setOwnerGuid(777);

        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msgBody.build());
        return "send ok";
    }


}
