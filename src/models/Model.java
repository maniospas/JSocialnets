package models;

import java.util.Map;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;

public abstract interface Model {
    public abstract void newInteraction(Interaction interaction);
    public abstract void newInteraction(Interaction interaction, String neighborModelParameters, boolean isReply);
    public abstract void doPeriodicStuff(long atTime);
    public abstract String getModelParameters(Interaction interaction);
    public abstract Map<String, Double> evaluate(Interaction interaction);
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new GNNModel(contextualEgoNetwork);
    }

}