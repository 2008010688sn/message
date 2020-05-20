//package com.wp.casino.messageserver.listen;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import javax.servlet.annotation.WebListener;
//
//import com.wp.casino.messageserver.service.MessageServer;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
///**
// * Netty 服务监听器
// *
// */
////@WebListener
//@Slf4j
////@Component
//public class NettyServerListener implements ServletContextListener {
//
//
//
//
//    /** 注入NettyServer */
////    @Autowired
////    private MessageServer messageServer;
//
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//    	log.info("ServletContex初始化...");
//        MessageServer messageServer=new MessageServer(9876);
//        //初始化
//        messageServer.init();
//        //启动
//        messageServer.start();
////
////    	Thread thread = new Thread(new NettyServerThread());
////        // 启动netty服务
////        thread.start();
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//
//    }
//
//    /**
//     * Netty 服务启动线程
//     */
//    private class NettyServerThread implements Runnable {
//
//        @Override
//        public void run() {
////            MessageServer messageServer=new MessageServer();
////            messageServer.start();
//        }
//    }
//
//}