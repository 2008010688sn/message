package com.wp.casino.messagenetty.deec;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wp.casino.messagenetty.utils.MessageMappingHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author sn
 * @date 2020/5/15 16:11
 */
public class MessageDecoder extends ByteToMessageDecoder {

    ByteBuf buf = Unpooled.buffer(1024 * 40);
    int currentPackageLength = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        buf.writeBytes(in);
        while (buf.readableBytes() > 0) {
            if (currentPackageLength == 0) {
                if (buf.readableBytes() < 6) {
                    return;
                }
                currentPackageLength = buf.readShort();
                buf.readBytes(4);
            }
            if (buf.readableBytes() < currentPackageLength) {
                return;
            } else {
                ByteBuf body = buf.readBytes(currentPackageLength);
                int opcode = body.readInt();

                Parser<?> parser= MessageMappingHolder.getParser(opcode);
                if (parser==null){
                    throw new IllegalArgumentException("illegal opCode " + opcode);
                }
                byte[] bytes = new byte[currentPackageLength - 4];
                body.readBytes(bytes);
                MessageLite messageLite= (MessageLite) parser.parseFrom(bytes);
                currentPackageLength = 0;
                buf.discardReadBytes();
                ctx.fireChannelRead(messageLite);//将消息传递下去，或者在这里将消息发布出去
            }
        }

//        if (in.readableBytes() < 6)
//            return;
//        in.markReaderIndex();
//        short length = in.readShort();//消息的长度
//        if ((length <= 4))
//            throw new IllegalArgumentException();
////        int crc= in.readInt();//校验位
//        in.readShort();
//        in.readByte();
//        in.readByte();
//        if (in.readableBytes() < length) {
//            in.resetReaderIndex();
//            return;
//        }
//        int opCode= in.readInt();//opcode
//
//        //通过索引获得该协议对应的解析器(客户端与服务器需要保持索引的一致性)
//        Parser<?> parser= MessageMappingHolder.getParser(opCode);
//        if (parser==null){
//            throw new IllegalArgumentException("illegal opCode " + opCode);
//        }
//
//        byte[] bytes = new byte[length];
//        in.readBytes(bytes);
//        MessageLite messageLite= (MessageLite) parser.parseFrom(bytes);
//        ctx.fireChannelRead(messageLite);//将消息传递下去，或者在这里将消息发布出去
    }
}
