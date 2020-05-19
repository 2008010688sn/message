package com.wp.casino.messagenetty.deec;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import io.netty.buffer.ByteBuf;
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
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out) throws Exception {
        //先获取消息对应的opCode编号
        int openCode = MessageMappingHolder.getopCode(msg);
        byte[] opcodeBytes=intToBytes(openCode);
        byte[] body = msg.toByteArray();
        byte[] resultBody=byteMergerAll(opcodeBytes,body);//合并后的消息体
        short msgLength=(short)resultBody.length;

        out.writeShort(msgLength);//消息的长度
        out.writeInt(2);//校验位
//            out.writeInt(openCode);//opcode
        out.writeBytes(resultBody);//消息体
        return;
    }
    //int 转为字节数组
    public static byte[] intToBytes(int value )
    {
        byte[] src = new byte[4];
        src[0] =  (byte) (value & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[3] =  (byte) ((value>>24) & 0xFF);
        return src;
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
