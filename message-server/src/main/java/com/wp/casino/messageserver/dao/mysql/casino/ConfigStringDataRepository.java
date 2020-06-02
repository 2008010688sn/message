package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.ConfigStringData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author sn
 * @date 2020/6/1 15:58
 */
public interface ConfigStringDataRepository extends JpaRepository<ConfigStringData,String> {


    @Query(value = "select cs_key, cs_value from tb_config_string_data",nativeQuery = true)
    List<ConfigStringData> findConfigStringDataList();
}
