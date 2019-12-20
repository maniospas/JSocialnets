package models;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Edge;
import contextualegonetwork.Interaction;

public abstract interface Model {
	public static class EdgeInteraction {
		private Edge edge;
		private Interaction interaction;
		public EdgeInteraction(Edge edge, Interaction interaction) {
			this.edge = edge;
			this.interaction = interaction;
		}
		public Edge getEdge() {
			return edge;
		}
		public Interaction getInteraction() {
			return interaction;
		}
	}
	
    public abstract void newInteraction(EdgeInteraction interaction);
    public abstract void newInteraction(EdgeInteraction interaction, String neighborModelParameters);
    public abstract void doPeriodicStuff(long atTime);
    public abstract String getModelParameters(EdgeInteraction interaction);
    public abstract double evaluate(EdgeInteraction interaction);
    
    public static Model create(ContextualEgoNetwork contextualEgoNetwork) {
    	return new GNNModel(contextualEgoNetwork);
    }

}