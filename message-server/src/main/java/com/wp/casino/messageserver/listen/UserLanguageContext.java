package com.wp.casino.messageserver.listen;

import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.domain.mysql.casino.ConfigGlobalString;
import com.wp.casino.messageserver.domain.mysql.casino.GlobalErrorDesc;
import com.wp.casino.messageserver.service.MessageClient;
import com.wp.casino.messageserver.utils.ClubDataUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sn
 * @date 2020/5/19 17:18
 */
@Component
@Slf4j
@Order(3)
public class UserLanguageContext implements ApplicationRunner {

    public  static ConcurrentHashMap<String, String> enMaps=new ConcurrentHashMap<>(0);
    public  static ConcurrentHashMap<String, String> cnMaps=new ConcurrentHashMap<>(0);
    public  static ConcurrentHashMap<String, String> twMaps=new ConcurrentHashMap<>(0);

    public  static ConcurrentHashMap<Integer, String> enErrMaps=new ConcurrentHashMap<>(0);
    public  static ConcurrentHashMap<Integer, String> cnErrMaps=new ConcurrentHashMap<>(0);
    public  static ConcurrentHashMap<Integer, String> twErrMaps=new ConcurrentHashMap<>(0);


    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<ConfigGlobalString> list =  ClubDataUtil.findConfigGlobalList();
        List<GlobalErrorDesc> errlist =  ClubDataUtil.findGlobalErrorDescList();
        if (list != null && list.size() > 0) {
            for (ConfigGlobalString cgs : list) {
                switch (cgs.getGsLang()) {
                    case MsgConstants.EN_US_LANGUAGE :
                        enMaps.put(cgs.getGsName(), cgs.getGsContext());
                    case MsgConstants.ZH_CN_LANGUAGE :
                        cnMaps.put(cgs.getGsName(), cgs.getGsContext());
                    case MsgConstants.ZH_TW_LANGUAGE:
                        twMaps.put(cgs.getGsName(), cgs.getGsContext());
                }
            }
        }

        if (errlist != null && errlist.size() > 0) {
            for (GlobalErrorDesc gd : errlist) {
                enErrMaps.put(gd.getGdAutoId(), gd.getGdEnUsDesc());
                cnErrMaps.put(gd.getGdAutoId(), gd.getGdZhCnDesc());
                twErrMaps.put(gd.getGdAutoId(), gd.getGdZhTwDesc());
            }
        }
    }
}
