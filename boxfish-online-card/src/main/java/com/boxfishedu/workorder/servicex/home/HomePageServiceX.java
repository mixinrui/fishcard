package com.boxfishedu.workorder.servicex.home;

import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by hucl on 16/11/30.
 */
@Component
public class HomePageServiceX {
    @Autowired
    private AccountCardInfoService accountCardInfoService;
    @Autowired
    private RepeatedSubmissionChecker checker;
    @Autowired
    private OnlineAccountService onlineAccountService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public JsonResultModel getHomePage(String order_type, @PathVariable("student_id") Long studentId) {
        if(!onlineAccountService.isMember(studentId)){
            logger.debug("@userCardInfo#{}不是在线购买用户,直接返回",studentId);
            return JsonResultModel.newJsonResultModel(AccountCardInfo.buildEmpty());
        }
        AccountCardInfo accountCardInfo=accountCardInfoService.queryByStudentId(studentId);
        if(null!=order_type) {
            switch (order_type) {
                case "chinese":
                    accountCardInfo.setComment(null);
                    accountCardInfo.setForeign(null);
                    break;
                case "foreign":
                    accountCardInfo.setComment(null);
                    accountCardInfo.setChinese(null);
                    break;
                case "comment":
                    accountCardInfo.setForeign(null);
                    accountCardInfo.setChinese(null);
                    break;
                default:
                    break;
            }
        }
        return JsonResultModel.newJsonResultModel(accountCardInfo);
    }
}
