package com.boxfishedu.workorder.service.transfishcard;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.bean.ChannelTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.web.param.fishcardcenetr.StudentSysParam;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by jiaozijun on 17/3/1.
 */

@Component
public class ComputeFishCardOne2One{


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;


    // 计算在线1对1 课程  ,看最终鱼卡状态 每  10分钟轮训
    @Transactional
    public void compute()  {

        logger.info("@@computeFishCardNoticeStudentSystem1to1Begin");
        //1 满足条件的鱼卡
        List<Integer> listStatus = getStatus();

        //2 (计算)获取满足条件的鱼卡数据
        List<WorkOrder>  listWorks = getMyListWorks(listStatus);

        if(CollectionUtils.isEmpty(listWorks)) return;

        List<Long> ids = Lists.transform(listWorks, input -> input.getId());



        //3 (发送)发送数据
        logger.info("@@computeFishCardNoticeStudentSystem1to1compute:发送数据[{}]", JSON.toJSON(ids));

        sendMessage(listWorks);

        // 4 (标记)对已经发送数据记录进行标记

        workOrderJpaRepository.setFixedIsComputeSendFor( new Short((short) 1),  ids);

        logger.info("@@computeFishCardNoticeStudentSystem1to1End");
    }

    //计算满足条件的鱼卡课程
    public List<WorkOrder> getMyListWorks(List<Integer> listStatus){
        return  workOrderJpaRepository.findByComputeSendDatas(getBeginDate(),getBaseNowDate(), listStatus, ClassTypeEnum.INSTNAT.name());
    }

    private void sendMessage(List<WorkOrder> listWorks){

        listWorks.stream().forEach(wo -> {
            StudentSysParam studentSysParam = new StudentSysParam();

            if ( checkISForeign(wo.getSkuId())) { //判断外教
                studentSysParam.setChannel_type(ChannelTypeEnum.COURSE_FOREIGN_1P);
            }else {
                studentSysParam.setChannel_type(ChannelTypeEnum.COURSE_CHINESE_1P);
            }

            if(null!=studentSysParam.getChannel_type()){
                //发送/ 添加日志
                teacherStudentRequester.pushStudentSysOnlineMsg(wo.getStudentId(),wo.getCourseId(),studentSysParam);
            }

        });


    }


    // 获取 过滤的鱼卡状态
    private List<Integer> getStatus() {
        List<String> status = Arrays.asList(urlConf.getFishcard_status().split(","));
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
