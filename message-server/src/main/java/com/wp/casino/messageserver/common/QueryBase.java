package com.wp.casino.messageserver.common;

import lombok.Data;
import org.springframework.data.annotation.Transient;

@Data
public class QueryBase {

    /**
     * 页数
     */
    @Transient
    protected int page=1;

    /**
     * 获取一页行数
     * */
    @Transient
    protected int limit=10;


}
