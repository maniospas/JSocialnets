package models;

import java.util.HashMap;
import java.util.Map;

import contextualegonetwork.Interaction;

public class MajorityModel implements Model {
	private double vote = Math.random();
	
    protected MajorityModel() {
    }
	@Override
	public void newInteraction(Interaction interaction) {
	}
	@Override
	public void newInteraction(Interaction interaction, String neighborModelParameters, boolean isReply) {
		vote = vote*0.9 + Double.parseDouble(neighborModelParameters)*0.1;
	}
	@Override
	public void doPeriodicStuff(long atTime) {
	}
	@Override
	public Map<String, Double> evaluate(Interaction interaction) {
		HashMap<String, Double> result = new HashMap<String, Double>();
		result.put("Consesous", Math.abs(vote-0.5));
		return result;
	}
	@Override
	public String getModelParameters(Interaction interaction) {
		//System.out.println(""+vote);
		return ""+vote;
	}
}
