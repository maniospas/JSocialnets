package simulation.managers;

import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.protocols.DGNNProtocol;

public class ContextualEgoNetworkManager {
	private static long numberOfManagers = -1;
	private ContextualEgoNetwork contextualEgoNetwork;
	
	public ContextualEgoNetworkManager() {
		numberOfManagers += 1;
		contextualEgoNetwork = new ContextualEgoNetwork(new contextualegonetwork.Node("node"+numberOfManagers, null));
		contextualEgoNetwork.setCurrent(contextualEgoNetwork.createContext("Default context"));
	}
	
	/**
	 * initialize this object
	 * @return true if something went terribly wrong and the simulation must be stopped, false otherwise
	 */
	public boolean initialize() {
//		do initialization stuff, like populate the cen object
		return false;
	}
	
	public ContextualEgoNetwork getContextualEgoNetwork() {
		return contextualEgoNetwork;
	}
	
	public void addEdgeIfNeeded(String destination) {
//		TODO: write this method? or find a better way to do it?
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
		StringBuilder commonNeighboursIds=new StringBuilder();
		String[] alterNeighboursIds=message.body.split(DGNNProtocol.SEPARATOR);
		for(String alterNeighbourId : alterNeighboursIds) {
			for(contextualegonetwork.Node egoNeighbour : context.getNodes()) {
				if(alterNeighbourId.compareTo(egoNeighbour.getId())==0) {
//					if a common node is found, add the missing edge
					context.addEdge(sender, egoNeighbour);
					commonNeighboursIds.append(alterNeighbourId);
					commonNeighboursIds.append(DGNNProtocol.SEPARATOR);
				}
			}
		}
		
//		when the scan is complete, send back the set of Ids of common neighbours
		Message reply=new Message();
		reply.type=MessageType.EGO_NETWORK_REPLY;
		reply.senderId=contextualEgoNetwork.getEgo().getId();
		reply.recipientId=message.senderId;
		reply.body=commonNeighboursIds.toString();
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
		
//		get the common neighbour ids from the message
		String[] commonNeighboursIds=message.body.split(DGNNProtocol.SEPARATOR);
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
