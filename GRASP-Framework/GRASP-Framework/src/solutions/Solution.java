package solutions;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Solution<E> extends ArrayList<E> {
	
	public Double cost = Double.POSITIVE_INFINITY;
	public Double weight = 0.0;
	
	public Solution() {
		super();
	}

	public Solution(Double cost) {
		this.cost = cost;
		this.weight = 0.0;
	}
	
	public Solution(Solution<E> sol) {
		super(sol);
		cost = sol.cost;
	}

	@Override
	public String toString() {
		return "Solution: cost=[" + cost + "], size=[" + this.size() + "], elements=" + super.toString();
	}

}

