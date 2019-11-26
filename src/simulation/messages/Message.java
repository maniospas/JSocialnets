package simulation.messages;

public class Message implements Cloneable{

	public MessageType type=MessageType.EGO_NETWORK_QUERY;
	public Cloneable body=null;
	public String senderId=null;
	public String recipientId=null;
	
	@Override
	public Message clone() {
		Message copy=new Message();
		copy.type=this.type;
		copy.body=this.clone();
		copy.senderId=this.senderId;
		copy.recipientId=this.recipientId;
		return copy;
	}
	
}
