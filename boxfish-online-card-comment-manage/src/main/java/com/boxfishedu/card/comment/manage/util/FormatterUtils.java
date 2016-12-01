package com.boxfishedu.card.comment.manage.util;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Created by ansel on 16/11/30.
 */
public class FormatterUtils {

    // 千分位
    public final static ThreadLocal<DecimalFormat> DECIMAL_FORMAT = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("#,###.00");
        }
    };

    public final static DateTimeFormatter DATE_TIME_FORMATTER_0 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
}
