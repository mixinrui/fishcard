package com.boxfishedu.workorder.servicex.smallclass.event;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import lombok.Data;

import java.util.Observable;

/**
 * Created by hucl on 17/1/4.
 */
@Data
public class SmallClassEvent extends Observable {
    private SmallClassEventDispatch smallClassEventDispatch;
    //事件起源
    private SmallClass source;
    //事件的类型
    private PublicClassInfoStatusEnum type;

    //传入事件的源头,默认为新建类型
    public SmallClassEvent(SmallClass smallClass, SmallClassEventDispatch smallClassEventDispatch) {
        this(smallClass, smallClassEventDispatch, PublicClassInfoStatusEnum.CREATE);
    }

    //事件源头以及事件类型
    public SmallClassEvent(SmallClass p, SmallClassEventDispatch smallClassEventDispatch, PublicClassInfoStatusEnum type) {
        this.smallClassEventDispatch = smallClassEventDispatch;
        this.source = p;
        this.type = type;
        //事件触发
        this.notifyEventDispatch();
    }

    //通知事件处理中心
    private void notifyEventDispatch() {
        super.addObserver(smallClassEventDispatch);
        super.setChanged();
        super.notifyObservers(source);
    }
}
