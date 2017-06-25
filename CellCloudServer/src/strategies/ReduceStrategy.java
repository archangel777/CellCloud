package strategies;
import java.util.ArrayList;

import utils.DataPool;
import utils.Tuple;

public interface ReduceStrategy {

	public Long reduce(ArrayList<Tuple<Long>> t);
	
	public Long baseline(DataPool pool);
	
	public String strategyName();
	
}
