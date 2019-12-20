package simulation.managers;

import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.protocols.DGNNProtocol;

public class ContextualEgoNetworkManager {
	private ContextualEgoNetwork contextualEgoNetwork;
	private DGNNProtocol protocol;
	private final String stringSeparator = "$";//not necessarily the same as in SimulationTransferProtocol
	
	public ContextualEgoNetworkManager(String id, DGNNProtocol protocol) {
		contextualEgoNetwork = ContextualEgoNetwork.createOrLoad(id, null);
		contextualEgoNetwork.setCurrent(contextualEgoNetwork.createContext("Default context"));
		this.protocol = protocol;
	}
	
	public ContextualEgoNetwork getContextualEgoNetwork() {
		return contextualEgoNetwork;
	}
	
	/**
	 * Handling of EGO_NETWORK_QUERY message
	 */
	public void handleENQ(peersim.core.Node node, Message message) {
		updateCen(message.senderId);
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
		String[] alterNeighboursIds=message.body.split(stringSeparator);
		for(String alterNeighbourId : alterNeighboursIds) {
			for(contextualegonetwork.Node egoNeighbour : context.getNodes()) {
				if(alterNeighbourId.compareTo(egoNeighbour.getId())==0) {
//					if a common node is found, add the missing edge
					context.addEdge(sender, egoNeighbour);
					commonNeighboursIds.append(alterNeighbourId);
					commonNeighboursIds.append(stringSeparator);
//					also send a message to the common neighbours to notify them					
					Message update=new Message();
					update.type=MessageType.EGO_NETWORK_NEW_EDGE;
					update.senderId=contextualEgoNetwork.getEgo().getId();
					update.recipientId=alterNeighbourId;
					update.body=message.senderId + stringSeparator + message.recipientId;
					protocol.sendMessage(update);
				}
			}
		}
		
//		when the scan is complete, send back the set of Ids of common neighbours
		Message reply=new Message();
		reply.type=MessageType.EGO_NETWORK_REPLY;
		reply.senderId=contextualEgoNetwork.getEgo().getId();
		reply.recipientId=message.senderId;
		reply.body=commonNeighboursIds.toString();
		protocol.sendMessage(reply);

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
		String[] commonNeighboursIds=message.body.split(stringSeparator);
		for(String commonNeighbourId : commonNeighboursIds) {
			for(contextualegonetwork.Node egoNeighbour : context.getNodes()) {
				if(commonNeighbourId.compareTo(egoNeighbour.getId())==0) {
//					if a common node is found, add the missing edge
					context.addEdge(sender, egoNeighbour);
				}
			}
		}
	}

	/**
	 * method to handle an EGO_NETWORK_NEW_EDGE message
	 * @param message
	 */
	public void handleENNE(String sourceId, String destinationId) {
		Context context=contextualEgoNetwork.getCurrentContext();
		contextualegonetwork.Node source=contextualEgoNetwork.getOrCreateNode(sourceId, null);
		contextualegonetwork.Node destination=contextualEgoNetwork.getOrCreateNode(destinationId, null);
		if(!context.getNodes().contains(source)) context.addNode(source);
		if(!context.getNodes().contains(destination)) context.addNode(destination);
		if(context.getEdge(source, destination)==null) context.addEdge(source, destination);
	}
	
	/**
	 * update the cen, possibly adding new nodes and edges
	 * @param alterId The id of the node with which the interaction happened
	 * @param iteractionType the type of the interaction
	 * @return false if no edges were added, true if some edges were added and therefore an update to the cen of the neighbours is needed.
	 */
	public boolean updateCen(String alterId) {
		boolean modification=false;
//		create the node or add it to the current context if necessary
		contextualegonetwork.Node alter = contextualEgoNetwork.getOrCreateNode(alterId, null);
		if(!contextualEgoNetwork.getCurrentContext().getNodes().contains(alter))
			contextualEgoNetwork.getCurrentContext().addNode(alter);
		if(contextualEgoNetwork.getCurrentContext().getEdge(contextualEgoNetwork.getEgo(), alter)==null) {
			modification=true;
		}
		contextualEgoNetwork.getCurrentContext().getOrAddEdge(contextualEgoNetwork.getEgo(), alter);
		return modification;
	}
}
