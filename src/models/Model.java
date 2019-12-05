package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;

public abstract interface Model {
    public abstract void newInteraction(Interaction interaction, String neighborModelParameters);
    public abstract void doPeriodicStuff(long atTime);
    public abstract String getModelParameters(Interaction interaction);
    public abstract double evaluation();
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new MajorityModel(contextualEgoNetwork);
    }

}