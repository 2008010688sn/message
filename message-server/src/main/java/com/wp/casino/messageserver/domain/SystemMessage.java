package com.wp.casino.messageserver.domain;

import com.wp.casino.messageserver.common.AutoValue;
import com.wp.casino.messageserver.common.QueryBase;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Data
@Document(collection = "system_message")
public class SystemMessage<T>  implements Serializable{


    private static final long serialVersionUID = 8897204663201860790L;

    @Id
    private ObjectId id;

    @Field(value = "cm_sender_id")
    private long sendId;

    //
    @Field(value = "cm_message_typ")
    private Integer messageType;

    @Field(value = "cm_message_status")
    private Integer messageStatus;

    //
    @Field(value = "cm_show_message_typ")
    private Integer showMessageType;

    @Field(value = "cm_message")
    private T messageContext;

    //
    @Field(value = "cm_send_time")
    private Integer sendTime;

    @Field(value = "cm_recive_time")
    private Integer receiveTime;

    @Field(value = "cm_club_id")
    private Integer clubId;

    @Field(value = "cm_magic_id")
    private String magicId;

    @Field(value = "cm_global_status")
    private Integer globalStatus;

    @Field(value = "cm_operator")
    private Integer operator;

    @Field(value = "cm_expire_time")
    private Integer expireTime;

    @Field(value = "cm_title")
    private Integer title;

    @Field(value = "_auto_id")
    @AutoValue//字段自增
    private Integer autoId;

    @Field(value = "cm_reciver_ids")
    private List<ReceiveObj> receiveObjList;

    @Field(value = "createtime")
    private Integer createTime;

    public String getId() {
        if(id!=null){
            return id.toString();
        }else{
            return "";
        }
    }

    public void setId(String id) {
        this.id = new ObjectId(id);;
    }


}
