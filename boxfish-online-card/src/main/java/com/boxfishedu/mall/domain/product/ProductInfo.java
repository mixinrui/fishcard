package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data(staticConstructor = "getInstance")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfo extends BaseEntity {

    private static final long serialVersionUID = 7162313324667413245L;

    private Long productType;

    private String productCode;

    private String productName;

    private String productRemark;

    private Integer defaultPrice;

    private Date startTime;

    private Date stopTime;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;

    @Transient
    private List<ProductOption> productOptions;
}