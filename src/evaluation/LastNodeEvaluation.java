package evaluation;

import java.util.HashMap;

import evaluation.EvaluationFrame.EvaluationMeasure;

public class LastNodeEvaluation extends EvaluationMeasure {
	private HashMap<Object, Double> outcomes = new HashMap<Object, Double>();
	
	public LastNodeEvaluation() {
		super();
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
