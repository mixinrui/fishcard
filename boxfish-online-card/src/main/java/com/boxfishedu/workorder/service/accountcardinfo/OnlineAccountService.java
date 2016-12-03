package com.boxfishedu.workorder.service.accountcardinfo;

import com.boxfishedu.workorder.common.threadpool.AsyncNotifyPoolManager;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.dao.mongo.OnlineAccountSetMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.OnlineAccountSet;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 16/9/24.
 * 判断用户是否为在线运营的用户,不是直接返回空
 */
@Component
public class OnlineAccountService {
    @Autowired
    private @Qualifier("teachingServiceRedisTemplate") StringRedisTemplate redisTemplate;

    @Autowired
    private OnlineAccountSetMorphiaRepository onlineAccountSetMorphiaRepository;

    @Autowired
    private AsyncNotifyPoolManager asyncNotifyPoolManager;

    private final String ONLINE_ACCOUNT_KEY="account:online";

    private final String SYNC_KEY="syncMongo2Redis";

    @Autowired
    private ThreadPoolManager threadPoolManager;

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    //更新mongo,更新redis
    public void add(Long studentId){
        try {
            asyncNotifyPoolManager.execute(new Thread(()->{onlineAccountSetMorphiaRepository.add(studentId);}));
            logger.debug("@OnlineAccountService#add#向redis中增加用户信息#user[{}]", studentId);
            redisTemplate.opsForSet().add(ONLINE_ACCOUNT_KEY, studentId.toString());
        }
        catch (Exception ex){
            logger.error("@OnlineAccountService#add#exception#user#{}",studentId,ex);
        }
    }

    //如果redis出问题,直接查mongo
    public boolean isMember(Long studentId){
        boolean result=false;
        //分流;mongo效率更低
        if(studentId%3==0){
            logger.debug("<<<@OnlineAccountService#isMember#user:[{}]########<<mongo>>>>",studentId);
            return (onlineAccountSetMorphiaRepository.queryByStudentId(studentId)!=null);
        }
        try {
            logger.debug("<<<@OnlineAccountService#isMember#user:[{}]########<<redis>>>>",studentId);
            if(redisTemplate.hasKey(ONLINE_ACCOUNT_KEY)) {
                result = redisTemplate.opsForSet().isMember(ONLINE_ACCOUNT_KEY, studentId.toString());
            }
            else{
                //将mongo的数据同步到redis中去
                if(redisTemplate.opsForValue().setIfAbsent(SYNC_KEY,Boolean.TRUE.toString())) {
                    threadPoolManager.execute(new Thread(() -> {
                        syncMongo2Redis();
                    }));
                }
                return (onlineAccountSetMorphiaRepository.queryByStudentId(studentId)!=null);
            }
            return result;
        }
        catch (Exception ex){
            logger.debug("@OnlineAccountService#isMember#exception#redis#user:[{}]",studentId,ex);
            return (onlineAccountSetMorphiaRepository.queryByStudentId(studentId)!=null);
        }
    }

    public void syncMongo2Redis(){
        Long mongoCount=onlineAccountSetMorphiaRepository.count();
        Long redisCount=redisTemplate.opsForSet().size(ONLINE_ACCOUNT_KEY);
        logger.debug("@syncMongo2Redis#mongo的个数:{},redis的个数:{}",mongoCount,redisCount);
        if(mongoCount.longValue()>redisCount.longValue()) {
            logger.info("@syncMongo2Redis###############mongo#notif#equal#redis,同步数据开始");
            List<OnlineAccountSet> allAccounts = onlineAccountSetMorphiaRepository.getAll();
            logger.debug("@syncMongo2Redis#需要同步的数据[{}]",allAccounts.size());
            for(OnlineAccountSet onlineAccountSet:allAccounts){
                if(!Objects.isNull(onlineAccountSet.getStudentId())) {
                    logger.debug("=>{}", onlineAccountSet.getStudentId());
                    redisTemplate.opsForSet().add(ONLINE_ACCOUNT_KEY, onlineAccountSet.getStudentId().toString());
                }
            }
        }
        logger.debug(">>>>>>>同步完成");
        redisTemplate.delete(SYNC_KEY);
    }
}
