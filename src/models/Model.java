package models;

import java.io.Serializable;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import simulation.messages.ModelMessageBody;

public abstract interface Model {

    public abstract void newInteraction(Interaction interaction, Object neighborModelParameters);
    public abstract void doPeriodicStuff(long timeTicks);
    public abstract String getMessageModelParameters();
    public abstract double evaluation();
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new MajorityModel(contextualEgoNetwork);
    }

}