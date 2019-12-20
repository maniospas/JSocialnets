package models;

public interface Evaluator {
	public void aggregate(Object source, double outcome);

	public static Evaluator create() {
		return new evaluation.LastNodeEvaluation();
	}
	
}
