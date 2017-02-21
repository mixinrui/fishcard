package com.boxfishedu.workorder.web.controller.home;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
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

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/student/{student_id}", method = RequestMethod.GET)
    public JsonResultModel userCardInfo(String order_type, @PathVariable("student_id") Long studentId) {
        return homePageServiceX.getHomePage(order_type, studentId);
    }

    @RequestMapping(value = "/student/{student_id}/public", method = RequestMethod.GET)
    public JsonResultModel publicClassInfo() {
        return homePageServiceX.getPublicHomePage();
    }

    @RequestMapping(value = "/student/classInfo", method = RequestMethod.GET)
    public JsonResultModel studentClassInfo(@RequestParam("studentId") Long studentId, @RequestParam("date") String date) {
        return homePageServiceX.studentClassInfo(studentId, DateUtil.simpleString2Date(date));
    }

    @RequestMapping(value = "/student/classInfo/{student_id}", method = RequestMethod.GET)
    public JsonResultModel studentClassInfo(@PathVariable("student_id") Long studentId) {
        return homePageServiceX.studentClassInfo(studentId, DateUtil.date2SimpleDate(new Date()));
    }

}
