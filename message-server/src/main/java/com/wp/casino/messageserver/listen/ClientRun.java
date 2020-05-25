package com.wp.casino.messageserver.listen;

import com.wp.casino.messageserver.service.MessageClient;
import com.wp.casino.messageserver.service.MessageServer;
import lombok.extern.slf4j.Slf4j;
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



    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageClient messageClient=new MessageClient();
        messageClient.start();
        messageClient.connect("127.0.0.1",9123);
        addHook(messageClient);
    }

    private  void addHook(MessageClient client) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {

                    try {
                        client.stop();
                    } catch (Exception e) {
                        log.error(" server stop ex", e);
                    }
                    log.info("jvm exit, all service stopped.");

                }, "messageserver-shutdown-hook-thread")
        );
    }
}
