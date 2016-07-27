package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionPickerServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionServiceX;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/6/17.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/makeup")
public class FishCardMakeUpController {
    @Autowired
    private MakeUpLessionPickerServiceX makeUpLessionPickerServiceX;

    @Autowired
    private MakeUpLessionServiceX makeUpLessionServiceX;


    /**
     *获取补课的可选时间列表
     */
    @RequestMapping(value = "/{workorder_id}/slots", method = RequestMethod.GET)
    public JsonResultModel listAvaliableSlots(@PathVariable("workorder_id") Long fishcardId) {
       return makeUpLessionPickerServiceX.listAvaliableSlots(fishcardId);
    }

    /**
     *点击补课触发操作
     */
    @RequestMapping(value = "/modification", method = RequestMethod.POST)
    public JsonResultModel makeUpCourse(@RequestBody MakeUpCourseParam makeUpCourseParam){
        makeUpLessionServiceX.makeUpCourse(makeUpCourseParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 更改鱼卡状态(jiaozijun新增)
     */
    @RequestMapping(value = "/fishcard/status/change", method = RequestMethod.POST)
    public JsonResultModel fishcardStatusChange(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.fishcardStatusChange(makeUpCourseParam);
    }


    /**
     * 删除鱼卡(用于鱼卡后台)
     * @param makeUpCourseParam
     * @return
     */
    @RequestMapping(value = "/fishcard/delete", method = RequestMethod.POST)
    public JsonResultModel fishcardDelete(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.deleteFishCard(makeUpCourseParam);
    }

}
