package initializers;

import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import protocols.DGNNProtocol;

/**
 * Control to build the social overlay network.
 *
 */
public class ProtocolInitializer implements Control{

	public ProtocolInitializer(String prefix) {
		DGNNProtocol.dgnnProtocolId=Configuration.getPid(prefix + "." + DGNNProtocol.DGNN_PROTOCOL_ID);
		DGNNProtocol.prefix=prefix;
	}
	
	@Override
	public boolean execute() {
//		step 1: get the ego network of the user corresponding to this node.
		DGNNProtocol.idTranslator=new HashMap<String, Integer>();
		for(int i=0; i<Network.size(); i++) {
			DGNNProtocol node=(DGNNProtocol)Network.get(i).getProtocol(DGNNProtocol.dgnnProtocolId);
			node.initialize();
			DGNNProtocol.idTranslator.put(""+i, i); //TODO: modify with key=node id in the cen
		}
		return false;
	}

}
