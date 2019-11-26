package simulation.managers;

import java.util.HashSet;
import java.util.Set;

import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.protocols.DGNNProtocol;

public class ContextualEgoNetworkManager {
	private static long numberOfManagers = 0;
	private ContextualEgoNetwork contextualEgoNetwork;
	
	public ContextualEgoNetworkManager() {
		numberOfManagers += 1;
		contextualEgoNetwork=new ContextualEgoNetwork(new contextualegonetwork.Node("node"+numberOfManagers, null));
	}
	
	/**
	 * initialize this object
	 * @return true if something went wrong and the simulation must be stopped, false otherwise
	 */
	public boolean initialize() {
//		do initialization stuff, like populate the cen object
		return false;
	}
	
	public ContextualEgoNetwork getContextualEgoNetwork() {
		return contextualEgoNetwork;
	}
	
	/**
	 * Handling of EGO_NETWORK_QUERY message
	 */
	public void handleENQ(peersim.core.Node node, Message message) {
//		get sender node
		Context context=contextualEgoNetwork.getCurrentContext();
		contextualegonetwork.Node sender=null;
		for(contextualegonetwork.Node neighbour : context.getNodes()) {
			if(message.senderId.compareTo(neighbour.getId())==0) {
				sender=neighbour;
			}
		}
		
//		scan its friend list and check which ones are in common
		HashSet<String> commonNeighboursIds=new HashSet<String>();
		@SuppressWarnings("unchecked")
		Set<String> alterNeighboursIds=(Set<String>) message.body;
		for(String alterNeighbourId : alterNeighboursIds) {
			for(contextualegonetwork.Node egoNeighbour : context.getNodes()) {
				if(alterNeighbourId.compareTo(egoNeighbour.getId())==0) {
//					if a common node is found, add the missing edge
					context.addEdge(sender, egoNeighbour);
					commonNeighboursIds.add(alterNeighbourId);
				}
			}
		}
		
//		when the scan is complete, send back the set of Ids of common neighbours
		Message reply=new Message();
		reply.type=MessageType.EGO_NETWORK_REPLY;
		reply.senderId=contextualEgoNetwork.getEgo().getId();
		reply.recipientId=message.senderId;
		reply.body=commonNeighboursIds;
		DGNNProtocol.sendMessage(reply, node);
	}
	
	/**
	 * method to handle an EGO_NETWORK_REPLY message
	 * @param message
	 */
	public void handleENR(Message message) {
//		get the replying node by its id
		Context context=contextualEgoNetwork.getCurrentContext();
		contextualegonetwork.Node sender=null;
		for(contextualegonetwork.Node neighbour : context.getNodes()) {
			if(message.senderId.compareTo(neighbour.getId())==0) {
				sender=neighbour;
			}
		}
		
//		get the common neighbours ids from the message
		@SuppressWarnings("unchecked")
		Set<String> commonNeighboursIds=(Set<String>) message.body;
		for(String commonNeighbourId : commonNeighboursIds) {
			for(contextualegonetwork.Node egoNeighbour : context.getNodes()) {
				if(commonNeighbourId.compareTo(egoNeighbour.getId())==0) {
//					if a common node is found, add the missing edge
					context.addEdge(sender, egoNeighbour);
				}
			}
		}
	}
}
