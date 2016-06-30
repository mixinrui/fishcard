package com.boxfishedu.workorder.web.view.base;

import lombok.Data;
import org.jdto.annotation.Source;
import org.jdto.mergers.DateFormatMerger;

/**
 * Created by hucl on 16/3/18.
 */
@Data
public class BaseView {
    private Long id;
    @Source(value = "createTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
    @Source(value = "updateTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
