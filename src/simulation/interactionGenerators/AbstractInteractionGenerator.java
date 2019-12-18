package simulation.interactionGenerators;

import java.util.List;

import peersim.core.Control;
import peersim.core.Network;
import peersim.edsim.EDSimulator;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.server.SimulationTransferProtocol;

public abstract class AbstractInteractionGenerator implements Control {

	private static final String SEPARATOR = "£";

	@Override
	public boolean execute() {
//		get all the interactions for this time slot
		List<String> interactions=nextInteractionBatch();
		for(String interaction : interactions) {
//			generate an event for each interaction
			String[] parts=interaction.split(AbstractInteractionGenerator.SEPARATOR);
			peersim.core.Node interactionSource = Network.get(SimulationTransferProtocol.getServerSideAddress(parts[0]));
			Message event=new Message();
			event.type=MessageType.NEW_INTERACTION;
			event.senderId=parts[0];	//source of the interaction
			event.recipientId=parts[1];	//destination of the interaction
			event.body=parts[3];	//this is the body of the interaction
			event.parameters=parts[4];//these are the parameters to pass alongside the actual interaction
//			add the event to the simulator
			EDSimulator.add(Long.parseLong(parts[2]), event, interactionSource, SimulationTransferProtocol.dgnnProtocolId);
		}
		return false;
	}
	
	public String createInteraction(String senderId, String recipientId, long delay, String body, String parameters) {
		return senderId
				+ SEPARATOR + recipientId
				+ SEPARATOR + delay
				+ SEPARATOR + body 
				+ SEPARATOR + parameters;
		
	}
	
	/**
	 * 
	 * @return a list of strings containing the source and destination user of the interaction, and the delay at which the interaction will take place (with respect to when this method is called), separated by the separator specified in this class (TODO:implement it in a better way) or null 
	 */
	protected abstract List<String> nextInteractionBatch();

}
