package com.wp.casino.messageserver.listen;

import com.wp.casino.messageserver.service.MessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/5/19 17:18
 */
@Component
@Slf4j
@Order(2)
public class ClientRun implements ApplicationRunner {

    @Value("${server.client_port}")
    private int port;

    @Value("${server.client_address}")
    private String host;

    @Value("${server.client_heartbeat}")
    private int heartbeat;

    @Value("${server.client_interval}")
    private int interval;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageClient messageClient=new MessageClient();
        messageClient.start();
        messageClient.connect(host,port,heartbeat,interval);
        addHook(messageClient);
    }

    private  void addHook(MessageClient client) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        client.stop();
                    } catch (Exception e) {
                        log.error(" clent stop ex", e);
                    }
                    log.info("jvm exit, all service stopped.");

                }, "messageclent-shutdown-hook-thread")
        );
    }



}
