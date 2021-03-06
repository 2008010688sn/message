package com.wp.casino.messageserver.utils;

import com.wp.casino.messagenetty.utils.MessageEnum;
import com.wp.casino.messageserver.dao.mysql.accout.AccountDetailRepository;
import com.wp.casino.messageserver.dao.mysql.casino.*;
import com.wp.casino.messageserver.dao.mysql.casinolating.MessageUserDataLatingRepository;
import com.wp.casino.messageserver.domain.mysql.account.AccountDetail;
import com.wp.casino.messageserver.domain.mysql.casino.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static ConfigGlobalStringRepository configGlobalStringRepository;

    public static void setConfigGlobalStringRepository(ConfigGlobalStringRepository configGlobalStringRepository) {
        ClubDataUtil.configGlobalStringRepository = configGlobalStringRepository;
    }

    private static GlobalErrorDescRepository globalErrorDescRepository;
    public static void setGlobalErrorDescRepository(GlobalErrorDescRepository globalErrorDescRepository) {
        ClubDataUtil.globalErrorDescRepository = globalErrorDescRepository;
    }

    private static  ConfigStringDataRepository configStringDataRepository;

    public static void setConfigStringDataRepository(ConfigStringDataRepository configStringDataRepository) {
        ClubDataUtil.configStringDataRepository = configStringDataRepository;
    }

    private static MessageFriendAutoIdRepository messageFriendAutoIdRepository;

    public static void setMessageFriendAutoIdRepository(MessageFriendAutoIdRepository messageFriendAutoIdRepository) {
        ClubDataUtil.messageFriendAutoIdRepository = messageFriendAutoIdRepository;
    }

    private static MessageUserDataLatingRepository messageUserDataLatingRepository;

    public static void setMessageUserDataLatingRepository(MessageUserDataLatingRepository messageUserDataLatingRepository) {
        ClubDataUtil.messageUserDataLatingRepository = messageUserDataLatingRepository;
    }

    /**
     * 根据clubId获取有添加权限的俱乐部创建者和管理员信息
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
     * 根据clubId获取所有俱乐部创建者和管理员信息
     * @param clubId
     * @return
     */
    public static List<PyqClubMembers> getAllClubAdminList(Integer clubId){
        if (clubRepository==null){
            clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubMembersRepository.class);
        }
        List<PyqClubMembers> clubAdminLlist = clubRepository.findAllClubAdmin(clubId);
        return clubAdminLlist;
    }

    /**
     * 根据clubid查询俱乐部成员
     * @param clubId
     * @return
     */
    public static List<PyqClubMembers> findClubMembersByClubId(Integer clubId){
        if (clubRepository==null){
            clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubMembersRepository.class);
        }
        List<PyqClubMembers> clubMembers=null;
        if (clubId==10000){//俱乐部10000为大俱乐部，成员太多，根据业务返回50条数据
           clubMembers=clubRepository.findAllByCmClubIdAndCmStatusLimitNum(clubId,50);
        }else{
            //状态为有效的俱乐部的成员信息
            clubMembers = clubRepository.findAllByCmClubIdAndCmStatus(clubId,0);
        }
        return clubMembers;
    }

    /**
     * 根据clubId和uid查询俱乐部成员信息
     * @param clubId
     * @param uid
     * @return
     */
    public static PyqClubMembers findClubMemberByClubIdAndUid(Integer clubId,Long uid){
        if (clubRepository==null){
            clubRepository = ApplicationContextProvider.getApplicationContext().getBean(ClubMembersRepository.class);
        }
        PyqClubMembers clubMember = clubRepository.findByCmClubIdAndCmPlyGuid(clubId,uid);
        return clubMember;
    }

    /**
     * 根据uid查询messageUuserdata信息
     * @param uid
     * @return
     */
    public static MessageUserData findMessageUserDataByUid(Long uid){
        if (messageUserDataRepository==null){
            messageUserDataRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageUserDataRepository.class);
        }
        MessageUserData messageUserData = messageUserDataRepository.findById(uid).get();
        return messageUserData;
    }

    /**
     *
     * @param mdNickname
     * @param mdVip
     * @param mdLevel
     * @param mdLoginTime
     * @param mdPlyGuid
     * @return
     */
    public static Integer updateMessageUserData (String mdNickname, Integer mdVip, Integer mdLevel, Integer mdLoginTime, long mdPlyGuid) {
        if (messageUserDataRepository==null){
            messageUserDataRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageUserDataRepository.class);
        }
        String nickname = messageUserDataRepository.selectLatinNicknameByUid(mdPlyGuid);

        return messageUserDataRepository.updateMessageUser(nickname, mdVip, mdLevel, mdLoginTime, mdPlyGuid);
    }

    /**
     *
     * @param mdNickname
     * @param mdVip
     * @param mdLevel
     * @param mdLoginTime
     * @param mdPlyGuid
     * @return
     */
    public static Integer updateMessageUserDataLating (String mdNickname, Integer mdVip, Integer mdLevel, Integer mdLoginTime, long mdPlyGuid) {
        if (messageUserDataLatingRepository==null){
            messageUserDataLatingRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageUserDataLatingRepository.class);
        }
//        String nickname = messageUserDataLatingRepository.selectLatinNicknameByUid(mdPlyGuid);
        String nickname = messageUserDataLatingRepository.selectLatinNicknameByNickName(mdNickname);

        return messageUserDataLatingRepository.updateMessageUser(nickname, mdVip, mdLevel, mdLoginTime, mdPlyGuid);
    }

    /**
     * 保存mesageuserdata信息
     * @param messageUserData
     * @return
     */
    public static MessageUserData saveMessageUserData(MessageUserData messageUserData){

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
     * 获取俱乐部聊天记录
     * @param clubId
     * @param autoId
     * @param joinTime
     * @param countNum
     * @return
     */
    public static List<ClubChatInfo>  findClubChatInfos(Integer clubId,Integer autoId,Integer joinTime,Integer countNum){
        if (clubChatInfoRepository==null){
            clubChatInfoRepository=ApplicationContextProvider.getApplicationContext().getBean(ClubChatInfoRepository.class);
        }
        List<ClubChatInfo> chats=null;
        if (autoId==0){
            chats = clubChatInfoRepository.findChats(clubId, joinTime, countNum);
        }else{
            chats= clubChatInfoRepository.findLtChats(clubId, autoId, joinTime, countNum);
        }
        return  chats;
    }

    /**
     * 查询最大messageId
     * @param clubUid
     * @return
     */
    public static Integer findMaxClubMessageIdByClubId(int clubUid) {
        if (clubChatInfoRepository==null){
            clubChatInfoRepository=ApplicationContextProvider.getApplicationContext().getBean(ClubChatInfoRepository.class);
        }
        return clubChatInfoRepository.findMaxMessageId(clubUid);
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
     * 根据uid查询数量
     * @param uid
     * @return
     */
    public static Integer findMessageFriendListCountByUID(Long uid){
        if (messageFriendListRepository==null){
            messageFriendListRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageFriendListRepository.class);
        }
        Integer count = messageFriendListRepository.findCountByGuid(uid);
        if (count==null){
            return 0;
        }
        return  count;
    }

    /**
     *加载configglobalstring数据
     * @return
     */
    public static List<ConfigGlobalString> findConfigGlobalList(){
        if (configGlobalStringRepository==null){
            configGlobalStringRepository=ApplicationContextProvider.getApplicationContext().getBean(ConfigGlobalStringRepository.class);
        }
        List<ConfigGlobalString> configGlobalStringList = configGlobalStringRepository.findConfigGlobalStringList();
        return  configGlobalStringList;
    }

    /**
     *加载GlobalErrorDesc数据
     * @return
     */
    public static List<GlobalErrorDesc> findGlobalErrorDescList(){
        if (globalErrorDescRepository==null){
            globalErrorDescRepository=ApplicationContextProvider.getApplicationContext().getBean(GlobalErrorDescRepository.class);
        }
        List<GlobalErrorDesc> globalErrorDescList = globalErrorDescRepository.findGlobalErrorDescList();
        return  globalErrorDescList;
    }

    /**
     * 加载ConfigStringData数据
     * @return
     */
    public static List<ConfigStringData> findConfigStringDataList(){
        if (configStringDataRepository==null){
            configStringDataRepository=ApplicationContextProvider.getApplicationContext().getBean(ConfigStringDataRepository.class);
        }
        List<ConfigStringData> configStringDataList = configStringDataRepository.findConfigStringDataList();
        return  configStringDataList;
    }

    /**
     * 查询MessageFriendAutoId
     * @return
     */
    public static MessageFriendAutoId findMessageFriendAutoId(){
        if (messageFriendAutoIdRepository==null){
            messageFriendAutoIdRepository=ApplicationContextProvider.getApplicationContext().getBean(MessageFriendAutoIdRepository.class);
        }
        MessageFriendAutoId messageFriendAutoId = messageFriendAutoIdRepository.findAll().get(0);
        return  messageFriendAutoId;
    }

    public static Map<String,Object> loadPlyData(long uid){
        Map<String,Object> map=new HashMap<>();
        Integer messageFriendListCount = findMessageFriendListCountByUID(uid);
        String face="";
        AccountDetail accountByUid = findAccountByUid(uid);
        if (accountByUid!=null){
            face=accountByUid.getAFace();
        }
        MessageUserData messageUserDataByUid = findMessageUserDataByUid(uid);
        Integer mdApproveNoti=0;
        Integer mdFriendLimit=0;
        if (messageUserDataByUid!=null){
             mdApproveNoti = messageUserDataByUid.getMdApproveNoti();
             mdFriendLimit = messageUserDataByUid.getMdFriendLimit();
        }
        map.put("face",face);
        map.put("approveNoti",mdApproveNoti);
        map.put("friendLimit",mdFriendLimit);
        map.put("friendNum",messageFriendListCount);
        return map;
    }

}
