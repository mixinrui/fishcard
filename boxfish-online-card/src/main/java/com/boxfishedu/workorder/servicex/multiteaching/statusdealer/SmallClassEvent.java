package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;

import java.util.Observable;

/**
 * Created by hucl on 17/1/4.
 */
public class SmallClassEvent extends Observable {
    //事件起源
    private SmallClass source;
    //事件的类型
    private SmallClassCardStatus type;

    //传入事件的源头,默认为新建类型
    public SmallClassEvent(SmallClass smallClass) {
        this(smallClass,SmallClassCardStatus.CREATE);
    }

    //事件源头以及事件类型
    public SmallClassEvent(SmallClass p,SmallClassCardStatus type){
        this.source = p;
        this.type = type;
        //事件触发
        this.notifyEventDispatch();
    }


    //通知事件处理中心
    private void notifyEventDispatch(){
//        super.addObserver(EventDispatch.getEventDispathc());
        super.setChanged();
        super.notifyObservers(source);
    }
}
