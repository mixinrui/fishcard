package com.boxfishedu.workorder.common.util;

/**
 * Created by hucl on 16/5/19.
 */
public class SystemUtil {
    public static int getCpuNum(){
        return Runtime.getRuntime().availableProcessors();
    }
}
