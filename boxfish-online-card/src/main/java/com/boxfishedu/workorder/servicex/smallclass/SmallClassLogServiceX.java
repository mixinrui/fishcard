package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.dao.mongo.SmallClassLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfo;
import com.boxfishedu.workorder.web.param.SmallClassParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jiaozijun on 17/1/12.
 */

@Service
public class SmallClassLogServiceX {

    @Autowired
    private SmallClassLogMorphiaRepository smallClassLogMorphiaRepository;

    public JsonResultModel listSmallClassLogByUnlimitedUserCond(SmallClassParam smallClassParam, Pageable pageable) {

        Long count = smallClassLogMorphiaRepository.queryCount(smallClassParam);
        this.trimPage(smallClassParam,pageable);

        List<SmallClassLog> smallClassLogs = smallClassLogMorphiaRepository.queryCondition(smallClassParam).asList();
        Page<SmallClassLog> pagelist = new PageImpl(smallClassLogs, pageable, count);

        return JsonResultModel.newJsonResultModel(pagelist);
    }

    private void trimPage(SmallClassParam smallClassParam, Pageable pageable){
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();
        int limit = size;
        int offset = page * size;
        smallClassParam.setLimit(limit);
        smallClassParam.setOffSet(offset);
    }
}
