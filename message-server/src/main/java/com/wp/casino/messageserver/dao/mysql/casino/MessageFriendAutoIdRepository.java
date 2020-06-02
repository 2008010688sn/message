package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.MessageFriendAutoId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author sn
 * @date 2020/6/1 17:28
 */
public interface MessageFriendAutoIdRepository extends JpaRepository<MessageFriendAutoId,Integer> {



}
