package simulation.server;

import java.util.HashMap;

import contextualegonetwork.Utils;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.transport.Transport;
import simulation.messages.Message;
import simulation.protocols.TransferProtocol;

public class SimulationTransferProtocol implements TransferProtocol {
	public static int dgnnProtocolId;
	private static final String stringSeparator = "$";
	
	private static HashMap<String, Integer> idTranslator = new HashMap<String, Integer>();
	
	public SimulationTransferProtocol() {
	}
	
	public int get(String id) {
		return idTranslator.get(id);
	}
	
	public static void put(String id, int value) {
		idTranslator.put(id, value);
	}
	
	public static int getServerSideAddress(String id) {
		return idTranslator.get(id);
	}

	@Override
	public void sendMessage(Message message) {
		peersim.core.Node sender;
		peersim.core.Node recipient;
		try{
			sender=Network.get(idTranslator.get(message.senderId));
			recipient=Network.get(idTranslator.get(message.recipientId));
		}
		catch(NullPointerException e){
			Utils.error(e);
			return;
		}
		Transport transport=(Transport)sender.getProtocol(FastConfig.getTransport(dgnnProtocolId));
        transport.send(sender, recipient, message, dgnnProtocolId);
	}
	
	@Override
	public String getStringSeparator() {
		return stringSeparator;
	}
}
