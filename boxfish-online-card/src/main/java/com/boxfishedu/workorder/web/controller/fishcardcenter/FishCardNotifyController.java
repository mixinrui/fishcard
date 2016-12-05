package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.coursenotify.CourseChangeTimeNotifySerceX;
import com.boxfishedu.workorder.web.param.StudentNotifyParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 鱼卡提醒控制类
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard")
public class FishCardNotifyController {
    @Autowired
    private CourseChangeTimeNotifySerceX courseChangeTimeNotifySerceX;


    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 发信息(短信+app提醒 学生该时间)通知学生
     * @param studentNotifyParam
     * @return
     */
    @RequestMapping(value = "/notifyStudentitem", method = RequestMethod.POST)
    public JsonResultModel changeTeacher(@RequestBody StudentNotifyParam studentNotifyParam) {
        courseChangeTimeNotifySerceX.notifyStu(studentNotifyParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

}
