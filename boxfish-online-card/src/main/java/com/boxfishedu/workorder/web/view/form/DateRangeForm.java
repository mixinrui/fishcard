package com.boxfishedu.workorder.web.view.form;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang.time.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/6/2.
 */
@Data
public class DateRangeForm {
    private final Date from;
    private final Date to;

    public DateRangeForm(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public long getBeginLongValue() {
        return from.getTime();
    }

    public long getEndLongValue() {
        return to.getTime();
    }

    public boolean isSameDay() {
        return DateUtils.isSameDay(from, to);
    }

    public boolean isWithIn(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.from(from.toInstant()))
                && dateTime.isBefore(LocalDateTime.from(to.toInstant()));
    }

    public <K> List<K> collect(Handle<? extends K> handle) throws CloneNotSupportedException {
        ArrayList<K> results = Lists.newArrayList();
        Date loop = new Date(from.getTime());
        do {
            results.add(handle.transfer(loop));
            loop = DateUtil.getTomorrowByDate(loop);
        } while(loop.getTime() <= to.getTime());
        return results;
    }

    @FunctionalInterface
    public interface Handle<K> {
        K transfer(Date date) throws CloneNotSupportedException;
    }
}