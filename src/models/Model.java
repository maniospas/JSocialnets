package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;

public abstract interface Model {

    public abstract void newInteraction(Interaction interaction, Object neighborModelParameters);
    public abstract void doPeriodicStuff(long timeTicks);
    public abstract String getMessageModelParameters();
    public abstract double evaluation();
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new MajorityModel(contextualEgoNetwork);
    }

}