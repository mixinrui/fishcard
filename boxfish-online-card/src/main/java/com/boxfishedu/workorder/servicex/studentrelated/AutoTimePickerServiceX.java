package com.boxfishedu.workorder.servicex.studentrelated;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.MD5Util;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.instantclass.SmallClassQueryService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.SmallClassAddStuParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.SmallClassAddStuTransParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 系统自动生成鱼卡 及其 鱼卡的小班课id
 */
@Component
public class AutoTimePickerServiceX {


    @Value("${parameter.small_class_size}")
    private Integer smallClassSize;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private UrlConf urlConf;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public JsonResultModel  addStudentForSmallClass(SmallClassAddStuParam smallClassAddStuParam){
        //根据小班课获取学生鱼卡信息 查看班级是否已满
        List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClassAddStuParam.getId());
        if(CollectionUtils.isEmpty(workOrders) || workOrders.size()>=smallClassSize ||
                (smallClassAddStuParam.getStudentIds().size()+workOrders.size())>smallClassSize ){
            return JsonResultModel.newJsonResultModel("fail");
        }

        SmallClass smallClass= smallClassJpaRepository.findOne(smallClassAddStuParam.getId());

        if(smallClass.getRoleId().intValue()== TeachingType.WAIJIAO.getCode()){
            smallClassAddStuParam.setComboType(ComboTypeToRoleId.FSCF.name());
        }
        if(smallClass.getRoleId().intValue()== TeachingType.ZHONGJIAO.getCode()){
            smallClassAddStuParam.setComboType(ComboTypeToRoleId.FSCC.name());
        }

        logger.info(String.format( "addStudentForSmallClass:beforerequestBody:%s",JSON.toJSON(smallClassAddStuParam)));

        for(Long studentId:smallClassAddStuParam.getStudentIds()){
            SmallClassAddStuTransParam smallClassAddStuTransParam =new SmallClassAddStuTransParam();
            smallClassAddStuTransParam.setUserId(studentId);  //1
            // couponCode userId smallClassKey
            smallClassAddStuTransParam.setSign(MD5Util.encrypt(urlConf.getOrder_check_couponCode()+studentId+urlConf.getOrder_check_small_class_key())); //2
            smallClassAddStuTransParam.setCouponCode(urlConf.getOrder_check_couponCode()); //3
            smallClassAddStuTransParam.setSmallClassId(smallClassAddStuParam.getId());     //4
            //向订单发送请求

            teacherStudentRequester.requestToCreateOrder(smallClassAddStuTransParam);
        }


        return JsonResultModel.newJsonResultModel("success");

    }

}
