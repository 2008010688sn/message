package com.wp.casino.messageserver.dao.mysql;

import com.wp.casino.messageserver.domain.PyqClubMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author sn
 * @date 2020/5/28 10:24
 */
public interface ClubRepository extends JpaRepository<PyqClubMembers,Integer> {

    //查询俱乐部的创建者及管理员信息
    @Query(value = "select pm  from PyqClubMembers pm where pm.cmClubId=?1 and pm.cmStatus in (0) and pm.cmRole in (1,2)")
    List<PyqClubMembers> findClubAdmin(Integer clubId);
}
