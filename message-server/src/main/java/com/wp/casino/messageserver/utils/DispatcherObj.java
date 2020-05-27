package com.wp.casino.messageserver.utils;

import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * @author sn
 * @date 2020/5/27 11:13
 */
@Data
public class DispatcherObj {

    private  String channelId;

    private ChannelHandlerContext channelHandlerContext;

    private MessageDispatcher messageDispatcher;

}
