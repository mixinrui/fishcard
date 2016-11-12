package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

/**
 * Created by LuoLiBing on 16/9/24.
 */
public class ServiceChoice {

    int index;
    int amount;

    public ServiceChoice(int index, int amount) {
        this.index = index;
        this.amount = amount;
    }

    public boolean hasNext() {
        return amount > 0;
    }

    public void decrement() {
        this.amount --;
    }
}
