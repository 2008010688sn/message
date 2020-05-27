package com.wp.casino.messageserver.utils;

import com.wp.casino.messagenetty.utils.MessageDispatcher;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/20 16:30
 */
public class HandlerContext {



    private  static ConcurrentHashMap<String, DispatcherObj> maps=new ConcurrentHashMap<>(0);


    private static class SingletonHolder {
        private final static HandlerContext INSTANCE = new HandlerContext();
    }

    private HandlerContext() {
    }

    public static HandlerContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addChannel(String id,DispatcherObj dispatcherObj) {
        maps.put(id,dispatcherObj);
    }

    public void removeChannel(String id) {
        maps.remove(id);
    }

    public DispatcherObj getChannel(String id){
        DispatcherObj dispatcherObj=maps.get(id);
        return dispatcherObj;
    }

    public ConcurrentHashMap<String, DispatcherObj> getMaps(){
        return maps;
    }

    public int getSize() {
        return maps.size();
    }

}
