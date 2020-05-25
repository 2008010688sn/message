package com.wp.casino.messageserver.utils;

import com.google.protobuf.MessageLite;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/25 15:07
 */
@Data
public class MessageDispatchTask implements Serializable {

    private String channelId;

    private MessageLite messageLite;

}
