package evaluation;

import evaluation.EvaluationFrame.EvaluationMeasure;

public class AccumulativeEvaluator extends EvaluationMeasure {
	private double accumulation = 0;
	private double smoothing = 0.99;
	
	public AccumulativeEvaluator() {
		super();
	}

	public double register(Object source, double outcome) {
		accumulation = smoothing*accumulation + outcome*(1-smoothing);
		return accumulation;
	}
}
