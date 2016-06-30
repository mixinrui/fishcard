package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

/**
 * Created by hucl on 16/4/27.
 */
@Data
public class ServiceSKU extends BaseTimeView {
    private String skuCode;
    private String skuName;
    private Integer countUnit;
    private Object originalPrice;
    private String description;
    private String serviceType;
    private String flagEnable;
    private String flagVisible;
    private String deadline;
    private Integer validDay;
}
