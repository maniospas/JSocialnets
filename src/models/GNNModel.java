package models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Edge;
import contextualegonetwork.Interaction;
import contextualegonetwork.Node;
import contextualegonetwork.Utils;
import models.GNN.Tensor;

public class GNNModel implements Model {
	private ContextualEgoNetwork contextualEgoNetwork;
	private Tensor relation;
	private Tensor egoId;
	private HashMap<Node, Tensor> alterIds;
	private static int dims = 10;
	private Edge lastEdge = null;
	private Edge prevEdge = null;

	
    protected GNNModel(ContextualEgoNetwork contextualEgoNetwork) {
    	this.contextualEgoNetwork = contextualEgoNetwork;
    	egoId = new Tensor(dims);
    	egoId.randomize();
    	egoId.normalize();
    	if(relation==null) {
    		relation = new Tensor(dims);
    		relation.setOnes();
    		relation.normalize();
    	}
    	alterIds = new HashMap<Node, Tensor>();
    }

	private static double getVotingStrength(Interaction interaction) {
		return 0;
	}

	@Override
	public void newInteraction(Interaction interaction) {
	}

	@Override
	public synchronized void newInteraction(Interaction interaction, String neighborModelParameters, boolean isReply) {
		if(!isReply) {
			prevEdge = lastEdge;
			lastEdge = interaction.getEdge();
		}
		String[] params = neighborModelParameters.split("\\|");
		Tensor alterId = new Tensor(params[0]);
		//Tensor alterRelation = new Tensor(params[1]);
		alterIds.put(interaction.getEdge().getAlter(), alterId);
		Tensor accum = egoId.zero();
		Tensor relationAccum = relation.zero();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) {
			Tensor otherAlterId = alterIds.get(edge.getAlter());
			if(otherAlterId==null || edge.getSrc()!=contextualEgoNetwork.getEgo())
				continue;
			double target = otherAlterId==alterId?1:0;
			double weight = target==1?contextualEgoNetwork.getCurrentContext().getEdges().size():1;
			double output = 1./(1+Math.exp(-relation.dot(egoId, otherAlterId)));
			double partial = (1-target)*output - target*(1-output);
			partial *= weight;
			accum = accum.add(relation.multiply(otherAlterId).multiply(partial));
			relationAccum = relationAccum.add(egoId.multiply(otherAlterId).multiply(partial));
		}
		//accum.add(egoId.multiply(0.1));
		egoId = egoId.add(accum.multiply(-1));
		egoId.normalize();
		//relation = relation.add(relationAccum.multiply(-0.01/contextualEgoNetwork.getCurrentContext().getEdges().size()));
		//System.out.println(relation);
		double sim = 1;//egoId.dot(alterId)/egoId.norm()/alterId.norm();
		//relation = relation.multiply(1-sim*0.5).add(alterRelation.multiply(sim*0.5));
	}

	@Override
	public void doPeriodicStuff(long atTime) {
	}

	@Override
	public String getModelParameters(Interaction interaction) {
		return egoId.toString()+"|"+relation.toString();
	}

	@Override
	public Map<String, Double> evaluate(Interaction interaction) {
		Map<String, Double> result =  new LinkedHashMap<String, Double>();
		if(interaction.getEdge().getInteractions().size()<=1)
			return result;
		if(!alterIds.containsKey(interaction.getEdge().getAlter()) || interaction.getEdge().getSrc()!=contextualEgoNetwork.getEgo())
			return result;
		HashMap<Edge, Double> evaluations = new HashMap<Edge, Double>();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) {
			if(edge.getSrc()!=contextualEgoNetwork.getEgo())
				continue;
			Tensor alterId = alterIds.get(edge.getAlter());
			if(alterId!=null)
				evaluations.put(edge, relation.dot(alterId, egoId));
		}
		double topk = topK(evaluations, interaction.getEdge());
		result.put("HR@5", topk<=5?1.:0.);
		result.put("HR@3", topk<=3?1.:0.);
		result.put("HR@1", topk<=1?1.:0.);
		//result.put("Degree", (double) evaluations.size());
		//result.put("Top Position", topk/evaluations.size());
		

		evaluations = new HashMap<Edge, Double>();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) 
			if(alterIds.get(edge.getAlter())!=null)
				evaluations.put(edge, Math.random());
		topk = topK(evaluations, interaction.getEdge());
		//result.put("Random HR@5", topk<=5?1.:0.);
		result.put("Random HR@3", topk<=3?1.:0.);
		

		if(lastEdge!=null)
			result.put("Last edge", lastEdge==prevEdge?1.:0.);
		//result.put("parameter norm", dot(egoParameters, egoParameters));
		//result.put("Random Top Position", topk/evaluations.size());
		
		
		return result;
	}
	
	protected static int topK(HashMap<Edge, Double> evaluations, Edge target) {
		double assignedEvaluation = evaluations.get(target);
		int topk = 0;
		for(double value : evaluations.values())
			if(value>=assignedEvaluation)
				topk += 1;
		return topk;
	}
}
