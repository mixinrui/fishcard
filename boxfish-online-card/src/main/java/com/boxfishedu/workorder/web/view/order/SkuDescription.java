package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

/**
 * Created by hucl on 16/4/27.
 */
@Data
public class SkuDescription extends BaseTimeView {
    private ServiceSKU serviceSKU;
    private Integer skuAmount;
    private Integer skuCycle;
    private Double actualPrice;
    private Double originalPrice;
}
