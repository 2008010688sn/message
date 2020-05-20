package com.wp.casino.messagenetty.deec;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author sn
 * @date 2020/5/15 16:11
 */
@ChannelHandler.Sharable
public final class MessageEncoder extends MessageToByteEncoder<MessageLite> {
    public static final MessageEncoder INSTANCE=new MessageEncoder();
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf buf) throws Exception {
        //先获取消息对应的opCode编号
        int openCode = MessageMappingHolder.getopCode(msg);

//        ByteBuf buf = Unpooled.buffer(1024 * 40);
        byte[] body = msg.toByteArray();
        int bodyLen = body.length + 4;
        buf.writeShort(bodyLen);
        buf.writeShort(0);
        buf.writeByte(0);
        buf.writeByte(1606);
        buf.writeInt(openCode);
        buf.writeBytes(body);

        return;
    }
    //int 转为字节数组
    public static byte[] intToBytes(ByteBuf buf, int value)
    {
        buf.writeInt(value);
//        byte[] src = new byte[4];
//        src[3] =  (byte) (value & 0xFF);
//        src[2] =  (byte) ((value>>8) & 0xFF);
//        src[1] =  (byte) ((value>>16) & 0xFF);
//        src[0] =  (byte) ((value>>24) & 0xFF);
//        return src;
        return null;
    }

//合并字节数组
    private static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }
}
