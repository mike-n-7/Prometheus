
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Memory contains the overall structure of what is and isn't relevant. Replaces expert system.
 * @author Michael Noseworthy
 *
 */
public class OldMemory {
	CircularArray memory;
	private int memoryDepth = 100;
	
	// Need a structure to store all rules (even when not in memory). 
	LinkedList<OldKnowledgeNode> KnowledgeBase = new LinkedList<OldKnowledgeNode>();
	
	// Constructor. Takes the size of the memory as well as the filename where the rules are stored.
	OldMemory(String s) {
		memory = new CircularArray(100, memoryDepth);
		
		// Load the KnowledgeBase.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(s));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String strIn = line.split("->")[0].trim();
				String strOut = "";
				if ((line.split("->").length > 1)) strOut = line.split("->")[1].trim();
				KnowledgeBase.add(new OldKnowledgeNode(strIn, strOut.split(",")));
			}
		} catch (FileNotFoundException e) {
			System.out.println("The input file was not found.");
		} catch (IOException e) {
			System.out.println("IO Exception.");
		}
	}
	
	// Given a bunch of facts about the environment, 
	// 1. Add facts to memory.
	// 2. Go through memory from start to limit executing any rule we encounter. 
	//		- rule should be passed memory[start : limit] to execute facts.
	// 3. Rotate the memory.
	
	void Process(Queue<String> env) {
		// Env currently only has tags from the environment.
		LinkedList<String> newFacts = new LinkedList<String>();
		LinkedList<String> newActions = new LinkedList<String>();
		
		// Reset all knowledge nodes.
		for (OldKnowledgeNode kn : KnowledgeBase) kn.reset();
		
		// Add tags that are stored in the active section of the memory.
		for (int j = 0; j < memory.getLimit(); j++) {
			LinkedList<KnowledgeNode> bucket = memory.getBucket(j);
			for (KnowledgeNode kn : bucket) {
				env.addAll(kn.getOutFacts());
			}
		}
		
		// Decay every tag.
		memory.rotate();
		
		// Process knowledge nodes.
		// TODO: Stop when we have nothing left to execute. DO NOT REMOVE TAGS from the queue.
		while (! env.isEmpty()) {
			String tag = env.remove();
			for (OldKnowledgeNode node : KnowledgeBase) {
				node.process(tag);
				if (node.isTriggered() == true) {
					memory.insertStart(node);
					newFacts.addAll(node.getOutFacts());
					newActions.addAll(node.getOutActions());
					env.addAll(node.getOutFacts());
				}
			}
		}
		
		// TODO: Pass tags through the expert system.
		
		System.out.println("Facts: " + newFacts);
		System.out.println("Actions: " + newActions);
		newFacts.clear();newActions.clear();
	}
}
