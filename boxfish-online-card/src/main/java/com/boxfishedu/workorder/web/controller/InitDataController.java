package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ContinousAbsenceMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by hucl on 16/9/19.
 * 用于初始化程序相关的数据
 */
@CrossOrigin
@RestController
@RequestMapping("/init")
public class InitDataController {
    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private ContinousAbsenceMorphiaRepository continousAbsenceMorphiaRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private AbsenceDealService absenceDealService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private AccountCardInfoService accountCardInfoService;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/absencenum", method = RequestMethod.POST)
    public JsonResultModel init() {
        List<Long> userIds = workOrderJpaRepository.findDistinctUsersFromWorkOrder();
        for (Long userId : userIds) {
            logger.info("###############################################正在处理用户============[{}]",userId);
            List<WorkOrder> workOrders = workOrderJpaRepository.findByStudentIdAndEndTimeLessThanOrderByStartTimeDesc(userId,new Date());
            for (WorkOrder workOrder : workOrders) {
                if (workOrder.getStatus() != FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
                    break;
                }
                if (!workOrder.getService().getComboType().equals(ComboTypeEnum.EXCHANGE.toString())) {
                    break;
                }
                ContinousAbsenceRecord continousAbsenceRecord = continousAbsenceMorphiaRepository.queryByStudentIdAndComboType(userId, ComboTypeEnum.EXCHANGE.toString());
                if(null==continousAbsenceRecord){
                    continousAbsenceRecord=new ContinousAbsenceRecord();
                    continousAbsenceRecord.setCreateTime(new Date());
                    continousAbsenceRecord.setContinusAbsenceNum(1);
                    continousAbsenceRecord.setComboType(ComboTypeEnum.EXCHANGE.toString());
                    continousAbsenceRecord.setStudentId(userId);
                    continousAbsenceMorphiaRepository.save(continousAbsenceRecord);
                }
                else{
                    continousAbsenceRecord.setContinusAbsenceNum(continousAbsenceRecord.getContinusAbsenceNum()+1);
                    absenceDealService.updateCourseAbsenceNum(continousAbsenceRecord);
                }
            }
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/home", method = RequestMethod.POST)
    public JsonResultModel initHomePage(){
        List<Long> studentIds=serviceJpaRepository.findDistinctUsersFromService();
        studentIds.forEach(studentId->dataCollectorService.updateBothChnAndFnItemAsync(studentId));
        return JsonResultModel.newJsonResultModel("ok");
    }


}
