package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import simulation.messages.ModelMessageBody;

public class MajorityModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	
    protected MajorityModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    }
	@Override
	public void newInteraction(Interaction interaction, ModelMessageBody neighborModelParameters) {
		
	}
	@Override
	public void doPeriodicStuff(long timeTicks) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object getMessageModelParameters() {
		return null;
	}
}
