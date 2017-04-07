package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassStudentStatusServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hucl on 17/1/7.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/teacher/smallclass")
public class SmallClassTeacherController {

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    SmallClassStudentStatusServiceX smallClassStudentStatusServiceX;

    @Autowired
    SmallClassServiceX smallClassServiceX;

    @RequestMapping(value = "/{smallclass_id}/validate", method = RequestMethod.GET)
    public JsonResultModel validate(@PathVariable("smallclass_id") Long smallClassId) {

        Map<String, Object> map = smallClassServiceX.getTeacherValidateMap(smallClassId);

        return JsonResultModel.newJsonResultModel(map);
    }

    @RequestMapping(value = "/{smallclass_id}/withoutValidate", method = RequestMethod.GET)
    public JsonResultModel withoutValidate(@PathVariable("smallclass_id") Long smallClassId) {

        Map<String, Object> map = smallClassServiceX.getTeacherWithoutValidateMap(smallClassId);

        return JsonResultModel.newJsonResultModel(map);
    }

    @RequestMapping(value = "/{smallclass_id}/detail", method = RequestMethod.GET)
    public JsonResultModel classDetail(@PathVariable("smallclass_id") Long smallClassId) {
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        return JsonResultModel.newJsonResultModel(smallClass);
    }

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(
            @PathVariable("smallclass_id") Long smallClassId
            , @RequestBody Map<String, String> statusReport, Long userId) {
        smallClassStudentStatusServiceX.status(smallClassId, userId, statusReport);
        return JsonResultModel.newJsonResultModel("success");
    }

    /**
     * 教师骰子获取随机未被点名的学生
     *
     * @param smallClassId
     * @return
     */
    @RequestMapping(value = "/{smallclass_id}/selectCandidate", method = RequestMethod.GET)
    public JsonResultModel selectRandomStudentList(@PathVariable("smallclass_id") Long smallClassId) {

        String studentId = smallClassServiceX.selectCandidate(smallClassId);

        return JsonResultModel.newJsonResultModel(studentId);
    }

    @RequestMapping(value = "/candidateInterval", method = RequestMethod.GET)
    public JsonResultModel candidateInterval() {
        return JsonResultModel.newJsonResultModel(smallClassServiceX.candidateInterval());
    }
}
