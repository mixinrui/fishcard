package com.boxfishedu.workorder.web.controller.home;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.home.HomePageServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
        smallClass.setClassType(ClassTypeEnum.SMALL.name());
        smallClass.setRoleId(2);
        smallClass.setChatRoomId(111111L);
        smallClass.setClassDate(new Date());
        smallClass.setCourseId("JJJKKKKKLLLSS");
        smallClass.setCourseName("魔术师的帽子里都藏着什么？");
        smallClass.setCourseType("TALK");
        smallClass.setCreateTime(new Date());
        smallClass.setGroupId("group id");
        smallClass.setGroupName("group name");
        smallClass.setStatus(200);
        smallClass.setSlotId(20);
        smallClass.setTeacherId(222L);
        smallClass.setUpdateTime(new Date());
        smallClass.setId(333L);
        smallClass.setDifficultyLevel("LEVEL_2");
        smallClass.setCreateTime(new Date());
        smallClass.setStatus(20);
        smallClass.setClassDate(new Date());
        smallClass.setActualEndTime(new Date());
        smallClass.setActualStartTime(new Date());
        smallClass.setCover("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");
        smallClass.setRoleId(2);
        smallClass.setUpdateTime(new Date());
        smallClass.setTeacherName("张老师");
        smallClass.setStartTime(new Date());
        smallClass.setEndTime(new Date());
        smallClass.setTeacherPhoto("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");


        SmallClass smallClass2 = smallClass.clone();
        smallClass2.setDifficultyLevel("LEVEL_3");

        SmallClass smallClass3 = smallClass.clone();
        smallClass3.setDifficultyLevel("LEVEL_4");

        SmallClass smallClass4 = smallClass.clone();
        smallClass4.setDifficultyLevel("LEVEL_5");


        List<AccountCourseBean.CardCourseInfo> cardCourseInfos = Lists.newArrayList();

        AccountCourseBean.CardCourseInfo cardCourseInfo = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0NvbnZlcnNhdGlvbjIvMDExLumtlOacr-W4iOeahOW4veWtkOmHjOmDveiXj-edgOS7gOS5iO-8ny54bHN4");
        cardCourseInfo.setCourseName("魔术师的帽子里都藏着什么？");
        cardCourseInfo.setCourseType("CONVERSATION");
        cardCourseInfo.setDateInfo(new Date());
        cardCourseInfo.setDifficulty("LEVEL_2");
        cardCourseInfo.setSmallClassId(333L);
        cardCourseInfo.setThumbnail("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");

        AccountCourseBean.CardCourseInfo cardCourseInfo2 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo2.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0NvbnZlcnNhdGlvbjIvMDExLumtlOacr-W4iOeahOW4veWtkOmHjOmDveiXj-edgOS7gOS5iO-8ny54bHN4");
        cardCourseInfo2.setCourseName("魔术师的帽子里都藏着什么？");
        cardCourseInfo2.setCourseType("CONVERSATION");
        cardCourseInfo2.setDateInfo(new Date());
        cardCourseInfo2.setDifficulty("LEVEL_3");
        cardCourseInfo2.setSmallClassId(333L);
        cardCourseInfo2.setThumbnail("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");

        AccountCourseBean.CardCourseInfo cardCourseInfo3 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo3.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0NvbnZlcnNhdGlvbjIvMDExLumtlOacr-W4iOeahOW4veWtkOmHjOmDveiXj-edgOS7gOS5iO-8ny54bHN4");
        cardCourseInfo3.setCourseName("魔术师的帽子里都藏着什么？");
        cardCourseInfo3.setCourseType("CONVERSATION");
        cardCourseInfo3.setDateInfo(new Date());
        cardCourseInfo3.setDifficulty("LEVEL_4");
        cardCourseInfo3.setSmallClassId(333L);
        cardCourseInfo3.setThumbnail("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");

        AccountCourseBean.CardCourseInfo cardCourseInfo4 = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo4.setCourseId("L3NoYXJlL3N2bi9MZXZlbCAzX0NvbnZlcnNhdGlvbjIvMDExLumtlOacr-W4iOeahOW4veWtkOmHjOmDveiXj-edgOS7gOS5iO-8ny54bHN4");
        cardCourseInfo4.setCourseName("魔术师的帽子里都藏着什么？");
        cardCourseInfo4.setCourseType("CONVERSATION");
        cardCourseInfo4.setDateInfo(new Date());
        cardCourseInfo4.setDifficulty("LEVEL_5");
        cardCourseInfo4.setSmallClassId(333L);
        cardCourseInfo4.setThumbnail("https://api.boxfish.cn/student/publication/data/data/650d5e6131224e1406b5ca3e66aa64a2");


        cardCourseInfo.setSmallClassInfo(smallClass);
        cardCourseInfo2.setSmallClassInfo(smallClass2);
        cardCourseInfo3.setSmallClassInfo(smallClass3);
        cardCourseInfo4.setSmallClassInfo(smallClass4);

        cardCourseInfos.add(cardCourseInfo);
        cardCourseInfos.add(cardCourseInfo2);
        cardCourseInfos.add(cardCourseInfo3);
        cardCourseInfos.add(cardCourseInfo4);
        return JsonResultModel.newJsonResultModel(cardCourseInfos);
    }
}
