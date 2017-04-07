package com.boxfishedu.workorder.web.controller.member;

import com.boxfishedu.workorder.servicex.member.MemberServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 17/4/7.
 */
@CrossOrigin
@RestController
@RequestMapping("/memberInfo/student")
public class MemberController {

    @Autowired
    private MemberServiceX memberServiceX;

    @RequestMapping(value = "/{student_id}", method = RequestMethod.GET)
    public JsonResultModel memberRecord(@PathVariable("student_id") Long studentId){
        return JsonResultModel.newJsonResultModel(memberServiceX.memberRecord(studentId));
    }
}
