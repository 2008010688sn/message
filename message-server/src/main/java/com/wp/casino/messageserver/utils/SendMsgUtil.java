package com.wp.casino.messageserver.utils;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.LoginMessage;
import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messageserver.domain.ReceiveObj;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SendMsgUtil {

    /**
     *
     * @param autoId
     * @param sendId
     * @param receiveObjs
     * @param msgType
     * @param msgShowType
     * @param msgStatus
     * @param clubId
     * @param jsonStr
     * @param expireTime
     * @param magicId
     */
    public static void sendNotiMsg(long autoId, long sendId, List<ReceiveObj> receiveObjs, int msgType, int msgShowType,
                             int msgStatus, int clubId, String jsonStr, int expireTime, String magicId) {
        LoginMessage.proto_fl_noti_msg.Builder fnm = LoginMessage.proto_fl_noti_msg.newBuilder();

        LoginMessage.proto_NotiMsgInfo.Builder msgInfo = LoginMessage.proto_NotiMsgInfo.newBuilder();
        msgInfo.setAutoid(autoId);
        msgInfo.setSenderId(sendId);
        msgInfo.setMsgType(msgType);
        msgInfo.setMsgShowType(msgShowType);
        msgInfo.setMsgStatus(msgStatus);
        msgInfo.setSendTime(Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)));
        msgInfo.setClubId(clubId);
        msgInfo.setMsg(jsonStr);
        msgInfo.setExpireTime(expireTime);
        msgInfo.setTitle(String.format("title_%s", magicId));

        // 根据接收人列表循环发送
        for (Object obj: receiveObjs) {
            ReceiveObj receiveObj = (ReceiveObj) obj;
            if (StringUtils.isBlank(HandlerServerContext.getInstance().getChannel(receiveObj.getId()))) {
                log.info("ply_guid:%lld offline, jsonStr:%s", receiveObj.getId(), jsonStr);
                continue;
            }
            msgInfo.setRecieverId(receiveObj.getId());
            msgInfo.setStatus(LoginMessage.proto_NotiMsgInfo.STATUS.UNREAD);

            List<LoginMessage.proto_NotiMsgInfo> notiMsg = new ArrayList<>();
            notiMsg.add(msgInfo.build());
            fnm.addAllNotiMsgInfo(notiMsg);

            // 协议最终封装并发送
            sendProtoPack(MessageEnum.FL_NOTI_MSG.getOpCode(), fnm.build(), receiveObj.getId());
        }
    }

    /**
     * 最终结果封装
     * @param opcode
     * @param messageLite
     * @param plyGuid
     */
    public static void sendProtoPack(int opcode, MessageLite messageLite, long plyGuid) {
        // 发送最终协议
        LoginMessage.proto_fc_message_wrap_sync.Builder resultResp = LoginMessage.proto_fc_message_wrap_sync.newBuilder();
        resultResp.setData(messageLite.toByteString());
        resultResp.setOpcode(opcode);
        resultResp.setPlyGuid(plyGuid);

        // 发送
        transferMessage(plyGuid, resultResp.build());
    }

    /**
     * 通道发送
     * @param recieverId
     * @param message
     */
    public static void transferMessage(long recieverId, LoginMessage.proto_fc_message_wrap_sync message) {
        if (recieverId == -1) {
            // 发送全部
        } else {
            String channelId = HandlerServerContext.getInstance().getChannel(recieverId);
            if (StringUtils.isNotBlank(channelId)) {
                Channel ch = HandlerContext.getInstance().getChannel(channelId);
                if (ch != null) {
                    ch.writeAndFlush(message);
                }
            }
        }
    }
}
