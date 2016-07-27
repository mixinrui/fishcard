package com.boxfishedu.workorder.web.param.fishcardcenetr;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/7/26.
 */
@Data
public class FishCardDeleteParam {
    //批量删除学生
    private List<Long> studentIds;

    //批量删除指定的鱼卡
    private List<Long> fishCardIds;

    //删除指定日期之后 YY-mm-dd HH:mm:ss
    private String beginDate;
}
