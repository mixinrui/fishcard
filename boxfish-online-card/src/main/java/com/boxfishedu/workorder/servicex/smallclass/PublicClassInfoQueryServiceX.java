package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfo;
import com.boxfishedu.workorder.service.instantclass.PublicClassInfoQueryService;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by jiaozijun on 17/1/11.
 */

@Service
public class PublicClassInfoQueryServiceX {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PublicClassInfoQueryService publicClassInfoQueryService;


    public JsonResultModel listFishCardsByUnlimitedUserCond(PublicFilterParam publicFilterParam, Pageable pageable) {
        processDateParam(publicFilterParam);
        List<PublicClassInfo> workOrderList = publicClassInfoQueryService.filterFishCards(publicFilterParam, pageable);

        Long count = publicClassInfoQueryService.filterFishCardsCount(publicFilterParam);
        Page<PublicClassInfo> page = new PageImpl(workOrderList, pageable, count);

        return JsonResultModel.newJsonResultModel(page);
    }



    public void processDateParam(PublicFilterParam publicFilterParam) {
        if (null == publicFilterParam.getBeginDate()) {
            publicFilterParam.setBeginDateFormat(DateUtil.String2Date(ConstantUtil.EARLIEST_TIME));
        } else {
            publicFilterParam.setBeginDateFormat(DateUtil.String2Date(publicFilterParam.getBeginDate()));
        }
        if (null == publicFilterParam.getEndDate()) {
            publicFilterParam.setEndDateFormat(DateUtil.String2Date(ConstantUtil.LATEST_TIME));
        } else {
            publicFilterParam.setEndDateFormat(DateUtil.String2Date(publicFilterParam.getEndDate()));
        }

        if(null != publicFilterParam.getCreateBeginDate()){
            publicFilterParam.setCreateBeginDateFormat(  DateUtil.String2Date(publicFilterParam.getCreateBeginDate()));
        }

        if(null != publicFilterParam.getCreateEndDate()){
            publicFilterParam.setCreateEndDateFormat(DateUtil.String2Date(publicFilterParam.getCreateEndDate()));
        }
    }




    public JsonResultModel listAllStatus() {
        Map<Integer, String> statusMap = Maps.newHashMap();
        for (PublicClassInfoStatusEnum smallClassCardStatus : PublicClassInfoStatusEnum.values()) {
            statusMap.put(smallClassCardStatus.getCode(), smallClassCardStatus.getDesc());
        }
        return JsonResultModel.newJsonResultModel(statusMap);
    }
}
