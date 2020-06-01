package com.wp.casino.messageserver.utils;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/20 16:30
 */
public class HandlerServerContext {

    private  static ConcurrentHashMap<Long, String> chServermaps=new ConcurrentHashMap<>(0);


    private static class SingletonHolder {
        private final static HandlerServerContext INSTANCE = new HandlerServerContext();
    }

    private HandlerServerContext() {
    }

    public static HandlerServerContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addChannel(Long plyGuid,String channelId) {
        chServermaps.put(plyGuid, channelId);
    }

    public void removeChannel(Long plyGuid) {
        chServermaps.remove(plyGuid);
    }

    public String getChannel(Long plyGuid){
        String channelId = chServermaps.get(plyGuid);
        return channelId;
    }

    public ConcurrentHashMap<Long, String> getMaps(){
        return chServermaps;
    }

    public int getSize() {
        return chServermaps.size();
    }

}
