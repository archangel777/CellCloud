package com.example.ericklima.cellcloud;

/**
 * Created by ErickLima on 23/06/2017.
 */

public class Tuple<T> {
    private T t1, t2;

    public Tuple(T t1, T t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T getT1() {
        return t1;
    }

    public T getT2() {
        return t2;
    }

    public void setT1(T t1) {
        this.t1 = t1;
    }

    public void setT2(T t2) {
        this.t2 = t2;
    }
}
