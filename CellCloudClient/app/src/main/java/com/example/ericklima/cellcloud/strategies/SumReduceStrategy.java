package com.example.ericklima.cellcloud.strategies;

import com.example.ericklima.cellcloud.Tuple;

import java.util.ArrayList;

public class SumReduceStrategy implements ReduceStrategy {

	@Override
	public Long reduce(ArrayList<Tuple<Long>> t) {
		if (t.size() == 1) return t.get(0).getT1() + t.get(0).getT2();
        Tuple<Long> t1 = t.remove(0), t2 = t.remove(0);
        t.add(new Tuple<>(t1.getT1() + t1.getT2(), t2.getT1() + t2.getT2()));
        return reduce(t);
	}

}
