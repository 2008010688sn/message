package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.ConfigGlobalString;
import com.wp.casino.messageserver.domain.mysql.casino.MessageFriendList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author sn
 * @date 2020/6/1 15:47
 */
public interface ConfigGlobalStringRepository extends JpaRepository<ConfigGlobalString,Integer> {

    @Query(value = "select gs_auto_id,gs_lang,gs_name,gs_context from tb_config_global_string order by gs_name ASC, gs_lang asc ",nativeQuery = true)
    List<ConfigGlobalString> findConfigGlobalStringList();

}
