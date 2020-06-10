package com.wp.casino.messageserver.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 模拟序列类
 *
 */
@Getter
@Setter
@ToString
@Document(collection = "mongo_collection_info")
public class SeqInfo {

    @Id
    private String id;// 主键

    @Field(value = "collection")
    private String collName;// 集合名称

    @Field(value = "auto_id")
    private Long seqId;// 序列值

}