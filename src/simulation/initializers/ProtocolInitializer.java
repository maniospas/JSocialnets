package simulation.initializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.Control;
import peersim.core.Network;
import simulation.protocols.DGNNProtocol;

/**
 * Control to build the social overlay network.
 *
 */
public class ProtocolInitializer implements Control{

	private String inputFile=null;
	
	public ProtocolInitializer(String prefix) {
		DGNNProtocol.dgnnProtocolId=Configuration.getPid(prefix + "." + DGNNProtocol.DGNN_PROTOCOL_ID);
		try{
			inputFile=Configuration.getString(prefix + ".input");
		} catch(MissingParameterException e) {
			inputFile=null;
		}
		DGNNProtocol.prefix=prefix;
		contextualegonetwork.Utils.development = false;
	}
	
	@Override
	public boolean execute(){
		DGNNProtocol.idTranslator=new HashMap<String, Integer>();
		int i=0;
		try {
			if(inputFile!=null) {
				BufferedReader input=new BufferedReader(new FileReader(new File(inputFile)));
				String name=null;
				while((name=input.readLine())!=null) {
					DGNNProtocol node=(DGNNProtocol)Network.get(i).getProtocol(DGNNProtocol.dgnnProtocolId);
					node.initialize(name);
					DGNNProtocol.idTranslator.put(node.cenManager.getContextualEgoNetwork().getEgo().getId(), i); //key=node id in the cen, value is the position in the simulator array of nodes
					i++;
				}
				input.close();
			}
			for(; i<Network.size(); i++) {
				DGNNProtocol node=(DGNNProtocol)Network.get(i).getProtocol(DGNNProtocol.dgnnProtocolId);
				node.initialize();
				DGNNProtocol.idTranslator.put(node.cenManager.getContextualEgoNetwork().getEgo().getId(), i); //key=node id in the cen, value is the position in the simulator array of nodes
			}

		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		
		return false;
	}

}
