package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/6/1 15:56
 */
@Data
@Entity
@Table(name = "tb_config_string_data")
public class ConfigStringData implements Serializable {

    @Id
    String cs_key;

    String cs_value;


}
