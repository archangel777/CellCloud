package com.example.ericklima.cellcloud.strategies;
import com.example.ericklima.cellcloud.Tuple;

import java.util.ArrayList;

public interface ReduceStrategy {

	public Long reduce(ArrayList<Tuple<Long>> t);
	
}
