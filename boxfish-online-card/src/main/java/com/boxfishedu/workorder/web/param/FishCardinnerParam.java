package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/3.
 */
@Data
public class FishCardinnerParam {

    private  Long studentId;
    private  Long[] hasEvFishcardIds;  // 鱼卡数组
    private Integer skuId;
}
