package simulation.messages;

import java.io.Serializable;

public class Message {

	public MessageType type=MessageType.EGO_NETWORK_QUERY;
	public String body=null;//e.g. "like", "talked"
	public String parameters=null;
	public String senderId=null;
	public String recipientId=null;
	
	/*
	@Override
	public Message clone() {
		Message copy=new Message();
		copy.type=this.type;
		copy.body=this.clone();
		copy.senderId=this.senderId;
		copy.recipientId=this.recipientId;
		return copy;
	}*/
	
}
