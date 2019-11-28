package simulation.messages;

public enum MessageType {

	MODEL_PUSH,			// the body will contain the model
	EGO_NETWORK_QUERY,	// the body will contain the adjacency list of the node who sent the message as a set of node ids
	EGO_NETWORK_REPLY,	// the body will contain the set of nodeids that are in common with the node who sent the original query message
	NEW_INTERACTION		// the body will be empty, this is used to model the asynchronous interactions
}
