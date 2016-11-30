package com.boxfishedu.workorder.web.param;

import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;

/**
 * Created by hucl on 16/5/9.
 */
@Data
public class BaseTimeSlotParam {
    String beginDate;
    String endDate;
    Date beginDateFormat;
    Date endDateFormat;
    Integer teachingType;

}
