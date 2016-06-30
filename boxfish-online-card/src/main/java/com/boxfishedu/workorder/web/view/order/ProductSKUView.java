package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

@Data
public class ProductSKUView extends BaseTimeView {
    private ServiceSKU serviceSKU;
    private Integer skuAmount;
    private Integer skuCycle;
    private Object actualPrice;
    private Object originalPrice;
}
