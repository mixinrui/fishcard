package com.boxfishedu.workorder.web.controller.smallclass;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.monitor.MonitorUserService;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassSuperStuServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.SmallClassSuperStuParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 小班课 超级学生用户
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
@SuppressWarnings("ALL")
public class
SmallClassSuperStudentController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RepeatedSubmissionChecker checker;

    @Autowired
    private SmallClassSuperStuServiceX smallClassSuperStuServiceX;

    @Autowired
    private MonitorUserService monitorUserService;

    /**
     * 学生端批量选择课程的接口
     * 1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     */
    @RequestMapping(value = "/class/generator", method = RequestMethod.POST)
    public JsonResultModel classGenerator(@RequestBody SmallClassSuperStuParam smallClassSuperStuParam, Long userId) {
        smallClassSuperStuParam.setStudentId(userId);
        JsonResultModel jsonResultModel ;
        MonitorUser monitorUser =monitorUserService.checkMonitorUser(userId);
        if(Objects.isNull(monitorUser)){
            smallClassSuperStuParam.setMessage("该用户被冻结或者不是超级用户,不能监控小班课");
            return JsonResultModel.newJsonResultModel(smallClassSuperStuParam);
        }
        try {
            if (checker.checkRepeatedSubmission(smallClassSuperStuParam.getId())) {
                throw new RepeatedSubmissionException("正在提交当中,请稍候...");
            }

            WorkOrder workOrder = smallClassSuperStuServiceX.checkWorkOrder(smallClassSuperStuParam);
            if (!Objects.isNull(workOrder)) {
                logger.info("@classGeneratorHasClass:[{}]", JSON.toJSON(smallClassSuperStuParam));
                smallClassSuperStuParam.setWorkOrderId(workOrder.getId());
            } else {
                logger.info("@classGeneratorToHasClass:[{}]", JSON.toJSON(smallClassSuperStuParam));
                workOrder = smallClassSuperStuServiceX.generatorCourse(smallClassSuperStuParam);
                smallClassSuperStuParam.setWorkOrderId(workOrder == null ? null : workOrder.getId());
                smallClassSuperStuParam.setMessage(workOrder == null ? "未生成课程或请重试" : "OK");
            }
            checker.evictRepeatedSubmission(smallClassSuperStuParam.getId());
            return JsonResultModel.newJsonResultModel(smallClassSuperStuParam);
        } catch (Exception e) {
            evictRepeatedSubmission(e, smallClassSuperStuParam.getId());
            throw e;
        }
    }


    private void evictRepeatedSubmission(Exception e, Long orderId) {
        if (!(e instanceof RepeatedSubmissionException)) {
            checker.evictRepeatedSubmission(orderId);
        }
    }

}
