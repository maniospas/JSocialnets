package simulation.interactionGenerators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import peersim.config.Configuration;

public class CommaSeparatedInteractions extends AbstractInteractionGenerator {

	BufferedReader input;
	private static int cycleLength=0;
	private long cycleNumber=0;
	private String lastLine=null;
	private boolean finished = false;
	
	public CommaSeparatedInteractions(String prefix){
		cycleLength=Configuration.getInt(prefix + ".step");
		try {
			input=new BufferedReader(new FileReader(new File(Configuration.getString(prefix + ".input"))));
		} catch (FileNotFoundException e) {
			System.err.println("Must specify a valid input file in the configuration file. The property name is \"input\"");
			e.printStackTrace();
		}
	}

	@Override
	public boolean execute() {
		super.execute();
		return finished;
	}
	
	@Override
	protected List<String> nextInteractionBatch() {
		long lowerTimeLimit=cycleNumber*cycleLength;
		cycleNumber++;
		long upperTimeLimit=(cycleNumber)*cycleLength;
		LinkedList<String> interactionBatch=new LinkedList<String>();
		String line;
//		start reading lines
		try {
			
			//if there was a spare line from the previous interaction batch parse it and generate the interaction event
			if(lastLine!=null) {
				String[] parts=lastLine.split(",");
				String source=parts[0];
				String destination=parts[1];
				Long time=Long.parseLong(parts[2]);
//				if the time of the next interaction is after the end of the timeslot stop the process and save the last line
				if(time>upperTimeLimit) {
					return interactionBatch;
				}
//				else add the interaction in the interaction batch
				interactionBatch.add(createInteraction(source, destination, time-lowerTimeLimit, "generic_interaction", " "));
			}
			
//			otherwise read the file
			while((line=input.readLine())!=null) {
				String[] parts=line.split(",");
				String source=parts[0];
				String destination=parts[1];
				Long time=Long.parseLong(parts[2]);
//				skip the interaction if source and destination are the same node
				if(source.compareTo(destination)==0) continue;
//				if the time of the next interaction is after the end of the timeslot stop the process and save the last line
				if(time>upperTimeLimit) {
					lastLine=line;
					return interactionBatch;
				}
//				else add the interaction in the interaction batch
				interactionBatch.add(createInteraction(source, destination, time-lowerTimeLimit, "generic_interaction", " "));
			}
			finished = true;
			lastLine=null;
			return interactionBatch;
		} catch (IOException e) {
			e.printStackTrace();
		}
		lastLine=null;
		return interactionBatch;
	}

}
