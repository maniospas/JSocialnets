package simulation.interactionGenerators;

import java.util.List;

import peersim.core.Control;
import peersim.core.Network;
import peersim.edsim.EDSimulator;
import simulation.messages.Message;
import simulation.messages.MessageType;
import simulation.protocols.DGNNProtocol;

public abstract class AbstractInteractionGenerator implements Control {

	static final String SEPARATOR = "£";

	@Override
	public boolean execute() {
//		get all the interactions for this time slot
		List<String> interactions=nextInteractionBatch();
		for(String interaction : interactions) {
//			generate an event for each interaction
			String[] parts=interaction.split(AbstractInteractionGenerator.SEPARATOR);
			peersim.core.Node interactionSource=Network.get(DGNNProtocol.idTranslator.get(parts[0]));
			Message event=new Message();
			event.type=MessageType.NEW_INTERACTION;
			event.senderId=parts[0];	//source=source of the interaction
			event.recipientId=parts[1];	//recipient=destination of the interaction
//			add the event to the simulator
			EDSimulator.add(Integer.parseInt(parts[2]), event, interactionSource, DGNNProtocol.dgnnProtocolId);
		}
		return false;
	}
	
	/**
	 * 
	 * @return a list of strings containing the source and destination user of the interaction, and the delay at which the interaction will take place (with respect to when this method is called), separated by the separator specified in this class (TODO:implement it in a better way) or null 
	 */
	protected abstract List<String> nextInteractionBatch();

}
