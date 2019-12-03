package simulation.interactionGenerators;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Network;
import simulation.protocols.DGNNProtocol;

public class OneInteractionPerUser extends AbstractInteractionGenerator {

	private static Random rng=new Random();
	private static int cycleLength=0;
	
	public OneInteractionPerUser(String prefix){
		cycleLength=Configuration.getInt(prefix + ".step");
	}
	
	@Override
	protected List<String> nextInteractionBatch() {
		List<String> interactionBatch=new LinkedList<String>();
//		for each user
		for(int i=0; i<Network.size(); i++) {
//			generate a random interaction between this node and a random node
			int randomNodeId=rng.nextInt(Network.size());
			while(randomNodeId==i) 
				randomNodeId=rng.nextInt(Network.size());
			int randomDelay=rng.nextInt(cycleLength);
			DGNNProtocol sourceProtocol = (DGNNProtocol) Network.get(i).getProtocol(DGNNProtocol.dgnnProtocolId);
//			add it to the batch (change this to also add the interaction to the source node)]
			String interactionType = "simulation interaction";
			interactionBatch.add(createInteraction("node" + i,
													"node" + randomNodeId,
													randomDelay,
													interactionType,
													sourceProtocol.getMessageBodyAndRegisterInteraction("node" + randomNodeId, interactionType))); //I think this should not be happening here! here the interaction is created,
//																																					but it still has to reach the source node! Maybe this method should be called
//																																					when the destination reaches the destination of the interaction
		}
		
		return interactionBatch;
	}

}
