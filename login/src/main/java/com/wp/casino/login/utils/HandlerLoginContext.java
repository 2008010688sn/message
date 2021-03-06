package com.wp.casino.login.utils;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/20 16:30
 */
public class HandlerLoginContext {

    private final static ConcurrentHashMap<String, ChannelHandlerContext> maps=new ConcurrentHashMap<>(0);


    private static class SingletonHolder {
        private final static HandlerLoginContext INSTANCE = new HandlerLoginContext();
    }

    private HandlerLoginContext() {
    }

    public static HandlerLoginContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addChannel(String id,ChannelHandlerContext socket) {
        maps.put(id,socket);
    }

    public void removeChannel(String id) {
        maps.remove(id);
    }

    public ChannelHandlerContext getChannel(String id){
        ChannelHandlerContext ctx=maps.get(id);
        return ctx;
    }

    public int getSize() {
        return maps.size();
    }

}
