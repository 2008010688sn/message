package com.wp.casino.messageserver.controller;

import com.alibaba.fastjson.JSON;
import com.wp.casino.messageserver.common.SysLog;
import com.wp.casino.messageserver.dao.mysql.casino.MessageFriendAutoIdRepository;
import com.wp.casino.messageserver.utils.ApplicationContextProvider;
import com.wp.casino.messageserver.utils.ListLocation;
import com.wp.casino.messageserver.utils.RedisUtil;
import com.wp.casino.messagetools.monitor.MonitorResult;
import com.wp.casino.messagetools.monitor.ResultCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author sn
 * @date 2020/6/4 15:14
 */
@RestController
public class TestController {


    @Autowired
    RedisUtil redisUtil;



    @GetMapping(value = "/redis/test")
    public void redisTest(){
        System.out.println("------------------");
        redisUtil.setListData("snlist","a", ListLocation.LEFT);
        redisUtil.setListData("snlist","c", ListLocation.LEFT);
        redisUtil.setListData("snlist","b", ListLocation.LEFT);

        Object snlist = redisUtil.getListData("snlist", ListLocation.LEFT);
        Object snlist2 = redisUtil.getListData("snlist", ListLocation.LEFT);
        Object snlist3 = redisUtil.getListData("snlist", ListLocation.LEFT);

        System.out.println(snlist.toString()+"---"+snlist2.toString()+"---"+snlist3.toString());


    }

    @SysLog(value = "系统监控")
    @GetMapping(value = "/casino/monitor")
    public String monitor(){
        ResultCollector resultCollector = new ResultCollector();
        MonitorResult monitorResult=resultCollector.collect();
        String result=JSON.toJSONString(monitorResult);
        System.out.println(result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        Unsafe unsafe = getUnsafe();
        int anInt = unsafe.getInt(1);
        System.out.println(anInt);
    }

    public static Unsafe getUnsafe() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        return unsafe;
    }

}
