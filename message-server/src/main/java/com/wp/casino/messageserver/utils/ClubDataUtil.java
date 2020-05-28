package com.wp.casino.messageserver.utils;

import com.wp.casino.messageserver.dao.mysql.ClubRepository;
import com.wp.casino.messageserver.domain.PyqClubMembers;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * @author sn
 * @date 2020/5/28 17:07
 */
@ComponentScan
public class ClubDataUtil {

    /**
     * 根据clubId获取俱乐部创建者和管理员信息
     * @param clubId
     * @return
     */
    public static List<PyqClubMembers> getClubAdminList(Integer clubId){
        ClubRepository clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubRepository.class);
        List<PyqClubMembers> clubAdminLlist = clubRepository.findClubAdmin(clubId);
        return clubAdminLlist;
    }

}
