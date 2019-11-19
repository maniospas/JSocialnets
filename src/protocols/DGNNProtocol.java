package protocols;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import messages.Message;
import messages.MessageType;
import models.Model;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class DGNNProtocol implements EDProtocol, CDProtocol {

//	id number of the protocol
	public static int dgnnProtocolId;
	private static Map<String, Integer> idTranslator;
//	id string of the protocol
	public static final String DGNN_PROTOCOL_ID="dgnn";
//	prefix for getting stuff from the configuration file
	public static String prefix=null;
	
//	maybe the CEN? or maybe in another class/package?
	public ContextualEgoNetwork contextualEgoNetwork;
//	a reference to the model
	public Model model;
	
	public DGNNProtocol(String prefix) {}
	
	public boolean initialize() {
//		initialize the CEN
		return false;
	}
	
	@Override
	public void nextCycle(Node node, int protocolId) {
		model.doPeriodicStuff();
	}

	@Override
	public void processEvent(Node node, int protocolId, Object msg) {
		
		Message message=(Message) msg;
//		debugprint(message);
		switch(message.type){
		case EGO_NETWORK_QUERY: //can replay right away
			handleENQ(node, message);
			break;
		case EGO_NETWORK_REPLY: //update the CEN
			handleENR(message);			
			break;
		case MODEL_PUSH: //pass to the learner
			model.updateFromNeighbour(message);
			break;
		default:
			break;
		}

	}
	
	/**
	 * Handling of EGO_NETWORK_QUERY message
	 */
	private void handleENQ(Node node, Message message) {
//		get sender node
		Context context=contextualEgoNetwork.getCurrentContext();
		contextualegonetwork.Node sender=null;
		for(contextualegonetwork.Node neighbour : context.getNodes()) {
			if(message.senderId.compareTo(neighbour.getId())==0) {
				sender=neighbour;
			}
		}
		
//		scan its friend list and check which ones are in common
		Set<String> commonNeighboursIds=new HashSet<String>();
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
		sendMessage(reply, node);
	}
	
	/**
	 * method to handle an EGO_NETWORK_REPLY message
	 * @param message
	 */
	private void handleENR(Message message) {
//		get the replying node by its id
		Context context=contextualEgoNetwork.getCurrentContext();
		contextualegonetwork.Node sender=null;
		for(contextualegonetwork.Node neighbour : context.getNodes()) {
			if(message.senderId.compareTo(neighbour.getId())==0) {
				sender=neighbour;
			}
		}
		
//		get the common neighbours ids from the message
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
	
	/**
	 * utility method to send a message
	 * @param message the message to be sent (its fields must be already filled in)
	 * @param senderNode the peersim node sending the message
	 */
	public static void sendMessage(Message message, Node senderNode) {
		//UnreliableTransport transport = (UnreliableTransport) (Network.prototype).getProtocol(transportid);
		Node recipient;
		try{
			recipient=Network.get(DGNNProtocol.idTranslator.get(message.recipientId));
		} catch(NullPointerException e){
//			skipping non existent node in the network
			return;
		}
		Transport transport=(Transport)senderNode.getProtocol(FastConfig.getTransport(dgnnProtocolId));
        transport.send(senderNode, recipient, message, dgnnProtocolId);
	}
	
	@Override
	public Object clone() {
		return new DGNNProtocol(prefix);
	}

}
