package com.boxfishedu.workorder.service;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.card.bean.CourseTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/6/16.
 */
@Component
public class CourseType2TeachingTypeService {
    public int courseType2TeachingType(String courseType,TutorType tutorType){
       return getTeachingType(courseType,tutorType);
    }

    private static int getTeachingType(String courseType,TutorType tutorType){
            switch (tutorType){
                case CN:
                    return TeachingType.ZHONGJIAO.getCode();
                case FRN:
                    return TeachingType.WAIJIAO.getCode();
                case MIXED:{
                    if(courseType.equals(CourseTypeEnum.TALK.toString())){
                        return TeachingType.WAIJIAO.getCode();
                    }
                    else{
                        return TeachingType.ZHONGJIAO.getCode();
                    }
                }
                default:
                    return TeachingType.ZHONGJIAO.getCode();

            }
        }
    }

    /**
     * 提供一个静态调用
     * @param courseType
     * @return
     */
    public static int courseType2TeachingType2(String courseType,TutorType tutorType){
        return getTeachingType(courseType,tutorType);
    }
}
