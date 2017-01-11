package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by hucl on 17/1/5.
 */
@Data
public abstract class SmallClassEventCustomer {
    protected final Logger logger= LoggerFactory.getLogger("SmallClassEventCustomer");

    protected SmallClassCardStatus smallClassCardStatus;

    public abstract void exec(SmallClassEvent smallClassEvent);
}
