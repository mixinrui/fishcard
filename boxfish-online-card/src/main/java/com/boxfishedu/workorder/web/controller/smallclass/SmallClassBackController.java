package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassBackServiceX;
import com.boxfishedu.workorder.servicex.smallclass.PublicClassInfoQueryServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassLogServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassQueryServiceX;
import com.boxfishedu.workorder.web.param.SmallClassParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hucl on 17/1/9.
 */
@RestController
@RequestMapping("/service/backend")
public class SmallClassBackController {
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private SmallClassQueryServiceX smallClassQueryServiceX;

    @Autowired
    private PublicClassInfoQueryServiceX publicClassInfoQueryServiceX;

    @Autowired
    private SmallClassBackServiceX smallClassBackServiceX;

    @Autowired
    private SmallClassLogServiceX smallClassLogServiceX;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/smallclass/slot", method = RequestMethod.GET)
    public JsonResultModel publicSlots(String roleId) {
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(Long.parseLong(roleId));
        List<TimeSlots> timeSlotses = dayTimeSlots.getDailyScheduleTime();
        return JsonResultModel.newJsonResultModel(timeSlotses);
    }

    @RequestMapping(value = "/smallclassitem", method = RequestMethod.POST)
    public JsonResultModel buildPublicClass(@RequestBody PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@buildPublicClass创建公开课,参数[{}]", publicClassBuilderParam);
        smallClassBackServiceX.configPublicClass(publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/smallclass", method = RequestMethod.PUT)
    public JsonResultModel updatePublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@updatePublicClass更新公开课,参数[{}]", publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     * 查询公开课(后台)
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/public/listitem", method = RequestMethod.GET)
    public JsonResultModel list(PublicFilterParam publicFilterParam, Pageable pageable) {
        return smallClassQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    /**
     * 查询小班课(后台)
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/small/listitem", method = RequestMethod.GET)
    public JsonResultModel smalllist(PublicFilterParam publicFilterParam, Pageable pageable) {
        return smallClassQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    /**
     * public_class_info 公开课明细查询
     * **smallClassId****
     * **studentId*******
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/public/list", method = RequestMethod.GET)
    public JsonResultModel publiclist(PublicFilterParam publicFilterParam, Pageable pageable) {
        return publicClassInfoQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    @RequestMapping(value = "/smallclass/{smallclass_id}", method = RequestMethod.DELETE)
    public JsonResultModel delete(@PathVariable("smallclass_id") Long smallClassId) {
        smallClassBackServiceX.delete(smallClassId);
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 提供状态的查询列表
     */
    @RequestMapping(value = "/smallclass/status/list", method = RequestMethod.GET)
    public JsonResultModel listAllStatus() {
        return smallClassQueryServiceX.listAllStatus();
    }


    @RequestMapping(value = "/classlog/details", method = RequestMethod.GET)
    public JsonResultModel listCardDetail(SmallClassParam smallClassParam, Pageable pageable) throws Exception {
        return smallClassLogServiceX.listSmallClassLogByUnlimitedUserCond(smallClassParam, pageable);
    }

    /**
     * 鱼卡后台 小班课列表获取补课学生列表
     * @param smallClassId
     * @return
     */
    @RequestMapping(value = "/stulist/{smallclass_id}", method = RequestMethod.GET)
    public JsonResultModel stulist(@PathVariable("smallclass_id") Long smallClassId) {
       return smallClassBackServiceX.getStudentList(smallClassId);
    }

    /**
     * 鱼卡后台 小班课列表查询所有补课学生level
     * @return
     */
    @RequestMapping(value = "/stulevellist", method = RequestMethod.GET)
    public JsonResultModel stulistforlevel() {
        return  smallClassBackServiceX.getStudentList();
    }



}
