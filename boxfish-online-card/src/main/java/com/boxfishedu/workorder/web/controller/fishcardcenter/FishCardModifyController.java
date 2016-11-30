package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardFreezeServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardModifyServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.web.param.CourseChangeParam;
import com.boxfishedu.workorder.web.param.StartTimeParam;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.FishCardDeleteParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/5/10.
 * 供内部接口更换课程
 * 换时间
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard")
public class FishCardModifyController {
    @Autowired
    private FishCardModifyServiceX fishCardModifyServiceX;

    @Autowired
    private AvaliableTimeServiceX avaliableTimeServiceX;

    @Autowired
    private FishCardFreezeServiceX fishCardFreezeServiceX;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 换老师
     * @param teacherChangeParam
     * @return
     */
    @RequestMapping(value = "/teacher", method = RequestMethod.PUT)
    public JsonResultModel changeTeacher(@RequestBody TeacherChangeParam teacherChangeParam) {
        return fishCardModifyServiceX.changeTeacher(teacherChangeParam);
    }

    

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public JsonResultModel deleteFishCard(@RequestBody FishCardDeleteParam fishCardDeleteParam) {
        if (!CollectionUtils.isEmpty(fishCardDeleteParam.getFishCardIds())) {
            fishCardModifyServiceX.deleteFishCardsByIds(fishCardDeleteParam);
        }
        if (!CollectionUtils.isEmpty(fishCardDeleteParam.getStudentIds())) {
            fishCardModifyServiceX.deleteFishCardsByStudentIds(fishCardDeleteParam);
        }
        return JsonResultModel.newJsonResultModel("success");
    }

    /**
     * 冻结金币换课的鱼卡
     */
    @RequestMapping(value = "/freeze", method = RequestMethod.PUT)
    public JsonResultModel freezeFishCard(@RequestBody java.util.Map<String, Long> map) {
        return fishCardFreezeServiceX.freeze(map.get("workOrderId"));
    }

    /**
     * 解冻对应的鱼卡
     */
    @RequestMapping(value = "/unfreeze", method = RequestMethod.PUT)
    public JsonResultModel unfreezeFishCard(@RequestBody java.util.Map<String, Long> map) {
        return fishCardFreezeServiceX.unfreeze(map.get("workOrderId"));
    }


    /**
     * 按照订单查询已上红包  和 未上 课程 数量
     */
    @RequestMapping(value = "/classesinfo", method = RequestMethod.PUT)
    public JsonResultModel getClassesInfo(@RequestBody java.util.Map<String, Long> map) {
        return fishCardFreezeServiceX.getFishCardsInfo(map.get("orderId"));
    }

    /**
     * 冻结金币换课的鱼卡
     */
    @RequestMapping(value = "/freezeAll", method = RequestMethod.PUT)
    public JsonResultModel freezeAllFishCards(@RequestBody java.util.Map<String, Long> map) {
        fishCardFreezeServiceX.freezeAllFishCards(map.get("orderId"));
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     * 更换上课时间(后台)
     *  app 更换上课时间  StudentAppRelatedChangeClassTimeController
     * @param startTimeParam 包含鱼卡id  开始时间
     * @return
     */
    @RequestMapping(value = "/changeStartTime", method = RequestMethod.POST)
    public JsonResultModel changeStartTime(@RequestBody StartTimeParam startTimeParam) {
        return fishCardModifyServiceX.changeStartTime(startTimeParam,false);
    }

    @RequestMapping(value = "/time/available/{workorder_id}/{date}", method = RequestMethod.GET)
    public JsonResultModel timeAvailable(@PathVariable("workorder_id") Long workorder_id, @PathVariable("date") String date) throws CloneNotSupportedException {
        return avaliableTimeServiceX.getTimeAvailableChangeTime(workorder_id, date);
    }

}
