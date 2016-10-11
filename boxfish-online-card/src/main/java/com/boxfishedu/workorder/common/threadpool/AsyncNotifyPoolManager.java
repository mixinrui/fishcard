package com.boxfishedu.workorder.common.threadpool;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/10/10.
 */
@Component(ConstantUtil.THREADPOOL_ASYNC_NOTIFY)
public class AsyncNotifyPoolManager extends ThreadPoolManager {
        public int getActiveNum(){
            return threadPoolExecutor.getActiveCount();
        }
}
