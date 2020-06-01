package com.wp.casino.messageserver.utils;

import com.wp.casino.messageserver.dao.mysql.accout.AccountDetailRepository;
import com.wp.casino.messageserver.dao.mysql.casino.ClubChatInfoRepository;
import com.wp.casino.messageserver.dao.mysql.casino.ClubMembersRepository;
import com.wp.casino.messageserver.dao.mysql.casino.MessageFriendListRepository;
import com.wp.casino.messageserver.dao.mysql.casino.MessageUserDataRepository;
import com.wp.casino.messageserver.domain.mysql.account.AccountDetail;
import com.wp.casino.messageserver.domain.mysql.casino.ClubChatInfo;
import com.wp.casino.messageserver.domain.mysql.casino.MessageFriendList;
import com.wp.casino.messageserver.domain.mysql.casino.MessageUserData;
import com.wp.casino.messageserver.domain.mysql.casino.PyqClubMembers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    private static AccountDetailRepository accountDetailRepository;

    public static void setAccountDetailRepository(AccountDetailRepository accountDetailRepository) {
        ClubDataUtil.accountDetailRepository = accountDetailRepository;
    }

    private static MessageUserDataRepository messageUserDataRepository;

    public static void setMessageUserDataRepository(MessageUserDataRepository messageUserDataRepository) {
        ClubDataUtil.messageUserDataRepository = messageUserDataRepository;
    }

    private static ClubChatInfoRepository clubChatInfoRepository;

    public static void setClubChatInfoRepository(ClubChatInfoRepository clubChatInfoRepository) {
        ClubDataUtil.clubChatInfoRepository = clubChatInfoRepository;
    }

    private static MessageFriendListRepository messageFriendListRepository;

    public static void setMessageFriendListRepository(MessageFriendListRepository messageFriendListRepository) {
        ClubDataUtil.messageFriendListRepository = messageFriendListRepository;
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

    /**
     * 根据uid查询messageUuserdata信息
     * @param uid
     * @return
     */
    public static MessageUserData findByUid(Long uid){
        if (messageUserDataRepository==null){
            messageUserDataRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageUserDataRepository.class);
        }
        MessageUserData messageUserData = messageUserDataRepository.findById(uid).get();
        return messageUserData;
    }

    /**
     * 保存mesageuserdata信息
     * @param messageUserData
     * @return
     */
    public static MessageUserData saveUserData(MessageUserData messageUserData){

        if (messageUserDataRepository==null){
            messageUserDataRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageUserDataRepository.class);
        }
        MessageUserData userData = messageUserDataRepository.save(messageUserData);
        return userData;
    }

    /**
     * 根据uid查询玩家信息
     * @param uid
     * @return
     */
    public static AccountDetail findAccountByUid(Long uid){
        if (accountDetailRepository==null){
            accountDetailRepository=ApplicationContextProvider.getApplicationContext().getBean(AccountDetailRepository.class);
        }
        AccountDetail accountDetail = accountDetailRepository.findById(uid).get();
        return  accountDetail;
    }

    /**
     * 添加俱乐部连天信息
     * @param clubChatInfo
     * @return
     */
    public static ClubChatInfo saveClubChatInfo(ClubChatInfo clubChatInfo){
        if (clubChatInfoRepository==null){
            clubChatInfoRepository=ApplicationContextProvider.getApplicationContext().getBean(ClubChatInfoRepository.class);
        }
        ClubChatInfo chatInfo = clubChatInfoRepository.save(clubChatInfo);
        return chatInfo;
    }

    /**
     * 根据clubId查询clubChatInfo信息
     * @param clubId
     * @return
     */
    public static ClubChatInfo findClubChatInfoByUid(Integer clubId){
        if (clubChatInfoRepository==null){
            clubChatInfoRepository=ApplicationContextProvider.getApplicationContext().getBean(ClubChatInfoRepository.class);
        }
        ClubChatInfo byClClubId = clubChatInfoRepository.findByClClubId(clubId);
        return  byClClubId;
    }

    /**
     * 根据clubId查询clubChatInfo信息
     * @param clubId
     * @return
     */
    public static Integer  findClubChatInfoCountByClubId(Integer clubId){
        if (clubChatInfoRepository==null){
            clubChatInfoRepository=ApplicationContextProvider.getApplicationContext().getBean(ClubChatInfoRepository.class);
        }
        Integer countByClubId = clubChatInfoRepository.findCountByClubId(clubId);
        return  countByClubId;
    }


    /**
     * 根据uid查询messagefriendList信息
     * @param uid
     * @return
     */
    public static MessageFriendList findMessageFriendListByUID(Long uid){
        if (messageFriendListRepository==null){
            messageFriendListRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageFriendListRepository.class);
        }
        MessageFriendList byUid = messageFriendListRepository.findByUid(uid);
        return  byUid;
    }

    /**
     *
     * 查找消息
     * @param plyGuid
     * @param autoid
     * @param reqNum
     * @param clubUid
     * @return
     */
    public static List<ClubChatInfo> syncClubChatRecord(long plyGuid, int autoid, int reqNum, int clubUid) {
        // TODO
        return null;
    }

    /**
     *
     * @param plyGuid
     * @param clubUid
     * @param gameId
     * @param chatMsg
     * @param type
     * @return
     */
    public static ClubChatInfo addClubChatRecord(long plyGuid, int clubUid, int gameId, String chatMsg, int type) {
        return null;
    }
}
