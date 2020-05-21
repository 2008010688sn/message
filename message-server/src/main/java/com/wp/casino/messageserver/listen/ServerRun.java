package com.wp.casino.messageserver.listen;

import com.wp.casino.messageserver.service.MessageServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/5/19 15:11
 */
@Component
@Slf4j
@Order(1)
public class ServerRun implements ApplicationRunner {

    @Value("${server.bind_port}")
    private int port;

    @Value("${server.bind_address}")
    private String host;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageServer messageServer=new MessageServer(host,port);
        messageServer.init();
        messageServer.start();
    }
}
