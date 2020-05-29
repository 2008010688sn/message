package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.ClubChatInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author sn
 * @date 2020/5/29 18:24
 */
public interface ClubChatInfoRepository extends JpaRepository<ClubChatInfo,Integer> {

    ClubChatInfo findByClClubId(Integer clubId);

    //查询指定俱乐部的数量
    @Query(value = "select count(*) FROM tb_pyq_club_chat_info WHERE cl_club_id = ?1 ",nativeQuery = true)
    Integer findCountByClubId(Integer clubId);
}
