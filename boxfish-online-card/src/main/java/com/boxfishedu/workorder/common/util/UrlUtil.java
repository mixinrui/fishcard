package com.boxfishedu.workorder.common.util;

/**
 * Created by hucl on 16/3/16.
 */
public class UrlUtil {
    public static StringBuilder MergeUrl(String host, String suffix) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(host);
        buffer.append(suffix);
        return buffer;
    }
}
