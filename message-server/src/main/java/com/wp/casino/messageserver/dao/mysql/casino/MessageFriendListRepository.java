package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.MessageFriendList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sun.util.resources.ga.LocaleNames_ga;

/**
 * @author sn
 * @date 2020/5/29 18:46
 */
public interface MessageFriendListRepository extends JpaRepository<MessageFriendList,Long> {

    @Query(value = "select * from tb_message_friend_list",nativeQuery = true)
    MessageFriendList findByUid(Long uid);

}
