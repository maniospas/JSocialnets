package models;

import contextualegonetwork.Interaction;
import managers.ContextualEgoNetworkManager;
import messages.ModelMessageBody;

public abstract class Model {
	
//	should we pass a reference to the manager instead?
    public Model(ContextualEgoNetworkManager cenManager) {
    	
    }

    public abstract void newInteraction(Interaction interaction, ModelMessageBody neighborModelParameters);

    public abstract void doPeriodicStuff(long timeTicks);

    public abstract ModelMessageBody getMessageModelParameters();

}