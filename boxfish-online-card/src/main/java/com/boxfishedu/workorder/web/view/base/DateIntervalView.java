package com.boxfishedu.workorder.web.view.base;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.util.DateUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Created by hucl on 16/4/14.
 */
@Data
public class DateIntervalView {
    private String begin;
    private String end;

    public DateIntervalView() {}

    public DateIntervalView(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    public Date beginDate() {
        if(StringUtils.isBlank(begin)) {
            throw new ValidationException("begin开始日期为空");
        }
        return DateUtil.String2SimpleDate(begin);
    }

    public Date endDate() {
        if(StringUtils.isBlank(end)) {
            throw new ValidationException("end结束日期为空");
        }
        return DateUtil.String2SimpleDate(end);
    }

    public long getBeginLongValue() {
        return DateUtil.String2SimpleDate(begin).getTime();
    }

    public long getEndLongValue() {
        return DateUtil.String2SimpleDate(end).getTime();
    }
}
