package simulation.interactionGenerators;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import peersim.config.Configuration;
import peersim.core.Network;

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
			while(randomNodeId==i) {
				randomNodeId=rng.nextInt(Network.size());
			}
			int randomDelay=rng.nextInt(cycleLength);
//			add it to the batch
			interactionBatch.add("node" + i + SEPARATOR + "node" + randomNodeId + SEPARATOR + randomDelay);
		}
		
		return interactionBatch;
	}

}
