package simulation.protocols;

import java.util.ArrayList;

import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Edge;
import contextualegonetwork.Interaction;
import contextualegonetwork.Utils;
import models.Evaluator;
import models.Model;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import simulation.managers.ContextualEgoNetworkManager;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.server.SimulationTransferProtocol;

public class DGNNProtocol implements EDProtocol, CDProtocol {
	public ContextualEgoNetworkManager cenManager;
	private Model model;
	private Evaluator evaluator;
	private SimulationTransferProtocol simulationTransferProtocol;
	
	public DGNNProtocol() {}

	public DGNNProtocol(String protocolId) {
	}
	
	/**
	 * initialize this object with all the modules running on it (the cen manager, and the gnn model at least)
	 * @return true if something went wrong and the simulation must be stopped, false otherwise
	 */
	public boolean initialize(String id, Evaluator evaluator, SimulationTransferProtocol simulationTransferProtocol) {
		cenManager = new ContextualEgoNetworkManager(id, this);
		model = Model.create(cenManager.getContextualEgoNetwork());
		this.evaluator = evaluator;
		this.simulationTransferProtocol = simulationTransferProtocol;
		return false;
	}
	
	@Override
	public void nextCycle(peersim.core.Node node, int protocolId) {
//		also let the model do some periodic stuff if needed
		model.doPeriodicStuff(Utils.getCurrentTimestamp());
	}

	@Override
	public void processEvent(peersim.core.Node node, int protocolId, Object msg) {
		Message reply;
		Message message=(Message) msg;
		Interaction interaction;
		Edge edge;
		switch(message.type){
			case EGO_NETWORK_QUERY: //can reply right away
				cenManager.handleENQ(node, message);
				break;
			case EGO_NETWORK_REPLY: //update the CEN
				cenManager.handleENR(message);
				break;
			case EGO_NETWORK_NEW_EDGE:
				String[] parts=message.body.split(simulationTransferProtocol.getStringSeparator());
				cenManager.handleENNE(parts[0], parts[1]);
				// TODO perhaps send an interaction to the model (of the common alters)
				break;
			case MODEL_PUSH: //pass to the learner
//				the message will contain the source of the interaction, the type of the interaction, and the model of the node generating the interaction
				ContextualEgoNetwork cen = cenManager.getContextualEgoNetwork();
				contextualegonetwork.Node senderNode = cen.getOrCreateNode(message.senderId, null);
				contextualegonetwork.Node recepientNode = cen.getOrCreateNode(message.recipientId, null);
//				register the interaction in the cen
				cenManager.handleENNE(message.senderId, message.recipientId); //TODO: remove comment if necessary, but it should work just fine as it is
				edge = cen.getCurrentContext().getEdge(senderNode, recepientNode);
				interaction = edge.addDetectedInteraction(message.body);
				model.newInteraction(new Model.EdgeInteraction(edge, interaction), message.parameters);
//				then reply to the sender with the model of this node
				reply=new Message();
				reply.type=MessageType.MODEL_REPLY;
				reply.senderId=message.recipientId;
				reply.recipientId=message.senderId;
				reply.parameters=model.getModelParameters(new Model.EdgeInteraction(edge, interaction));
				sendMessage(reply);
				break;
			case MODEL_REPLY: //got a model as a reply for pushing mine
				model.doSomethingWithNeighbourModel(message.parameters);
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
						adjacencyList.append(simulationTransferProtocol.getStringSeparator());
					}
					Message contextUpdate=new Message();
					contextUpdate.type=MessageType.EGO_NETWORK_QUERY;
					contextUpdate.senderId=cen.getEgo().getId();
					contextUpdate.recipientId=message.recipientId;
					contextUpdate.body=adjacencyList.toString();
					sendMessage(contextUpdate);
				}
//				register the interaction in the cen
				edge = cen.getCurrentContext().getEdge(senderNode, recepientNode);
				interaction = edge.addDetectedInteraction(message.body);
				if(evaluator!=null)
					evaluator.aggregate(this, model.evaluate(new Model.EdgeInteraction(edge, interaction)));
				model.newInteraction(new Model.EdgeInteraction(edge, interaction));
				//push the model and the interaction to the destination of the interaction
				reply=new Message();
				reply.type=MessageType.MODEL_PUSH;
				reply.senderId=cen.getEgo().getId();
				reply.recipientId=message.recipientId;
				reply.body=message.body;
				reply.parameters=model.getModelParameters(new Model.EdgeInteraction(edge, interaction)); 
				sendMessage(reply);
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
	public void sendMessage(Message message) {
		this.simulationTransferProtocol.sendMessage(message);
	}
	
	@Override
	public Object clone() {
		// TODO Find what needs be done.
		return new DGNNProtocol();
	}
}
