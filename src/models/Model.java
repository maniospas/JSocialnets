package models;

import messages.Message;

public abstract class Model {

	public abstract void updateFromNeighbour(Message message);

	public abstract void doPeriodicStuff();

}
