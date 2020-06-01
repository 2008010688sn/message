package com.wp.casino.messageserver.dao.mongodb.message;

import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.domain.ClubMsgCount;
import com.wp.casino.messageserver.domain.QuerySystemMessage;
import com.wp.casino.messageserver.domain.SystemMessage;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
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
        Criteria cri = Criteria.where("cm_reciver_ids.$.id").is(plyGuid).and("cm_reciver_ids.$.status").is(MsgConstants.MSG_STATUS_UNREAD);
        if (clubId > 0) {
            cri.and("cm_club_id").is(clubId);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(cri),
                Aggregation.group("cm_club_id").count().as("count"),
                Aggregation.project("clubId", "count")
        );
        AggregationResults<ClubMsgCount> outputType = mongoTemplate
                .aggregate(aggregation, "system_message", ClubMsgCount.class);
        return outputType.getMappedResults();
    }

    /**
     * 修改code值
     * @param messageId
     * @param code
     * @param whoGuid
     */
    public void updateMsgCode(long messageId, int code, long whoGuid) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.where("_auto_id").is(messageId).and("cm_reciver_ids.$.id")
                .is(whoGuid).and("cm_reciver_ids.$.status").ne(MsgConstants.MSG_STATUS_DELETED);
        query.addCriteria(criteria);
        Update update = new Update().set("cm_message.code", code).set("cm_global_status", -1).set("cm_operator", whoGuid)
                .set("cm_magic_id", "").set("cm_reciver_ids.$.status", MsgConstants.MSG_STATUS_READ);
        super.update(query, update);
    }
}
