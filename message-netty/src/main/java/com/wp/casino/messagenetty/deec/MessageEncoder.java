package com.wp.casino.messagenetty.deec;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author sn
 * @date 2020/5/15 16:11
 */
@Slf4j
@ChannelHandler.Sharable
public final class MessageEncoder extends MessageToByteEncoder<MessageLite> {
    public static final MessageEncoder INSTANCE=new MessageEncoder();
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf buf) throws Exception {
        log.info("Encode---satrt---");
        //先获取消息对应的opCode编号
        int openCode = MessageMappingHolder.getopCode(msg);
        log.info("encode---opcode:{}",openCode);
        log.info("encode---msg----{}", Arrays.toString(msg.toByteArray()));
//        ByteBuf buf = Unpooled.buffer(1024 * 40);
        byte[] body = msg.toByteArray();
        int msgLen = body.length + 4;

        byte pData[] = new byte[msgLen];
        pData[0] = (byte) ((openCode >> 24) & 0xFF);
        pData[1] = (byte) ((openCode >> 16) & 0xFF);
        pData[2] = (byte) ((openCode >> 8) & 0xFF);
        pData[3] = (byte) (openCode & 0xFF);

        System.arraycopy(body, 0, pData, 4, msgLen - 4);

        byte crcVerify = 0;
        int verfiyCode = msgLen ^ 1606;
        for (int i = 0; i < msgLen; ++i) {
            crcVerify += (pData[i]);
        }

        buf.writeShort(msgLen);
        buf.writeShort(verfiyCode);
        buf.writeByte(0);
        buf.writeByte(crcVerify);
        buf.writeInt(openCode);
        buf.writeBytes(body);

        return;

//        try {
//
//            int len = 4 + msg.getSerializedSize();
//            int opcode = MessageMappingHolder.getopCode(msg);
//            short msgLen = (short) len;
//
//            byte pData[] = new byte[len];
////            pData[0] = (byte) (opcode >> 8);
////            pData[1] = (byte) (opcode & 0xFF);
//            pData[3] =  (byte) (opcode & 0xFF);
//            pData[2] =  (byte) ((opcode>>8) & 0xFF);
//            pData[1] =  (byte) ((opcode>>16) & 0xFF);
//            pData[0] =  (byte) ((opcode>>24) & 0xFF);
//            byte content[] = msg.toByteArray();
//
//            System.arraycopy(content, 0, pData, 4, len - 4);
//
//            if (len > Short.MAX_VALUE) {
//                log.info("{} message out of limit", opcode);
//                return;
//            }
//
//
//            byte crcVerify = 0;
//            int verfiyCode = msgLen ^ 1606;
//            for (int i = 0; i < msgLen; ++i) {
//                crcVerify += (pData[i]/* << 1*/);
//            }
//
//            byte head[] = new byte[6];
//            head[0] = (byte) (msgLen >> 8);
//            head[1] = (byte) (msgLen & 0xFF);
//            head[2] = (byte) (verfiyCode >> 8);
//            head[3] = (byte) (verfiyCode & 0xFF);
//            head[4] = 0;
//            head[5] = crcVerify;
//            buf.writeBytes(head);
//            buf.writeBytes(pData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
    //int 转为字节数组
    public static byte[] intToBytes( int value)
    {
//        buf.writeInt(value);
        byte[] src = new byte[4];
        src[3] =  (byte) (value & 0xFF);
        src[2] =  (byte) ((value>>8) & 0xFF);
        src[1] =  (byte) ((value>>16) & 0xFF);
        src[0] =  (byte) ((value>>24) & 0xFF);
        return src;
//        return null;
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


    public static void main(String[] args) {
        byte[] bytes = intToBytes(0);
        System.out.println(Arrays.toString(bytes));;

        byte[] aa=new byte[]{8, 111};
        String s = Arrays.toString(aa);
        System.out.println(s);;
    }
}
