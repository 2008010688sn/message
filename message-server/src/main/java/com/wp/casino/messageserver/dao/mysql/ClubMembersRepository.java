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
public interface ClubMembersRepository extends JpaRepository<PyqClubMembers,Integer> {

    //查询俱乐部的创建者及管理员信息
    @Query(value = "select pm.cm_ply_guid,pm.cm_role  from tb_pyq_club_members pm LEFT JOIN tb_pyq_club_role_privilege cp on pm.cm_club_id=cp.pc_clubid where  pm.cm_club_id=?1 and pm.cm_status=0 and (pm.cm_role=1 or  pm.cm_role like '2%')  and cp.pc_privilege_list like '%1%' ",nativeQuery = true)
    List<PyqClubMembers> findClubAdmin(Integer clubId);
}
