package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSku extends BaseEntity {

    private static final long serialVersionUID = 4775353166803465923L;

    private Long productId;

    private Integer skuPrice;

    private String optionOne;

    private String optionTwo;

    private String optionThree;

    private String optionFour;

    private String optionFive;

    public static ProductSku createProductSku(Long productId, Integer skuPrice, String... options) {
        return new ProductSku(productId, skuPrice, options);
    }

    public ProductSku(Long productId, Integer skuPrice, String... options) {
        switch (options.length) {
            case 5:
                this.optionOne = options[0];
                this.optionTwo = options[1];
                this.optionThree = options[2];
                this.optionFour = options[3];
                this.optionFive = options[4];
                break;
            case 4:
                this.optionOne = options[0];
                this.optionTwo = options[1];
                this.optionThree = options[2];
                this.optionFour = options[3];
                break;
            case 3:
                this.optionOne = options[0];
                this.optionTwo = options[1];
                this.optionThree = options[2];
                break;
            case 2:
                this.optionOne = options[0];
                this.optionTwo = options[1];
                break;
            case 1:
                this.optionOne = options[0];
        }
        this.productId = productId;
        this.skuPrice = skuPrice;

        this.setCreateTime(new Date());
    }
}