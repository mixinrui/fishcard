package com.boxfishedu.workorder.common.threadpool;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/5/19.
 */
@Component(ConstantUtil.THREADPOOL_ORDER2SERVICE)
public class OrderConvertPoolManager extends ThreadPoolManager {
    public int getActiveNum(){
        return threadPoolExecutor.getActiveCount();
    }
}
