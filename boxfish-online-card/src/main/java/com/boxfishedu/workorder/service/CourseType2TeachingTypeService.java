package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.card.bean.CourseTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/6/16.
 */
@Component
public class CourseType2TeachingTypeService {
    public int courseType2TeachingType(String courseType){
        if(courseType.equals(CourseTypeEnum.TALK.toString())){
            return TeachingType.WAIJIAO.getCode();
        }
        return TeachingType.ZHONGJIAO.getCode();
    }
}
