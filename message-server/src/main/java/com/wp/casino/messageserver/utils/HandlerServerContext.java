package com.wp.casino.messageserver.utils;

import com.wp.casino.messageserver.domain.LoginPlayer;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/20 16:30
 */
public class HandlerServerContext {

    private  static ConcurrentHashMap<Long, LoginPlayer> chServermaps=new ConcurrentHashMap<>(0);


    private static class SingletonHolder {
        private final static HandlerServerContext INSTANCE = new HandlerServerContext();
    }

    private HandlerServerContext() {
    }

    public static HandlerServerContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addChannel(Long plyGuid,LoginPlayer loginPlayer) {
        chServermaps.put(plyGuid, loginPlayer);
    }

    public void removeChannel(Long plyGuid) {
        chServermaps.remove(plyGuid);
    }

    public LoginPlayer getChannel(Long plyGuid){
        LoginPlayer loginPlayer = chServermaps.get(plyGuid);
        return loginPlayer;
    }

    public ConcurrentHashMap<Long, LoginPlayer> getMaps(){
        return chServermaps;
    }

    public int getSize() {
        return chServermaps.size();
    }

}
