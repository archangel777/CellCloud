package strategies;

import java.util.ArrayList;

import utils.DataPool;
import utils.Tuple;

public class MinStrategy implements ReduceStrategy {

	@Override
	public Long reduce(ArrayList<Tuple<Long>> t) {
		if (t.size() == 1) return Math.min(t.get(0).getT1(), t.get(0).getT2());
        Tuple<Long> t1 = t.remove(0), t2 = t.remove(0);
        t.add(new Tuple<>(Math.min(t1.getT1(), t1.getT2()), Math.min(t2.getT1(), t2.getT2())));
        return reduce(t);
	}

	@Override
	public Long baseline(DataPool pool) {
		DataPool test = pool.copy();
		Long min = test.poll();
		while(test.size() != 0) {
			Long l = test.poll();
			if (l < min) min = l;
		}
		return min;
	}
	
	
	@Override
	public String strategyName() {
		return "min";
	}

}
