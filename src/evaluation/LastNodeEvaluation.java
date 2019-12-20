package evaluation;

import java.util.HashMap;

public class LastNodeEvaluation extends EvaluationFrame {
	private HashMap<Object, Double> outcomes = new HashMap<Object, Double>();
	
	public LastNodeEvaluation() {
		super("Last node evaluation", "Loss");
	}

	public double register(Object source, double outcome) {
		outcomes.put(source, outcome);
		if(outcomes.isEmpty())
			return 0;
		double sum = 0;
		for(Double val : outcomes.values())
			sum += val;
		return sum / outcomes.size();
	}
}
