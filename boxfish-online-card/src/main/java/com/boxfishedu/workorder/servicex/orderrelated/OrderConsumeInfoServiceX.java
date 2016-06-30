package com.boxfishedu.workorder.servicex.orderrelated;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.service.ServeService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by hucl on 16/5/20.
 */
@Component
public class OrderConsumeInfoServiceX {
    @Autowired
    private ServeService serveService;

    /**
     * 获取订单的消费信息,总计多少,已经上了多少,剩余多少
     *
     * @return
     */
    public JsonResultModel getOrderConsumeInfo(Long orderId) {
        Map map = Maps.newHashMap();
        Service service = serveService.findByOrderId(orderId).get(0);
        int amount = service.getOriginalAmount();
        int foreignAmount = amount / 8;
        int chineseAmount = amount - foreignAmount;
        int unusedAmount = service.getAmount();
        map.put("foreignAmount",foreignAmount);
        map.put("chineseAmount",chineseAmount);
        map.put("unusedAmount",unusedAmount);
        //剩余的次数
        return JsonResultModel.newJsonResultModel(map);
    }
}
