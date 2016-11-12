package com.boxfishedu.mall.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by LuoLiBing on 16/10/18.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComboDurations {

    private Integer durationWeeks;

    private Integer perWeekCourseCount;

    private String comboCode;
}
