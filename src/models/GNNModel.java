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
	private HashMap<Node, Tensor> alterNegativeIds;
	private static int dims = 10;
	private Node lastAlter = null;
	private Node prevAlter = null;
	private double lastSimilarity = 0;
	
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
    	alterNegativeIds = new HashMap<Node, Tensor>();
    }
    
    private double getEdgeWeight(Edge edge, long currentTime) {
    	return 1;
    }

	private double getVotingStrength(Edge edge, Interaction current) {
		if(edge.getInteractions().size()==0)
			return 0;
		if(edge.getAlter()==current.getEdge().getAlter())
			return contextualEgoNetwork.getCurrentContext().getEdges().size();
		return 1;
	}

	@Override
	public void newInteraction(Interaction interaction) {
	}

	@Override
	public synchronized void newInteraction(Interaction interaction, String neighborModelParameters, boolean isReply) {
		if(!isReply) {
			prevAlter = lastAlter;
			lastAlter = interaction.getEdge().getAlter();
		}
		String[] params = neighborModelParameters.split("\\|");
		Tensor alterId = new Tensor(params[0]);
		//Tensor alterRelation = new Tensor(params[1]);
		alterNegativeIds.put(interaction.getEdge().getAlter(), new Tensor(params[2]));
		alterIds.put(interaction.getEdge().getAlter(), alterId);
		Tensor accum = egoId.zero();
		Tensor relationAccum = relation.zero();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) {
			Tensor otherAlterId = alterIds.get(edge.getAlter());
			if(otherAlterId==null || edge.getSrc()!=contextualEgoNetwork.getEgo())
				continue;
			double weight = getVotingStrength(edge, interaction);
			if(weight==0)
				continue;
			double target = otherAlterId==alterId?1:0;
			if(target==0)
				otherAlterId = alterNegativeIds.get(edge.getAlter());
			double output = 1./(1+Math.exp(-relation.dot(egoId, otherAlterId)));
			double partial = (1-target)*output - target*(1-output);
			partial *= weight;
			accum = accum.add(relation.multiply(otherAlterId).multiply(partial));
			relationAccum = relationAccum.add(egoId.multiply(otherAlterId).multiply(partial));
		}
		//accum.add(egoId.multiply(0.1));
	//	if(count>1)
		//	lastSimilarity = egoId.dot(accum.multiply(-1))/accum.norm();
		egoId = egoId.add(accum.multiply(-1));
		egoId.normalize();
		//relation = relation.add(relationAccum.multiply(-0.01));
		//System.out.println(relation);
		//double sim = 1;//egoId.dot(alterId)/egoId.norm()/alterId.norm();
		//relation = relation.multiply(1-sim*0.5).add(alterRelation.multiply(sim*0.5));
	}

	@Override
	public void doPeriodicStuff(long atTime) {
	}

	@Override
	public String getModelParameters(Interaction interaction) {
		Tensor negative = egoId.zero();
		int count = 0;
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges())
			if(edge.getSrc()!=interaction.getEdge().getAlter() && edge.getSrc()!=contextualEgoNetwork.getEgo()) {
				count += 1;
			}
		if(count!=0) 
			negative.multiply(1./count);
		//negative.normalize();
		return egoId.toString()+"|"+relation.toString()+"|"+negative.toString();
	}

	@Override
	public Map<String, Double> evaluate(Interaction interaction) {
		Map<String, Double> result =  new LinkedHashMap<String, Double>();

		//result.put("New edge", interaction.getEdge().getInteractions().size()<=1?1.:0);
		if(interaction.getEdge().getInteractions().size()<=1) {//>1 keeps only the next friendship predictions, <=1 keeps the prediction of next interaction among friends
			return result;
		}
		if(!alterIds.containsKey(interaction.getEdge().getAlter()) || interaction.getEdge().getSrc()!=contextualEgoNetwork.getEgo())
			return result;
		HashMap<Edge, Double> evaluations = new HashMap<Edge, Double>();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) {
			if(edge.getSrc()!=contextualEgoNetwork.getEgo())
				continue;
			if(interaction.getEdge().getInteractions().size()<=1 && edge.getInteractions().size()>1) //if predicting only next relationship ignore edges with multiple interactions
				continue;
			if(interaction.getEdge().getInteractions().size()>1 && edge.getInteractions().size()==0) //if predicting only next relationship ignore edges with multiple interactions
				continue;
			Tensor alterId = alterIds.get(edge.getAlter());
			if(alterId!=null)
				evaluations.put(edge, relation.dot(alterId, egoId));
		}
		if(evaluations.size()<=1)
			return result;
		double topk = topK(evaluations, interaction.getEdge());
		result.put("HR@5", topk<=5?1.:0.);
		result.put("HR@3", topk<=3?1.:0.);
		result.put("HR@1", topk<=1?1.:0.);
		//result.put("Evals", (double)evaluations.size());
		

		evaluations = new HashMap<Edge, Double>();
		for(Edge edge : contextualEgoNetwork.getCurrentContext().getEdges()) 
			if(alterIds.get(edge.getAlter())!=null)
				evaluations.put(edge, Math.random());
		topk = topK(evaluations, interaction.getEdge());
		result.put("Random HR@1", topk<=1?1.:0.);

		if(lastAlter!=null)
			result.put("Last interactions HR@1", lastAlter==prevAlter?1.:0.);
		
		//result.put("Last similarity", lastSimilarity);
		
		
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
