import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class Experiment {

	public static void main(String[] args) throws Exception {
		// check input data
		String edgePath = "datasets/fb-messages.edges";
		if(!edgePath.endsWith(".edges"))
			throw new RuntimeException("Should give an .edges file");

		
		//write a new cfg file if it doesn't exist already
		String cfgPath = edgePath.substring(0, edgePath.length()-5)+"cfg";
		//if(!(new File(cfgPath)).exists())
		{
			//create node list file
			String nodePath = edgePath.substring(0, edgePath.length()-5)+"nodes";
			HashSet<String> nodes = new HashSet<String>();
			BufferedReader edgeReader = new BufferedReader(new FileReader(new File(edgePath)));
			String line = null;
			while((line=edgeReader.readLine())!=null) {
				String[] splt = line.split("\\,");
				nodes.add(splt[0]);
				nodes.add(splt[1]);
			}
			edgeReader.close();
			BufferedWriter nodeWritter = new BufferedWriter(new FileWriter(new File(nodePath)));
			for(String node : nodes) {
				nodeWritter.write(node);
				nodeWritter.newLine();
			}
			nodeWritter.close();
			//create the new cfg file
			BufferedWriter cfgWritter = new BufferedWriter(new FileWriter(new File(cfgPath)));
			cfgWritter.write("SIZE "+nodes.size());
			cfgWritter.newLine();
			cfgWritter.write("init.4setupper.input "+nodePath);
			cfgWritter.newLine();
			cfgWritter.write("control.1interactiongenerator.input "+edgePath);
			cfgWritter.newLine();
			cfgWritter.newLine();
			BufferedReader cfgReader = new BufferedReader(new FileReader(new File("run.cfg.prototype")));
			while((line=cfgReader.readLine())!=null) {
				cfgWritter.write(line);
				cfgWritter.newLine();
			}
			cfgReader.close();
			cfgWritter.close();
		}
		
		
		//run the peersim simulator
		String[] params = new String[1];
		params[0] = cfgPath;
		peersim.Simulator.main(params);
	}

}
