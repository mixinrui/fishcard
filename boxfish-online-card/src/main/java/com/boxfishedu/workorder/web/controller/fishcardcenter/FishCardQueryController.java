package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardQueryServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 此接口主要提供给鱼卡中心的管理后台使用,主要包括:鱼卡列表,换课,换教师,换时间
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard")
public class FishCardQueryController {
    @Autowired
    private FishCardQueryServiceX fishCardQueryServiceX;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private WorkOrderService workOrderService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());


    /**
     * 用户id强制要求时候的查询
     * @param fishCardFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/limit/list", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByLimitUser(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByCond(fishCardFilterParam,pageable);
    }

    /**
     * 供小马调用  勿动
     * @param fishCardFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondfirst(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }

    /**
     * 用户id不做限制的查询  鱼卡管理
     */
    @RequestMapping(value = "/listitem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }


    /**
     * 用户id不做限制的查询 鱼卡管理(中教)
     */
    @RequestMapping(value = "/listzjitem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondZJ(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }

    /**
     * 用户id不做限制的查询 鱼卡管理(外教)
     */
    @RequestMapping(value = "/listwjitem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondWJ(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }

    /**
     * 用户id不做限制的查询 鱼卡管理(补课)
     */
    @RequestMapping(value = "/listbkitem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondBK(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }

    /**
     * 用户id不做限制的查询 鱼卡管理(退款)
     */
    @RequestMapping(value = "/listtkitem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondTK(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }

    /**
     * 用户id不做限制的查询 鱼卡管理(确认状态)
     */
    @RequestMapping(value = "/listqritem", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCondQR(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }


    /**
     * 提供状态的查询列表
     */
    @RequestMapping(value = "/status/list", method = RequestMethod.GET)
    public JsonResultModel listAllStatus(){
        return fishCardQueryServiceX.listAllStatus();
    }

    @RequestMapping(value = "/{card_id}/details", method = RequestMethod.GET)
    public JsonResultModel listCardDetail(@PathVariable("card_id") Long cardId) throws Exception {
        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(cardId);
        return JsonResultModel.newJsonResultModel(workOrderLogs);
    }


    /**
     * 获取课程类型(前段提供)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/courseType/list", method = RequestMethod.GET)
    public JsonResultModel listCourseType() throws Exception {
        return JsonResultModel.newJsonResultModel( CourseTypeEnum.values());
    }

    /**
     * 获取订单类型(前段提供)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/orderType/list", method = RequestMethod.GET)
    public JsonResultModel listOrderType() throws Exception {
        JSONObject json = new JSONObject();
        for (OrderChannelDesc v : OrderChannelDesc.values()) {
            if(!v.getCode().equals( OrderChannelDesc.STANDARD.getCode())){
                json.put(v.getCode(),v.getDesc());
            }
        }
        return JsonResultModel.newJsonResultModel( json);
    }


    /**
     * 退款状态类型
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rechargeType/list", method = RequestMethod.GET)
    public JsonResultModel rechargeType() throws Exception {
        return JsonResultModel.newJsonResultModel(FishCardChargebackStatusEnum.varMapout);
    }


    /**
     * 获取某个学生的所有的鱼卡信息
     * @param studentId
     * @return
     */
    @RequestMapping(value = "/{studentId}/studentlist",method = RequestMethod.GET)
    public JsonResultModel getFishCardListByStudentId(@PathVariable("studentId") Long studentId){
        return JsonResultModel.newJsonResultModel(workOrderService.findByStudentId(studentId));
    }

    /**
     * 获取群组关系
     * @param fishCardFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/getGroupInfo", method = RequestMethod.GET)
    public JsonResultModel getGroupInfo(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return JsonResultModel.newJsonResultModel(fishCardQueryServiceX.getGroupInfo(fishCardFilterParam.getId()));
    }

    /**
     *
     * @param cardId
     * @return
     */
    @RequestMapping(value = "/getNetPing", method = RequestMethod.GET)
    public JsonResultModel getNetPing(Long cardId, Pageable pageable) {
        return null;
//        return JsonResultModel.newJsonResultModel(fishCardQueryServiceX.getGroupInfo(fishCardFilterParam.getId()));
    }
}
