package models;

import java.util.Arrays;
import java.util.HashMap;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Edge;
import contextualegonetwork.Interaction;
import contextualegonetwork.Node;

public class LocalGNNModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	private double[] egoParameters;
	private HashMap<Node, double[]> alterParameters;
	
    protected LocalGNNModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    	egoParameters = new double[10];
    	for(int i=0;i<egoParameters.length;i++)
    		egoParameters[i] = Math.random();
    	alterParameters = new HashMap<Node, double[]>();
    }

	@Override
	public void newInteraction(EdgeInteraction interaction) {
		Node alter = alter(interaction.getEdge());
	}

	@Override
	public void newInteraction(EdgeInteraction interaction, String neighborModelParameters, boolean isReply) {
		double[] params = fromString(neighborModelParameters);
		Node alter = alter(interaction.getEdge());
		alterParameters.put(alter, params);
		for(int i=0;i<egoParameters.length;i++)
			egoParameters[i] = egoParameters[i]*0.9 + params[i]*0.1;
	}

	@Override
	public void doPeriodicStuff(long atTime) {
	}

	@Override
	public String getModelParameters(EdgeInteraction interaction) {
		return Arrays.toString(egoParameters);
	}

	@Override
	public double evaluate(EdgeInteraction interaction) {
		if(!alterParameters.containsKey(alter(interaction.getEdge())))
			return 0;
		HashMap<Edge, Double> evaluations = new HashMap<Edge, Double>();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) {
			double[] alterParams = alterParameters.get(alter(edge));
			if(alterParams!=null)
				evaluations.put(edge, dot(egoParameters, alterParams)/Math.sqrt(dot(alterParams, alterParams)));
		}
		double assignedEvaluation = evaluations.getOrDefault(alter(interaction.getEdge()), 0.);
		int topk = 0;
		for(double value : evaluations.values())
			if(value>=assignedEvaluation)
				topk += 1;
		if(topk<=3)
			return 1;
		return 0;
	}
	
	protected Node alter(Edge edge) {
		if(edge.getDst()==contextualEgoNetwork.getEgo())
			return edge.getSrc();
		if(edge.getSrc()==contextualEgoNetwork.getEgo())
			return edge.getDst();
		throw new RuntimeException("Cannot retrieve alter");
	}
	
	protected static double[] fromString(String neighborModelParameters) {
		String[] unparsedParams = neighborModelParameters.substring(1,neighborModelParameters.length()-1).split(",");
		double[] params = new double[unparsedParams.length];
		for(int i=0;i<params.length;i++)
			params[i] = Double.parseDouble(unparsedParams[i]);
		return params;
	}
	
	protected static double dot(double[] v1, double[] v2) {
		double sum = 0;
		for(int i=0;i<v1.length && i<v2.length;i++)
			sum += v1[i]*v2[i];
		return sum;
	}
}
