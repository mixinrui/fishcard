package com.boxfishedu.workorder.web.view.fishcard;

import com.boxfishedu.workorder.web.view.base.BaseView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import java.util.HashSet;
import java.util.Set;

@Data
public class ServiceView extends BaseView {
    private Long studentId;
    private String studentName;
    private Long orderId;
    private Integer originalAmount;
    private Integer amount;
    private String startTime;
    private String endTime;
    private Long skuId;
    private String skuName;
    @JsonIgnore
    @DTOTransient
    private Set<WorkOrderView> workOrders = new HashSet<>();
    private Integer validityDay;
    private String currDate;
    @Source(value = "id")
    private Long serviceId;
}
