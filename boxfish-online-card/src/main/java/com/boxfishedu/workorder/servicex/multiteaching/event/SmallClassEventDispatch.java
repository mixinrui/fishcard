package com.boxfishedu.workorder.servicex.multiteaching.event;

import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Component
public class SmallClassEventDispatch implements Observer {
    @Autowired
    private List<SmallClassEventCustomer> smallClassEventCustomers;

    public void update(Observable source, Object arg) {
        //事件的源头
        SmallClass smallClass = (SmallClass) arg;

        //事件
        SmallClassEvent event = (SmallClassEvent) source;

        for (SmallClassEventCustomer smallClassEventCustomer : smallClassEventCustomers) {
            if (smallClassEventCustomer.getSmallClassCardStatus().getCode() == event.getType().getCode()) {
                smallClassEventCustomer.exec(event);
                return;
            }
        }
    }
}

