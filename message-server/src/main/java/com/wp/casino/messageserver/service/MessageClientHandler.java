package com.wp.casino.messageserver.service;

import com.google.protobuf.MessageLite;
import com.wp.casino.messagenetty.proto.WorldMessage;
import com.wp.casino.messagenetty.utils.MessageDispatcher;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sn
 * @date 2020/5/15 17:25
 */
@ChannelHandler.Sharable//该注解Sharable主要是为了多个handler可以被多个channel安全地共享，也就是保证线程安全
@Slf4j
public class MessageClientHandler extends SimpleChannelInboundHandler<MessageLite> {



    public   static volatile boolean isBegin ;

    private  MessageDispatcher messageDispatcher;

    private MessageClient messageClient;

    protected  AtomicBoolean connected = new AtomicBoolean();


    public MessageClientHandler(MessageDispatcher messageDispatcher,MessageClient messageClient) {
        this.messageDispatcher=messageDispatcher;
        this.messageClient=messageClient;
    }


    /** 循环次数 */
    private AtomicInteger fcount = new AtomicInteger(1);

    /**
     * 建立连接时
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("和worldserver建立连接时：" + new Date());
        ctx.fireChannelActive();
    }

    /**
     * 关闭连接时
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("messageclient-----关闭连接时：" + new Date());
        Map<String,Object> map=getClientConfigPro();
        if (map!=null){
            String host =map.get("host").toString();
            Integer port=(Integer) map.get("port");
            Integer heartbeat=(Integer) map.get("heartbeat");
            Integer interval=(Integer) map.get("interval");
            messageClient.connect(host, port, heartbeat, interval);
//            while (connected.get()){
//                ChannelFuture future = messageClient.connect(host, port, heartbeat, interval);
//                if (!future.isSuccess()){
//                    Thread.sleep(interval);
//                }else{
//                    connected.compareAndSet(true, false);
//                }
//            }
        }
        super.channelInactive(ctx);
    }

    /**
     * 心跳请求处理 每100ms秒发送一次心跳请求;
     *
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        log.info("心跳请求：" + new Date() + "，次数" + fcount.get());
        if (ctx.channel()==null){
            log.info("reconnect---start--");
            Map<String,Object> map=getClientConfigPro();
            if (map!=null){
                String host =map.get("host").toString();
                Integer port=(Integer) map.get("port");
                Integer heartbeat=(Integer) map.get("heartbeat");
                Integer interval=(Integer) map.get("interval");
                messageClient.connect(host,port,heartbeat,interval);
            }
        }else{
            if (obj instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) obj;
                // 如果写通道处于空闲状态,就发送心跳命令
                if (IdleState.WRITER_IDLE.equals(event.state())) {
                    //发送ping
                    sendPingMsg(ctx);
                } else {
                    super.userEventTriggered(ctx, obj);
                }
            }
        }
    }

    /**
     * 发送ping消息
     * @param context
     */
    protected void sendPingMsg(ChannelHandlerContext context) {
        Timestamp timestamp= Timestamp.valueOf(LocalDateTime.now());
        int time = (int)(timestamp.getTime()/1000);
        WorldMessage.prt_ping ping=WorldMessage.prt_ping.newBuilder().setNowTime(time).build();
        context.channel().writeAndFlush(ping);
        int i = fcount.incrementAndGet();
        log.info("心跳请求次数{}",i);

    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        log.info("messageserver客户端接受到worldserver信息:{}",ctx.channel().remoteAddress().toString());
        log.info("messageLite----"+messageLite.toString());
        messageDispatcher.onMessage(ctx.channel(),messageLite);
    }

    /**
     * 获取客户端的配置
     * @return
     */
    private Map<String,Object> getClientConfigPro(){
        Map<String,Object> map=null;
        Properties properties = getProperties("application.yml");
        if (properties!=null){
            map=new HashMap<>();
            String host = properties.getProperty("client_address");
            String portStr = properties.getProperty("client_port");
            String heartbeatStr = properties.getProperty("client_heartbeat");
            String intervalStr = properties.getProperty("client_interval");
            Integer port=9123;
            Integer heartbeat=1;
            Integer interval=100;
            if (StringUtils.isNoneBlank(portStr)){
                port=Integer.parseInt(portStr);
            }
            if (StringUtils.isNoneBlank(heartbeatStr)){
                heartbeat=Integer.parseInt(heartbeatStr);
            }
            if (StringUtils.isNoneBlank(intervalStr)){
                interval=Integer.parseInt(intervalStr);
            }
            map.put("host",host);
            map.put("port",port);
            map.put("heartbeat",heartbeat);
            map.put("interval",interval);
        }
        return map;
    }

    public Properties getProperties(String file) {
        //暂时只对application.properties,配置文件进行判断
        HashMap<String,String> map=new HashMap<String,String>();
        map.put("/application.yml", "application.yml");
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean=new YamlPropertiesFactoryBean();
        String env = yamlPropertiesFactoryBean.getObject().getProperty("spring.profiles.active");
        if(map.containsKey(file)){
            //判断是否需要加载镜像或正式的配置文件
            if(StringUtils.isNotBlank(env) && !"develop".equals(env)){
                String fix=file.substring(0, file.indexOf("."));
                file=fix+"-"+env+".yml";
            }
        }
        Properties properties=null;
        try {
            properties = PropertiesLoaderUtils.loadAllProperties(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
