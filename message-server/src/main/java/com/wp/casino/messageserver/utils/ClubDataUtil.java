package com.wp.casino.messageserver.utils;

import com.wp.casino.messageserver.dao.mysql.ClubMembersRepository;
import com.wp.casino.messageserver.domain.PyqClubMembers;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author sn
 * @date 2020/5/28 17:07
 */
@Component
public class ClubDataUtil {


    private static ClubMembersRepository clubRepository;

    public static void setClubRepository(ClubMembersRepository clubRepository) {
        ClubDataUtil.clubRepository = clubRepository;
    }

    /**
     * 根据clubId获取俱乐部创建者和管理员信息
     * @param clubId
     * @return
     */
    public static List<PyqClubMembers> getClubAdminList(Integer clubId){
        if (clubRepository==null){
            clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubMembersRepository.class);
        }
        List<PyqClubMembers> clubAdminLlist = clubRepository.findClubAdmin(clubId);
        return clubAdminLlist;
    }



}
