package com.boxfishedu.workorder.servicex.smallclass.status.event;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by hucl on 17/1/5.
 */
@Data
public abstract class SmallClassEventCustomer {
    protected final Logger logger = LoggerFactory.getLogger("SmallClassEventCustomer");

    public final String prefix = "INIT_";

    protected PublicClassInfoStatusEnum smallClassCardStatus;


    public void exec(SmallClassEvent smallClassEvent) {
        SmallClass smallClass = smallClassEvent.getSource();
        this.execute(smallClass);
    }

    public abstract void execute(SmallClass smallClass);
}
