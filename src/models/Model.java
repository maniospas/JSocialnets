package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import simulation.messages.ModelMessageBody;

public abstract interface Model {

    public abstract void newInteraction(Interaction interaction, ModelMessageBody neighborModelParameters);
    public abstract void doPeriodicStuff(long timeTicks);
    public abstract Object getMessageModelParameters();
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new MajorityModel(contextualEgoNetwork);
    }

}