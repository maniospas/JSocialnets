package simulation.initializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import contextualegonetwork.Utils;
import models.Evaluator;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.Control;
import peersim.core.Network;
import simulation.server.SimulationTransferProtocol;
import simulation.protocols.DGNNProtocol;

/**
 * Control to build the social overlay network.
 *
 */
public class ProtocolInitializer implements Control{
	private String inputFile=null;
	
	public ProtocolInitializer(String prefix) {
		SimulationTransferProtocol.dgnnProtocolId=Configuration.getPid(prefix + ".dgnn");
		try {
			inputFile=Configuration.getString(prefix + ".input");
		}
		catch(MissingParameterException e) {
			inputFile=null;
			Utils.error(e);
		}
		contextualegonetwork.Utils.development = false;
	}
	
	@Override
	public boolean execute(){
		try {
			Evaluator evaluator = Evaluator.create();
			int i=0;
			if(inputFile!=null) {
				BufferedReader input=new BufferedReader(new FileReader(new File(inputFile)));
				String name=null;
				while((name=input.readLine())!=null) {
					DGNNProtocol node = (DGNNProtocol)Network.get(i).getProtocol(SimulationTransferProtocol.dgnnProtocolId);
					node.initialize(name, evaluator, new SimulationTransferProtocol());
					SimulationTransferProtocol.put(node.cenManager.getContextualEgoNetwork().getEgo().getId(), i); //key=node id in the cen, value is the position in the simulator array of nodes
					i++;
				}
				input.close();
			}
			for(; i<Network.size(); i++) {
				DGNNProtocol node=(DGNNProtocol)Network.get(i).getProtocol(SimulationTransferProtocol.dgnnProtocolId);
				node.initialize("node"+i, evaluator, new SimulationTransferProtocol());
				SimulationTransferProtocol.put(node.cenManager.getContextualEgoNetwork().getEgo().getId(), i); //key=node id in the cen, value is the position in the simulator array of nodes
			}

		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		
		return false;
	}

}
