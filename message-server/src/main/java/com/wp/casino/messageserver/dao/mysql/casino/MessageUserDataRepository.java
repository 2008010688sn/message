package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.MessageUserData;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author sn
 * @date 2020/5/29 15:56
 */
public interface MessageUserDataRepository extends JpaRepository<MessageUserData,Long> {
}
