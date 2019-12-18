package models;

public interface Evaluator {
	public void aggregate(double outcome);

	public static Evaluator create() {
		return null;
	}

}
