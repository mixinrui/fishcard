package com.boxfishedu.workorder.web.controller.home;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassType;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.home.HomePageServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hucl on 16/3/31.
 * 主要处理学生App相关的操作
 */
@CrossOrigin
@RestController
@RequestMapping("/home")
public class
HomePageController {

    @Autowired
    private HomePageServiceX homePageServiceX;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/student/{student_id}", method = RequestMethod.GET)
    public JsonResultModel userCardInfo(String order_type, @PathVariable("student_id") Long studentId) {
        return homePageServiceX.getHomePage(order_type, studentId);
    }

    @RequestMapping(value = "/student/{student_id}/public", method = RequestMethod.GET)
    public JsonResultModel publicClassInfo() {
        SmallClass smallClass = new SmallClass();
        smallClass.setGroupLeaderCard(1L);
        smallClass.setGroupLeader(11L);
        smallClass.setSmallClassType(SmallClassType.SMALL.name());
        smallClass.setRoleId(2);
        smallClass.setChatRoomId(111111L);
        smallClass.setClassDate(new Date());
        smallClass.setCourseId("JJJKKKKKLLLSS");
        smallClass.setCourseName("test course");
        smallClass.setCourseType("TALK");
        smallClass.setCreateTime(new Date());
        smallClass.setGroupId("group id");
        smallClass.setGroupName("group name");
        smallClass.setStatus(200);
        smallClass.setSlotId(20);
        smallClass.setTeacherId(222L);
        smallClass.setUpdateTime(new Date());
        smallClass.setId(333L);


        List<AccountCourseBean.CardCourseInfo> cardCourseInfos = Lists.newArrayList();

        AccountCourseBean.CardCourseInfo cardCourseInfo = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0NvbnZlcnNhdGlvbjIvMDExLumtlOacr-W4iOeahOW4veWtkOmHjOmDveiXj-edgOS7gOS5iO-8ny54bHN4");
        cardCourseInfo.setCourseName("魔术师的帽子里都藏着什么？");
        cardCourseInfo.setCourseType("CONVERSATION");
        cardCourseInfo.setDateInfo(new Date());
        cardCourseInfo.setDifficulty("LEVEL_2");
        cardCourseInfo.setSmallClassId(15l);
        cardCourseInfo.setThumbnail("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");
        cardCourseInfo.setSmallClassInfo(smallClass);

        AccountCourseBean.CardCourseInfo cardCourseInfo2 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo2.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0Z1bmN0aW9uMS8wMDMu5aaC5L2V5omT5ZCs5p-Q5Lq65piv6LCB77yfLnhsc3g");
        cardCourseInfo2.setCourseName("如何询问你爸爸是做什么的");
        cardCourseInfo2.setCourseType("CONVERSATION");
        cardCourseInfo2.setDateInfo(new Date());
        cardCourseInfo2.setDifficulty("LEVEL_3");
        cardCourseInfo2.setSmallClassId(16l);
        cardCourseInfo2.setThumbnail("https://api.boxfish.cn/student/publication/data/data/54b968303779a9d52ef68b4a201e97c1");
        cardCourseInfo2.setSmallClassInfo(smallClass);

        AccountCourseBean.CardCourseInfo cardCourseInfo3 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo3.setCourseId("L3NoYXJlL3N2bi9MZXZlbCA4X1JlYWRpbmc0LzAwNy7njq_kv53kuI7nvo7po5_lj6_ku6XlhbzlvpflkJfvvJ8ueGxzeA");
        cardCourseInfo3.setCourseName("环保与美食可以兼得吗？");
        cardCourseInfo3.setCourseType("CONVERSATION");
        cardCourseInfo3.setDateInfo(new Date());
        cardCourseInfo3.setDifficulty("LEVEL_4");
        cardCourseInfo3.setSmallClassId(16l);
        cardCourseInfo3.setThumbnail("http://api.boxfish.cn/student/publication/data/data/4949758367abb93e9f321d0ef50cede1");
        cardCourseInfo3.setSmallClassInfo(smallClass);


        AccountCourseBean.CardCourseInfo cardCourseInfo4 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo4.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0Z1bmN0aW9uMS8wMzAu5oCO5qC36K-i6Zeu5aSp5rCU5oOF5Ya177yfLnhsc3g");
        cardCourseInfo4.setCourseName("怎样询问天气情况？");
        cardCourseInfo4.setCourseType("FUNCTION");
        cardCourseInfo4.setDateInfo(new Date());
        cardCourseInfo4.setDifficulty("LEVEL_5");
        cardCourseInfo4.setSmallClassId(16l);
        cardCourseInfo4.setThumbnail("http://api.boxfish.cn/student/publication/data/data/1bcdfc18e71a4e94301d96099d5fb502");
        cardCourseInfo4.setSmallClassInfo(smallClass);

        cardCourseInfos.add(cardCourseInfo);
        cardCourseInfos.add(cardCourseInfo2);
        cardCourseInfos.add(cardCourseInfo3);
        cardCourseInfos.add(cardCourseInfo4);
        return JsonResultModel.newJsonResultModel(cardCourseInfos);
    }
}
