package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

@Data
public class ProductHasSKUView extends BaseTimeView {
    private ProductSKUView productSKU;
    private Integer amount;

}
