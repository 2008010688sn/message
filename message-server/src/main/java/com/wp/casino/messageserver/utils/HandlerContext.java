package com.wp.casino.messageserver.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/20 16:30
 */
public class HandlerContext {

    private  static ConcurrentHashMap<String, Channel> maps=new ConcurrentHashMap<>(0);


    private static class SingletonHolder {
        private final static HandlerContext INSTANCE = new HandlerContext();
    }

    private HandlerContext() {
    }

    public static HandlerContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addChannel(String id,Channel socket) {
        maps.put(id,socket);
    }

    public void removeChannel(String id) {
        maps.remove(id);
    }

    public Channel getChannel(String id){
        Channel ctx=maps.get(id);
        return ctx;
    }

    public ConcurrentHashMap<String, Channel> getMaps(){
        return maps;
    }

    public int getSize() {
        return maps.size();
    }

}
