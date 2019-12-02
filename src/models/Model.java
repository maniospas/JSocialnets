package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import contextualegonetwork.Node;

public abstract interface Model {
    public abstract void newInteraction(Interaction interaction, String neighborModelParameters);
    public abstract void doPeriodicStuff(long atTime);
    public abstract String getModelParameters(Node sendToNode);
    public abstract double evaluation();
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new MajorityModel(contextualEgoNetwork);
    }

}