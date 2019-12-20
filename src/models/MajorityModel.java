package models;

public class MajorityModel implements Model {
	private double vote = Math.random();
	
    protected MajorityModel() {
    }
	@Override
	public void newInteraction(EdgeInteraction interaction) {
	}
	@Override
	public void newInteraction(EdgeInteraction interaction, String neighborModelParameters) {
		vote = vote*0.9 + Double.parseDouble(neighborModelParameters)*0.1;
	}
	@Override
	public void doPeriodicStuff(long atTime) {
	}
	@Override
	public double evaluate(EdgeInteraction interaction) {
		return Math.abs(vote-0.5);
	}
	@Override
	public String getModelParameters(EdgeInteraction interaction) {
		//System.out.println(""+vote);
		return ""+vote;
	}
}
