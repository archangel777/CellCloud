package strategies;

import java.util.ArrayList;

import utils.DataPool;
import utils.Tuple;

public class SumStrategy implements ReduceStrategy {

	@Override
	public Long reduce(ArrayList<Tuple<Long>> t) {
		if (t.size() == 1) return t.get(0).getT1() + t.get(0).getT2();
        Tuple<Long> t1 = t.remove(0), t2 = t.remove(0);
        t.add(new Tuple<>(t1.getT1() + t1.getT2(), t2.getT1() + t2.getT2()));
        return reduce(t);
	}

	@Override
	public Long baseline(DataPool pool) {
		DataPool test = pool.copy();
		Long sum = 0l;
		while(test.size() != 0) {
			sum += test.poll();
		}
		return sum;
	}

	@Override
	public String strategyName() {
		return "sum";
	}

}
