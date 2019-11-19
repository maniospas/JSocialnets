package initializers;

import peersim.config.Configuration;
import peersim.core.Control;
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
		return false;
	}

}
