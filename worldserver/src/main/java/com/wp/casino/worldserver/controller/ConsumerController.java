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
        msgBody.setMsgContent("hahahahahah");
        msgBody.setClubId(1);
        msgBody.setMsgRstId("ios.sss");

        msg.setMsgType(1);
        msg.setMsgData(msgBody.build().toByteString());
        ChannelHandlerContext context = HandlerWorldContext.getInstance().getChannel("9832");
        context.writeAndFlush(msg.build());
        return "send ok";
    }
}
