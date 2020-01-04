package models;

import java.util.Map;


public interface Evaluator {
	public void aggregate(Object source, Map<String, Double> outcome);

	public static Evaluator create() {
		return new evaluation.EvaluationFrame(evaluation.LastNodeEvaluation.class);
	}
	
}
