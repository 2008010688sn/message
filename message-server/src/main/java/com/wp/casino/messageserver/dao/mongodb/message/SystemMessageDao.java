package com.wp.casino.messageserver.dao.mongodb.message;

import com.wp.casino.messageserver.domain.QuerySystemMessage;
import com.wp.casino.messageserver.domain.SystemMessage;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/5/20 20:11
 */
@Component
public class SystemMessageDao extends MessageMongodbBaseDao<SystemMessage, QuerySystemMessage> {

}
