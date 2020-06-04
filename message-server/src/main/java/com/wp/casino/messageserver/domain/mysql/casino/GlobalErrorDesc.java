package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/6/1 15:43
 */
@Data
@Entity
@Table(name = "tb_global_error_desc")
public class GlobalErrorDesc implements Serializable {

    @Id
    Integer gdAutoId;

    String gdEnUsDesc;

    String gdZhCnDesc;

    String gdZhTwDesc;

}
