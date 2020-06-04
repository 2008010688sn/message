package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.MessageUserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sn
 * @date 2020/5/29 15:56
 */
public interface MessageUserDataRepository extends JpaRepository<MessageUserData,Long> {


    @Modifying
    @Query(value = "UPDATE tb_message_user_data set md_system_auto_id=?1 where md_ply_guid=?2",nativeQuery = true)
    Integer updateByPlyGuid(Integer autoId,Long uid);

    @Modifying
    @Transactional
    @Query(value = "update tb_message_user_data set md_nickname = ?1, md_vip=?2,md_level=?3,md_login_time=?4 where md_ply_guid=?5", nativeQuery = true)
    Integer updateMessageUser(String mdNickname, Integer mdVip, Integer mdLevel, Integer mdLoginTime, long mdPlyGuid);

}
