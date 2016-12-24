package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.bean.AppPointRecordEventEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.requester.DataAnalysisRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.requester.resultbean.NetAnalysisBean;
import com.boxfishedu.workorder.requester.resultbean.NetSourceBean;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import com.boxfishedu.workorder.web.view.base.GroupInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.bean.SkuTypeEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardQueryService;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/5/9.
 */
@Component
public class FishCardQueryServiceX {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private FishCardQueryService fishCardQueryService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private DataAnalysisRequester dataAnalysisRequester;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public JsonResultModel listFishCardsByUnlimitedUserCond(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        workOrderService.processDateParam(fishCardFilterParam);
        List<WorkOrder> workOrderList = fishCardQueryService.filterFishCards(fishCardFilterParam, pageable);
        Long count = fishCardQueryService.filterFishCardsCount(fishCardFilterParam);
        Page<WorkOrder> page = new PageImpl(workOrderList, pageable, count);

        if(null==fishCardFilterParam.getShowGroup() ||  !fishCardFilterParam.getShowGroup()){
            try {
                trimPage(page);
            }catch (Exception e){
                e.printStackTrace();
                logger.info("listFishCardsByUnlimitedUserCond");
            }
        }

        return JsonResultModel.newJsonResultModel(page);
    }


    /**
     * 拼装房间号
     *
     * @param page
     */
    private void trimPage(Page<WorkOrder> page) {
        if (page == null || null == page.getContent())
            return;
        List<WorkOrder> list = ((List<WorkOrder>) page.getContent());
        List<Long> fishcards = Lists.newArrayList();
        list.stream().forEach(workOrder -> fishcards.add(workOrder.getId()));

        FishCardGroupsInfo[] fishCardGroupsInfos = teacherStudentRequester.getFishcardMessage(fishcards);

        if(null ==fishCardGroupsInfos|| fishCardGroupsInfos.length<1)
            return;


        ((List<WorkOrder>) page.getContent()).stream().forEach(workOrder -> {
            getGroupStatus(fishCardGroupsInfos,workOrder);
        });


    }


    public String EncoderByMd5(String str) {
        try {
            return   DigestUtils.md5Hex(str.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 根据鱼卡获取群组信息
     * @param workOrderId
     * @return
     */
    public GroupInfo  getGroupInfo(Long workOrderId){
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        FishCardGroupsInfo[] fishCardGroupsInfos = teacherStudentRequester.getFishcardMessage(Lists.newArrayList(workOrderId));
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setWorkOrderId(workOrderId);

        String [] infoMissIn = {  EncoderByMd5(workOrder.getStudentId().toString()) ,  EncoderByMd5( workOrder.getTeacherId().toString())   };

        groupInfo.setInfoMissIn(infoMissIn);
        if(null!=fishCardGroupsInfos && fishCardGroupsInfos.length==1){
             groupInfo.setInfoMissOut(fishCardGroupsInfos[0].getMemberAccount());
             groupInfo.setGroupId( fishCardGroupsInfos[0].getGroupId() );
             groupInfo.setChatRoomId(fishCardGroupsInfos[0].getChatRoomId());
             getGroupStatus(fishCardGroupsInfos,workOrder);
             groupInfo.setInfoMissIn(infoMissIn);
             groupInfo.setNormal(workOrder.isNormal());
        }
        return groupInfo;
    }



    private void getGroupStatus( FishCardGroupsInfo   [] fishCardGroupsInfos , WorkOrder workOrder){

        if(workOrder.getStudentId()==0 || workOrder.getTeacherId() ==0 || workOrder.getStudentId()==null || workOrder.getTeacherId() ==null ){
            workOrder.setNormal(false);
            return;
        }
        for (FishCardGroupsInfo fishCardGroupsInfo : fishCardGroupsInfos) {
            if (fishCardGroupsInfo.getWorkOrderId().equals(workOrder.getId())) {
                workOrder.setGroupId(fishCardGroupsInfo.getGroupId());
                workOrder.setChatRoomId(fishCardGroupsInfo.getChatRoomId());
                if (null == fishCardGroupsInfo.getMemberAccount() || fishCardGroupsInfo.getMemberAccount().length != 2) {
                    workOrder.setNormal(false);
                } else {
                    if (fishCardGroupsInfo.getMemberAccount()[0].equals(EncoderByMd5(workOrder.getStudentId().toString()))){
                        if(fishCardGroupsInfo.getMemberAccount()[1].equals(EncoderByMd5(workOrder.getTeacherId().toString()))){
                            workOrder.setNormal(true);
                        }else {
                            workOrder.setNormal(false);
                        }
                    }else if(fishCardGroupsInfo.getMemberAccount()[1].equals(EncoderByMd5(workOrder.getStudentId().toString()))){
                        if(fishCardGroupsInfo.getMemberAccount()[0].equals(EncoderByMd5(workOrder.getTeacherId()  .toString()))){
                            workOrder.setNormal(true);
                        }else {
                            workOrder.setNormal(false);
                        }
                    }else {
                        workOrder.setNormal(false);
                    }
                }

                break;
            }
        }

    }


    /**
     * Excel导出
     *
     * @param fishCardFilterParam
     * @param pageable
     * @return
     */
    public List<WorkOrder> listFishCardsByUnlimitedUserCondForExcel(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        workOrderService.processDateParam(fishCardFilterParam);
        return fishCardQueryService.filterFishCards(fishCardFilterParam, pageable);
    }

    //带用户的访问.这个接口在规划管理的页面做出来后会使用
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        workOrderService.processFilterParam(fishCardFilterParam, false);
        List<Service> services = ListServicesByCond(fishCardFilterParam);
        if (CollectionUtils.isEmpty(services)) {
            logger.error("无对应的服务");
            return JsonResultModel.newJsonResultModel(null);
        }
        Page<WorkOrder> page = listFishCardsByDifferentCond(fishCardFilterParam, services, pageable);
        for (WorkOrder workOrder : page.getContent()) {
            workOrder.setStatusDesc(FishCardStatusEnum.getDesc((workOrder.getStatus())));
            if (ConstantUtil.SKU_EXTRA_VALUE == workOrder.getSkuIdExtra()) {
                workOrder.setTeachingType(TeachingType.WAIJIAO.getCode());
            } else {
                workOrder.setTeachingType(TeachingType.ZHONGJIAO.getCode());
            }
        }
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(page);
        return jsonResultModel;
    }

    private List<Service> ListServicesByCond(FishCardFilterParam fishCardFilterParam) {
        if (null == fishCardFilterParam.getStudentId()) {

        }
        if (null != fishCardFilterParam.getOrderCode()) {
            return serveService.findServicesByUserAndCond(fishCardFilterParam.getStudentId(), fishCardFilterParam.getOrderCode()
                    , SkuTypeEnum.SKU_COURSE_PLAN.value(), SkuTypeEnum.SKU_ANSWER_ONLINE.value());
        } else {
            return serveService.findAllServicesByUser(fishCardFilterParam.getStudentId(), SkuTypeEnum.SKU_COURSE_PLAN.value()
                    , SkuTypeEnum.SKU_ANSWER_ONLINE.value());
        }
    }

    private Page<WorkOrder> listFishCardsByDifferentCond(FishCardFilterParam fishCardFilterParam, List<Service> services, Pageable pageable) {
        List<Long> ids = new ArrayList<>();
        for (Service service : services) {
            ids.add(service.getId());
        }
        Long[] idsArr = new Long[ids.size()];
        if (null == fishCardFilterParam.getStatus()) {
            return workOrderService.findByQueryCondAllStatus(fishCardFilterParam, ids.toArray(idsArr), pageable);
        } else {
            return workOrderService.findByQueryCondSpecialStatus(fishCardFilterParam, ids.toArray(idsArr), pageable);
        }
    }

    public JsonResultModel listAllStatus() {
        Map<Integer, String> statusMap = Maps.newHashMap();
        for (FishCardStatusEnum fishCardStatusEnum : FishCardStatusEnum.values()) {
            statusMap.put(fishCardStatusEnum.getCode(), fishCardStatusEnum.getDesc());
        }
        return JsonResultModel.newJsonResultModel(statusMap);
    }

    //直接获取详细信息
    public JsonResultModel getNetPingDetail(Long cardId,String role,Pageable pageable){
        WorkOrder workOrder=workOrderService.findOne(cardId);
        DataAnalysisLogParam dataAnalysisLogParam=new DataAnalysisLogParam();
        dataAnalysisLogParam.setStartTime(workOrder.getStartTime().getTime());
        dataAnalysisLogParam.setEndTime(workOrder.getEndTime().getTime());
        dataAnalysisLogParam.setEvent(AppPointRecordEventEnum.ONLINE_COURSE_HEARTBEAT.value());
        if(role.equals("student")) {
            dataAnalysisLogParam.setUserId(workOrder.getStudentId());
        }
        else{
            dataAnalysisLogParam.setUserId(workOrder.getTeacherId());
        }
        return JsonResultModel.newJsonResultModel(
                dataAnalysisRequester.getNetSourceBean(dataAnalysisLogParam,pageable));
    }
}
