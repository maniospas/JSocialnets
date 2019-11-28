package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import simulation.messages.ModelMessageBody;

public class MajorityModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	private double vote = Math.random();
	private double accumulation = 0;
	private int accumulationCount = 0;
	
    protected MajorityModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    }
	@Override
	public void newInteraction(Interaction interaction, Object neighborModelParameters) {
		accumulation += (Double)neighborModelParameters;
		accumulationCount += 1;
		System.out.println("Test");
	}
	@Override
	public void doPeriodicStuff(long timeTicks) {
		vote = (vote+accumulation)/(accumulationCount+1);
		accumulation = 0;
		accumulationCount = 1;
	}
	@Override
	public String getMessageModelParameters() {
		return ""+vote;
	}
	@Override
	public double evaluation() {
		return 0;
	}
}
