package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;

public class GNNModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	private double[] parameters;
	
    protected GNNModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    	parameters = new double[10];
    	for(int i=0;i<parameters.length;i++)
    		parameters[i] = Math.random();
    }

	@Override
	public void newInteraction(Interaction interaction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newInteraction(Interaction interaction, String neighborModelParameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPeriodicStuff(long atTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getModelParameters(Interaction interaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double evaluate(Interaction interaction) {
		// TODO Auto-generated method stub
		return 0;
	}

}
