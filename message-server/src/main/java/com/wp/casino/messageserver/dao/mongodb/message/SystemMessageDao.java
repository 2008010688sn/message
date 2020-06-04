package com.wp.casino.messageserver.dao.mongodb.message;

import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.domain.ClubMsgCount;
import com.wp.casino.messageserver.domain.QuerySystemMessage;
import com.wp.casino.messageserver.domain.SystemMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author sn
 * @date 2020/5/20 20:11
 */
@Component
public class SystemMessageDao extends MessageMongodbBaseDao<SystemMessage, QuerySystemMessage> {

    /**
     * 查找未读消息的数目
     * @param plyGuid
     * @param clubId
     * @return
     */
    public List<ClubMsgCount> getMsgCount(long plyGuid, int clubId) {
        Criteria criteria = Criteria.where("id").is(plyGuid).and("status").is(MsgConstants.MSG_STATUS_UNREAD);

        Criteria cri = Criteria.where("cm_reciver_ids").elemMatch(criteria);
        if (clubId > 0) {
            cri.and("cm_club_id").is(clubId);
        }

        Aggregation aggregation =  Aggregation.newAggregation(
                Aggregation.match(cri),
                Aggregation.group("cm_club_id").count().as("count")
                        .last("cm_club_id").as("clubId"),
                Aggregation.project("clubId", "count")
        );

        AggregationResults<ClubMsgCount> outputType = mongoTemplate.aggregate(aggregation,"system_message",ClubMsgCount.class);
        return outputType.getMappedResults();
    }

    /**
     * 修改code值
     * @param messageId
     * @param code
     * @param whoGuid
     */
    public void updateMsgCode(long messageId, int code, long whoGuid) {
        Criteria criteria1 = Criteria.where("id").is(whoGuid).and("status").ne(MsgConstants.MSG_STATUS_DELETED);
        Criteria criteria = Criteria.where("_auto_id").is(messageId).and("cm_reciver_ids").elemMatch(criteria1);
        Query query = new Query(criteria);
        Update update = new Update().set("cm_message.code", code).set("cm_global_status", -1).set("cm_operator", whoGuid)
                .set("cm_reciver_ids.$.status", MsgConstants.MSG_STATUS_READ);
        super.update(query, update);
    }

    /**
     * 查找用户的消息，不包含删除的
     * @param plyGuid
     * @param type
     * @param clubId
     * @param autoId
     * @param maxCount
     * @return
     */
    public List<SystemMessage> findNotiMsg(long plyGuid, int type, int clubId, long autoId, int maxCount) {
        Query query = new Query();
        // 消息接收人和消息
        Criteria criteria1 = Criteria.where("id").is(plyGuid).and("status").
                ne(MsgConstants.MSG_STATUS_DELETED);

        Criteria criteria = Criteria.where("cm_reciver_ids").elemMatch(criteria1);
        if (type != -1) {
            criteria.and("cm_message_typ").is(type);
        }
        if (clubId > -1) {
            criteria.and("cm_club_id").is(clubId);
        }
        if (maxCount <= 0) {
            maxCount = 30;
        }
        if (autoId > 0) {
            criteria.and("_auto_id").gte(autoId - maxCount).lt(autoId);
        }
        query.with(Sort.by(Sort.Order.desc("_auto_id")));
        query.addCriteria(criteria);

        // 根据条件查询所有消息
        List<SystemMessage> list =  super.find(query);
        return list;
    }

    /**
     * 修改消息状态
     * @param autoList
     * @param plyGuid
     * @param statusValue
     */
    public void updateReceiveStatus(List<Long> autoList, long plyGuid, int statusValue) {
        Query query = new Query();
        Criteria criteria1 = Criteria.where("id").is(plyGuid).and("status").lt(statusValue);
        Criteria criteria = Criteria.where("_auto_id").in(autoList).and("cm_reciver_ids").elemMatch(criteria1);
        query.addCriteria(criteria);
        Update update = new Update().set("cm_reciver_ids.$.status", statusValue);
        super.update(query, update);
    }

    /**
     * 查找申请俱乐部次数
     * @param cmSendId 申请人
     * @param clubId 俱乐部
     * @param messageType 消息类型
     * @param showMessageType 消息展示类型
     * @param time 当天0点时间戳
     * @return
     */
    public Integer findApplyClubCount (long cmSendId, int clubId, int messageType, int showMessageType, int time) {
        Query query = new Query();
        Criteria criteria = Criteria.where("cm_sender_id").is(cmSendId).and("cm_club_id").is(clubId)
                .and("cm_message_typ").is(messageType).and("cm_show_message_typ").is(showMessageType)
                .and("cm_send_time").gte(time);
        query.addCriteria(criteria);
        return super.find(query).size();
    }
}
