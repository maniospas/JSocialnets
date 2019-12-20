package evaluation;

public class AccumulativeEvaluator extends EvaluationFrame {
	private double accumulation = 0;
	private double smoothing = 0.99;
	
	public AccumulativeEvaluator() {
		super("Acc", "Loss");
	}

	public double register(Object source, double outcome) {
		accumulation = smoothing*accumulation + outcome*(1-smoothing);
		return accumulation;
	}
}
