package simulation.protocols;

import java.util.ArrayList;
import java.util.Map;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Interaction;
import contextualegonetwork.Utils;
import models.Model;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import simulation.managers.ContextualEgoNetworkManager;
import simulation.messages.Message;
import simulation.messages.MessageType;

public class DGNNProtocol implements EDProtocol, CDProtocol {

//	id number of the protocol
	public static int dgnnProtocolId;
//	artificial thing to know on which node runs a particular instance of the protocol
	public static Map<String, Integer> idTranslator;
//	id string of the protocol
	public static final String DGNN_PROTOCOL_ID="dgnn";
	public static final String SEPARATOR = "$";
//	prefix for getting stuff from the configuration file
	public static String prefix=null;
//	a reference to the peersim node
//	private Node peersimNode;
	
//	maybe the CEN? or maybe in another class/package?
	public ContextualEgoNetworkManager cenManager;
//	a reference to the model
	public Model model;
	
	public DGNNProtocol(String prefix) {}
	
	/**
	 * initialize this object with all the modules running on it (the cen manager, and the gnn model at least)
	 * @return true if something went wrong and the simulation must be stopped, false otherwise
	 */
	public boolean initialize() {
		cenManager = new ContextualEgoNetworkManager();
		if(cenManager.initialize()) return true;
		model = Model.create(cenManager.getContextualEgoNetwork());
//		peersimNode=node;
		return false;
	}
	
	@Override
	public void nextCycle(peersim.core.Node node, int protocolId) {
//		also let the model do some periodic stuff if needed
		model.doPeriodicStuff(Utils.getCurrentTimestamp());
	}

	@Override
	public void processEvent(peersim.core.Node node, int protocolId, Object msg) {
		Message message=(Message) msg;
		switch(message.type){
			case EGO_NETWORK_QUERY: //can reply right away
				cenManager.handleENQ(node, message);
				break;
			case EGO_NETWORK_REPLY: //update the CEN
				cenManager.handleENR(message);			
				break;
			case EGO_NETWORK_NEW_EDGE:
				String[] parts=message.body.split(SEPARATOR);
				cenManager.handleENNE(parts[0], parts[1]);
				break;
			case MODEL_PUSH: //pass to the learner
//				the message will contain the source of the interaction, the type of the interaction, and the model of the node generating the interaction
				ContextualEgoNetwork cen = cenManager.getContextualEgoNetwork();
				contextualegonetwork.Node senderNode = cen.getOrCreateNode(message.senderId, null);
				contextualegonetwork.Node recepientNode = cen.getOrCreateNode(message.recipientId, null);
//				register the interaction in the cen
				cenManager.handleENNE(message.senderId, message.recipientId);
				Interaction interaction = cen.getCurrentContext().getEdge(senderNode, recepientNode).addDetectedInteraction(message.body);
				model.newInteraction(interaction, message.parameters);
				break;
			case NEW_INTERACTION: //a new interaction happened!
				//update local CEN
				cen = cenManager.getContextualEgoNetwork();
				senderNode = cen.getOrCreateNode(message.senderId, null);
				recepientNode = cen.getOrCreateNode(message.recipientId, null);
				if(cenManager.updateCen(message.recipientId)) {
//					if the cen was updated (new edge was created), we also need to update the neighbours' cen
					ArrayList<contextualegonetwork.Node> neighbours=cen.getCurrentContext().getNodes();
					StringBuilder adjacencyList=new StringBuilder();
					for(contextualegonetwork.Node neighbour : neighbours) {
						adjacencyList.append(neighbour);
						adjacencyList.append(SEPARATOR);
					}
					Message contextUpdate=new Message();
					contextUpdate.type=MessageType.EGO_NETWORK_QUERY;
					contextUpdate.senderId=cen.getEgo().getId();
					contextUpdate.recipientId=message.recipientId;
					contextUpdate.body=adjacencyList.toString();
					DGNNProtocol.sendMessage(contextUpdate, node);
				}
//				register the interaction in the cen
				interaction = cen.getCurrentContext().getEdge(senderNode, recepientNode).addDetectedInteraction(message.body);
				model.newInteraction(interaction, message.parameters);
				//push the model and the interaction to the destination of the interaction
				Message reply=new Message();
				reply.type=MessageType.MODEL_PUSH;
				reply.senderId=cen.getEgo().getId();
				reply.recipientId=message.recipientId;
				reply.body=message.body;
				reply.parameters=model.getModelParameters(interaction); //btw, why is a parameter needed here? 
				DGNNProtocol.sendMessage(reply, node);
				break;
			default:
				break;
		}

	}
	
	/**
	 * utility method to broadcast something to all the neighbours of the sender node
	 * @param message
	 * @param senderNode
	 * @deprecated =D
	 */
	/*public void sendToAllAlters(Message message, peersim.core.Node senderNode) {
//		get all neighbours
		for(contextualegonetwork.Node neighbour : cenManager.getContextualEgoNetwork().getCurrentContext().getNodes()) {
//			copy the message and modify the recipient
			Message copy=message.clone();
			copy.recipientId=neighbour.getId();
			sendMessage(copy, senderNode);
		}
	}*/
	
	/**
	 * utility method to send a message
	 * @param message the message to be sent (its fields must be already filled in)
	 * @param senderNode the peersim node sending the message
	 */
	public static void sendMessage(Message message, peersim.core.Node senderNode) {
		//UnreliableTransport transport = (UnreliableTransport) (Network.prototype).getProtocol(transportid);
		peersim.core.Node recipient;
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
	
	public static void debugprint(Object... messages){
		System.out.print("DEBUG|||");
		for(Object message : messages){
			System.out.print(" " + message);
		}
		System.out.println();
	}

}
