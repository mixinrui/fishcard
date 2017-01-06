package com.boxfishedu.workorder.web.view.base;

import com.google.common.collect.Lists;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by LuoLiBing on 16/5/3.
 */
@Data
public class DateRange {

    private LocalDateTime from;
    private LocalDateTime to;
    // 默认为一周时间
    private Integer range = 7;

    public DateRange() {}

    public DateRange(LocalDateTime from) {
        this.from = from;
        this.to = from.plusDays(range);
    }

    public DateRange(LocalDateTime from, Integer range) {
        if(range != null) {
            this.range = range;
        }
        this.from = from;
        this.to = from.plusDays(this.range);
    }

    public void incrementAWeek() {
        from = from.plusWeeks(1);
        to = to.plusWeeks(1);
    }

    public <K> List<K> forEach(K k,Handle<? super K> handle) throws CloneNotSupportedException {
        ArrayList<K> results = Lists.newArrayList();
        for(int i=0; i < range; i++) {
            results.add((K) handle.transfer(from.plusDays(i),k));
        }
        return results;
    }

    @FunctionalInterface
    public interface Handle<K> {
        K transfer(LocalDateTime dateTime, K k) throws CloneNotSupportedException;
    }

    public <K> List<K> forEach(Handle<? super K> handle, Function<LocalDateTime,K> producer) throws CloneNotSupportedException {
        ArrayList<K> results = Lists.newArrayList();
        for(int i=0; i < range; i++) {
            results.add((K) handle.transfer(from.plusDays(i), producer.apply(from.plusDays(i))));
        }
        return results;
    }
}
