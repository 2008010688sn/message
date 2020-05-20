package com.wp.casino.messageclient;

import com.wp.casino.messageclient.service.MessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/5/19 17:18
 */
@Component
@Slf4j
public class ClientRun implements ApplicationRunner {



    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageClient messageClient=new MessageClient();
        messageClient.start();
        messageClient.connect("127.0.0.1",9876);
    }
}
