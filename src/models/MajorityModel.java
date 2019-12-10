package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;

public class MajorityModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	private double vote = Math.random();
	private double accumulation = 0;
	private int accumulationCount = 0;
	
    protected MajorityModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    }
	@Override
	public void newInteraction(Interaction interaction, String neighborModelParameters) {
		accumulation += Double.parseDouble(neighborModelParameters);
		accumulationCount += 1;
	}
	@Override
	public void doPeriodicStuff(long atTime) {
		vote = (vote+accumulation)/(accumulationCount+1);
		accumulation = 0;
		accumulationCount = 0;
		//System.out.println(this.contextualEgoNetwork.getEgo()+"Current vote "+vote);
	}
	@Override
	public double evaluation() {
		return 0;
	}
	@Override
	public String getModelParameters(Interaction interaction) {
		return ""+vote;
	}
}
