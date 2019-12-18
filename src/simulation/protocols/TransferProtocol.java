package simulation.protocols;

import simulation.messages.Message;

public interface TransferProtocol {
	void sendMessage(Message message);
	String getStringSeparator();
}
