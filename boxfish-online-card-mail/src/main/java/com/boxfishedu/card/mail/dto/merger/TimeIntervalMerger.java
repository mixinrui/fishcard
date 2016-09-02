package com.boxfishedu.card.mail.dto.merger;

import org.jdto.SinglePropertyValueMerger;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/1.
 */
public class TimeIntervalMerger implements SinglePropertyValueMerger<Integer, Date> {


    @Override
    public Integer mergeObjects(Date value, String[] extraParam) {
        Duration duration = Duration.between(value.toInstant(), Instant.now());
        return (int) duration.toHours();
    }

    @Override
    public boolean isRestoreSupported(String[] params) {
        return false;
    }

    @Override
    public Date restoreObject(Integer object, String[] params) {
        return null;
    }
}
