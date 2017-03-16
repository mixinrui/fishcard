package com.boxfishedu.workorder.service.transfishcard;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.bean.ChannelTypeEnum;
import com.boxfishedu.workorder.common.bean.RoleEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.SmallClassLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.web.param.fishcardcenetr.StudentSysParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiaozijun on 17/2/28.
 */

@Component
public class ComputeFishCardSMALL {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private SmallClassLogMorphiaRepository smallClassLogMorphiaRepository;


    @Transactional
    public void compute() {
        logger.info("@@computeFishCardNoticeStudentSystempublic_Begin");


        //1 (计算)获取满足条件的鱼卡数据(小班课 未发送过 3天内)
        List<WorkOrder> listWorks = getMyListWorks();

        if(CollectionUtils.isEmpty(listWorks)) return;

        //小班课对应中外教  key 小班课id  value :skuID
        Map<Long,Integer> smallClassesForCHMap =  Collections3.extractToMap(listWorks,"smallClassId","skuId");
        //小班课对应课程id  key 小班课id  value 课程id
        Map<Long,String>   smallClassesCourseIDMap =  Collections3.extractToMap(listWorks,"smallClassId","courseId");
        // 鱼卡id集合
        List<Long> ids = Lists.transform(listWorks, input -> input.getId());
        // 公开课id集合
        List<Long> smallClassIds = Lists.transform(listWorks,input -> input.getSmallClassId());// 公开课id集合
        smallClassIds = smallClassIds.stream().distinct().collect(Collectors.toList());

        //小班课id 和 学生id作为主健 发送消息的主体
        Map<String,SmallClassLog>  disSmallClassLog =    getPublicSmallClassLog(smallClassIds);

        if(CollectionUtils.isEmpty(disSmallClassLog)){
            return;
        }


        //3 (发送)发送数据
        logger.info("@@computeFishCardNoticeStudentSystempublic:发送数据[{}]", JSON.toJSON(disSmallClassLog));

        sendMessage(disSmallClassLog,smallClassesForCHMap,smallClassesCourseIDMap);

        // 4 (标记)对已经发送数据记录进行标记

        workOrderJpaRepository.setFixedIsComputeSendFor( new Short((short) 1),  ids);

        logger.info("@@computeFishCardNoticeStudentSystem1to1End");

    }


    // 公开课计算满足条件的公开课的条件信息
//    RoleEnum

    public Map<String,SmallClassLog> getPublicSmallClassLog(List<Long> smallClassIds){
        List<SmallClassLog> smallClassLogs =smallClassLogMorphiaRepository.querySmallAndPublicLog(smallClassIds, RoleEnum.STUDENT.name(),getStatus());
        Map<String,SmallClassLog>  distinctClassLog = Maps.newHashMap();
        //去处重复消息
        smallClassLogs.stream().forEach(smallClassLog -> {
            distinctClassLog.put(smallClassLog.getSmallClassId()+"_"+smallClassLog.getStudentId(),smallClassLog);
        });
        return distinctClassLog;

    }

    /**
     *
     * @param disSmallClassLog
     * @param smallClassesForCHMap
     * @param smallClassesCourseIDMap
     */
    private void sendMessage(Map<String,SmallClassLog> disSmallClassLog,Map<Long,Integer>   smallClassesForCHMap,Map<Long,String>   smallClassesCourseIDMap){

        disSmallClassLog.forEach((key,smallClassLog)->{

                    StudentSysParam studentSysParam = new StudentSysParam();
                    if ( checkISForeign(smallClassesForCHMap.get(smallClassLog.getSmallClassId()))) { //判断外教
                        studentSysParam.setChannel_type(ChannelTypeEnum.COURSE_FOREIGN_4P);
                    }else {
                        studentSysParam.setChannel_type(ChannelTypeEnum.COURSE_CHINESE_4P);
                    }

                    String courseId = smallClassesCourseIDMap.get(smallClassLog.getSmallClassId());
                    //发送/ 添加日志
                    if(null!=studentSysParam.getChannel_type() && !StringUtils.isEmpty(courseId)){
                        teacherStudentRequester.pushStudentSysOnlineMsg(smallClassLog.getStudentId(),courseId,studentSysParam);
                    }

                }
        );

    }


    //计算满足条件的鱼卡课程
    public List<WorkOrder> getMyListWorks(){
        return  workOrderJpaRepository.findByComputeSendDatasPUBLIC(getBeginDate(),getBaseNowDate(), ClassTypeEnum.SMALL.name());
    }

    //PublicClassInfoStatusEnum
    private List<Integer> getStatus() {
        List<String> status = Arrays.asList(urlConf.getFishcard_public_small_status().split(","));
        List<Integer> statusInteger = Lists.newArrayList();
        for(String ss : status){
            statusInteger.add(Integer.parseInt(ss));
        }
        return statusInteger;
    }

    public boolean checkISForeign(Integer skuId){
        return TeachingType.WAIJIAO.getCode() == skuId;
    }

    public Date getBeginDate(){
        return DateTime.now().minusDays(2).toDate();
    }

    public Date getBaseNowDate(){
        return DateTime.now().minusMinutes(40).toDate();
    }
}
