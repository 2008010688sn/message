package com.wp.casino.messageclient.listen;

import com.wp.casino.messageclient.service.MessageClient;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author sn
 * @date 2020/5/15 19:23
 */
@Slf4j
public class NettyClientListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("ServletContex初始化...");
        MessageClient messageClient=new MessageClient();
        messageClient.start();
        messageClient.connect("127.0.0.1",9876);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
