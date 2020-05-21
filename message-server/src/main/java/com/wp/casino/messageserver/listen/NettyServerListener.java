//package com.wp.casino.messageserver.listen;
//
//import com.wp.casino.messageserver.service.MessageServer;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//
///**
// * @author sn
// * @date 2020/5/21 14:25
// */
//@Slf4j
////@Component
//public class NettyServerListener implements ServletContextListener {
//
//
//        @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        log.info("NettyServerListener init...");
//        MessageServer messageServer=new MessageServer(9876);
//        messageServer.init();
//        messageServer.start();
//    }
//}
