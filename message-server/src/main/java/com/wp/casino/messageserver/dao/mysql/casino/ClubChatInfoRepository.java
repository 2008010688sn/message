package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.ClubChatInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author sn
 * @date 2020/5/29 18:24
 */
public interface ClubChatInfoRepository extends JpaRepository<ClubChatInfo,Integer> {

    ClubChatInfo findByClClubId(Integer clubId);

    //查询指定俱乐部的数量
    @Query(value = "select count(*) FROM tb_pyq_club_chat_info WHERE cl_club_id = ?1 ",nativeQuery = true)
    Integer findCountByClubId(Integer clubId);


    @Query(value = "    SELECT cl_auto_id, cl_member_uid, cl_game_id, cl_club_id, cl_chat_message, cl_message_send_time, cl_msg_type,cl_club_message_id FROM tb_pyq_club_chat_info WHERE cl_club_id = ?1  AND  cl_message_send_time>=?2 ORDER BY cl_auto_id DESC LIMIT ?3 ",nativeQuery = true)
    List<ClubChatInfo> findChats(Integer cluId,Integer joinTime,Integer countNum);

    @Query(value = "    SELECT cl_auto_id, cl_member_uid, cl_game_id, cl_club_id, cl_chat_message, cl_message_send_time, cl_msg_type,cl_club_message_id FROM tb_pyq_club_chat_info WHERE cl_club_id = ?1  AND cl_auto_id < ?2 AND  cl_message_send_time>=?3  ORDER BY cl_auto_id DESC LIMIT ?4 ",nativeQuery = true)
    List<ClubChatInfo> findLtChats(Integer cluId,Integer autoId,Integer joinTime,Integer countNum);

}
