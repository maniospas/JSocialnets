package evaluation;

import java.util.ArrayList;

import evaluation.EvaluationFrame.EvaluationMeasure;

public class FIFOEvaluator extends EvaluationMeasure {
	private ArrayList<Double> accumulation = new ArrayList<Double>();
	
	public FIFOEvaluator() {
	}

	public double register(Object source, double outcome) {
		accumulation.add(outcome);
		if(accumulation.size()>1000)
			accumulation.remove(0);
		double sum = 0;
		for(double val : accumulation)
			sum += val;
		return sum/accumulation.size();
	}
}
