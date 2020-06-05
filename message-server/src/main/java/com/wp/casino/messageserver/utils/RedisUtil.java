package com.wp.casino.messageserver.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
public class RedisUtil  {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置某个Key的过期时间
     * @param key
     * @param timeout
     * @param unit
     */
    public void setExpire(String key, long timeout, TimeUnit unit) {
        //设置超时时间10秒 第三个参数控制时间单位，详情查看TimeUnit
        redisTemplate.expire(key,timeout,unit);
    }

    /**
     * 删除某个Key
     * @param key
     * @param timeout
     * @param unit
     */
    public void remove(String key, long timeout, TimeUnit unit) {
        //设置超时时间10秒 第三个参数控制时间单位，详情查看TimeUnit
        redisTemplate.delete(key);
    }
    
    public void setData(String key,String value) {
        stringRedisTemplate.opsForValue().set(key,value);
    }

    public String getData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    
    /**
     * 操作list的操作
     * @param key
     * @param value
     * @param location
     */
    public void setListData(String key,String value,ListLocation location) {
        if (ListLocation.LEFT.equals(location)) {
            // 也可以这么设置 redisTemplate.boundListOps(key).leftPush(key,value);
            redisTemplate.opsForList().leftPush(key,value);
        } else {
            redisTemplate.opsForList().rightPush(key,value);
        }
    }

    /**
     * 获得list的值
     * @param key
     * @param location
     * @return
     */
    public Object getListData(String key,ListLocation location) {
        if (ListLocation.LEFT.equals(location)) {
            return redisTemplate.opsForList().leftPop(key);
        } else {
            return redisTemplate.opsForList().rightPop(key);
        }
    }


    /**
     * 获得list的值
     * @param key
     * @return
     */
    public void removeListData(String key) {
        Long size = redisTemplate.opsForList().size(key);
        redisTemplate.opsForList().remove(key,size,null);
    }

    /**
     * 针对map类型的数据操作-设置hash的值
     * @param key
     * @param hashKey
     * @param value
     */
    public void setHashData(String key,String hashKey,String value) {
        redisTemplate.opsForHash().put(key,hashKey,value);
    }

    /**
     * 针对map类型的数据操作-设置hash的值
     * @param key
     * @param map
     */
    public void setHashData(String key, Map map) {
        redisTemplate.opsForHash().putAll(key,map);
    }

    /**
     * 获得hash的值
     * @param key
     * @param hashKey
     * @return
     */
    public Object getHashData(String key,String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * zset类型数据操作-设置ZSet的值
     * @param key
     * @param value
     * @param source
     */
    public void setZsetData(String key,String value,Double source){
        redisTemplate.opsForZSet().add(key,value,source);
    }

    /**
     * zset类型数据操作-获得ZSet中的值
     * @param key
     * @param source1
     * @param source2
     * @return
     */
    public Set getZsetData(String key,Double source1,Double source2){
        Set set = redisTemplate.opsForZSet().rangeByScore(key, source1, source2);
        return set;
    }

    /**
     * set类型数据操作-设置set的值
     * @param key
     * @param value
     */
    public void setSetData(String key,String value){
        redisTemplate.opsForSet().add(key,value);
    }

    /**
     * set类型数据操作-获得set的值
     * @param key
     * @return
     */
    public Object getSetData(String key){
        Object pop = redisTemplate.opsForSet().pop(key);
        return pop;
    }


    public Object getSetDataCount(String key){
        Long size = redisTemplate.opsForSet().size(key);
        return size;
    }

    public void removeSetDataCount(String key){
        redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 设置Geo的值
     * @param key
     * @param location
     */
    public void setGeoData(String key, RedisGeoCommands.GeoLocation <Object> location){
        redisTemplate.opsForGeo().add(key,location);
    }

    /**
     * 获得Geo的值
     * @param key
     * @param circle
     * @return
     */
    public GeoResults getGeoData(String key, Circle circle){
        GeoResults radius = redisTemplate.opsForGeo().radius(key, circle);
        return radius;
    }

    /**
     * 设置HyperLogLog的值
     * @param key
     * @param objs
     */
    public void setHyperLogLogData(String key,Object... objs){
        redisTemplate.opsForHyperLogLog().add(key,objs);
    }

    /**
     * 获得HyperLogLog的值
     * @param key
     * @return
     */
    public Long getHyperLogLogData(String key){
        return redisTemplate.opsForHyperLogLog().size(key);
    }
    
    /**
     * 设置超时配置
     * @param key
     * @param value
     * @param timeOut
     */
    public void setDataTimeOut(String key,String value,Long timeOut) {
        stringRedisTemplate.opsForValue().set(key,value, timeOut, TimeUnit.MILLISECONDS);
    }


}
