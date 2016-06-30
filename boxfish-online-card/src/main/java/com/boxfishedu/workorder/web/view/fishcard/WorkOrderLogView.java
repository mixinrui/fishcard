package com.boxfishedu.workorder.web.view.fishcard;

import com.boxfishedu.workorder.web.view.base.BaseView;
import lombok.Data;
import org.jdto.annotation.DTOTransient;

/**
 * Created by hucl on 16/3/17.
 */
@Data
public class WorkOrderLogView extends BaseView {
    private String timeStamp;
    private Byte status;
    private String content;
    @DTOTransient
    private WorkOrderView workOrderView;
}
