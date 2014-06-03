
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

/**
 * Memory contains the overall structure of what is and isn't relevant. Replaces expert system.
 * @author Michael Noseworthy
 *
 */
public class Memory {
	CircularArray memory;
	
	// Need a structure to store all rules (even when not in memory). 
	LinkedList<KnowledgeNode> KnowledgeBase = new LinkedList<KnowledgeNode>();
	
	// Constructor. Takes the size of the memory as well as the filename where the rules are stored.
	public Memory(String s, int memoryDepth) {
		this.memory = new CircularArray(100, memoryDepth);
		
		// Load the KnowledgeBase.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(s));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String strIn = line.split("->")[0].trim();
				LinkedList<String> listIn = new LinkedList<String>();
				String conditions = new String(line.split("->")[0]);
				String cond = "";
				Boolean add = false;
				for (int i = 0; i < conditions.length(); i++) {
					char c = conditions.charAt(i);
					if (c == '(') {
						add = true;
						cond += c;
					} else if (c == ')') {
						cond += c;
						add = false;
						listIn.push(cond);
						cond = "";
					} else {
						if (add == true) cond += c;
					}
				}
				
				String strOut = "";
				if ((line.split("->").length > 1)) strOut = line.split("->")[1].trim();
				LinkedList<String> listOut = new LinkedList<String>();
				conditions = new String(line.split("->")[1]);
				cond = "";
				add = false;
				for (int i = 0; i < conditions.length(); i++) {
					char c = conditions.charAt(i);
					if (c == '(') {
						add = true;
						cond += c;
					} else if (c == ')') {
						cond += c;
						add = false;
						listOut.push(cond);
						cond = "";
					} else {
						if (add == true) cond += c;
					}
				}
				
				KnowledgeBase.add(new KnowledgeNode(listIn, listOut));
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
	public void Process(Vector envVec, LinkedList<String> subValues, double[] pheromones) {
		Queue<String> env = new LinkedList<String>();
		for (Object v : envVec) env.add(v.toString());
		
		// Env currently only has tags from the environment.
		LinkedList<String> newFacts = new LinkedList<String>();
		LinkedList<String> newActions = new LinkedList<String>();
		
		// Reset all knowledge nodes.
		for (KnowledgeNode kn : KnowledgeBase) kn.reset();
		
		// Substitute values based on current position of ant.
		for (KnowledgeNode kn : KnowledgeBase) kn.substitute(subValues);
		
		String checkLeft = "(VISITED LDX LDY)";
		String checkRight = "(VISITED RDX RDY)";
		String checkForward = "(VISITED FX FY)";
		for (String subPair : subValues) {
			checkLeft = checkLeft.replace(subPair.split(" ")[0], subPair.split(" ")[1]);
			checkRight = checkRight.replace(subPair.split(" ")[0], subPair.split(" ")[1]);
			checkForward = checkForward.replace(subPair.split(" ")[0], subPair.split(" ")[1]);
		}
		
		// Add tags that are stored in the active section of the memory. Also check if the location we may move to have been visited yet.
		for (int j = 0; j < this.memory.getLimit(); j++) {
			LinkedList<KnowledgeNode> bucket = this.memory.getBucket(j);
			for (KnowledgeNode kn : bucket) {
				env.addAll(kn.getOutFacts());
				//System.out.println(kn);
			}
		}
		if (!env.contains(checkLeft)) env.add("(NOT " + checkLeft.substring(1));
		if (!env.contains(checkRight)) env.add("(NOT " + checkRight.substring(1));
		if (!env.contains(checkForward)) env.add("(NOT " + checkForward.substring(1));
		
		//System.out.println(env);
		
		// Decay every tag.
		memory.rotate();
		
		//memory.display();
		// Process knowledge nodes.
		LinkedList<KnowledgeNode> addToMem = new LinkedList<KnowledgeNode>();
		while (! env.isEmpty()) {
			String tag = env.remove();
			for (KnowledgeNode node : KnowledgeBase) {
				node.process(tag);
				if (node.isTriggered() == true) {
					addToMem.add(new KnowledgeNode(node));
					newFacts.addAll(node.getOutFacts());
					newActions.addAll(node.getOutActions());
					env.addAll(node.getOutFacts());
				}
			}
		}
		//memory.display();
		//System.out.println("Facts: " + newFacts);
		//System.out.println("Actions: " + newActions);
		
		// Choose a direction to move based on the valid options.
		double total = 0.0;
		double left = 0.0;
		double right = 0.0;
		double forward = 0.0;
		if (newFacts.contains(checkLeft)) {
			left += pheromones[1]; total += pheromones[1];
		} 
		if (newFacts.contains(checkRight)) {
			right += pheromones[2]; total += pheromones[2];
		}
		if (newFacts.contains(checkForward)) {
			forward += pheromones[0]; total += pheromones[0];
		}
		left = left/total;
		right = right/total;
		forward = forward/total;
		
		// Randomly choose one based on calculated probabilities.
		// Add the corresponding rule and any other rules to memory.
		Random rand = new Random();
		double unif = rand.nextFloat();
		if (unif < left) {
			// Move left.
			envVec.add("(#TURN LD)");
			envVec.add("(#WALK)");
			for (KnowledgeNode kn : addToMem) {
				LinkedList<String> outFacts = kn.getOutFacts();
				if (!outFacts.contains(checkRight) && !outFacts.contains(checkForward)) memory.insertStart(kn);
			}
		} else if (unif < left + right) {
			// Move right.
			envVec.add("(#TURN RD)");
			envVec.add("(#WALK)");
			for (KnowledgeNode kn : addToMem) {
				LinkedList<String> outFacts = kn.getOutFacts();
				if (!outFacts.contains(checkLeft) && !outFacts.contains(checkForward)) memory.insertStart(kn);
			}
		} else if (unif < left + right + forward) {
			// Move forward.
			envVec.add("(#WALK)");
			for (KnowledgeNode kn : addToMem) {
				LinkedList<String> outFacts = kn.getOutFacts();
				if (!outFacts.contains(checkRight) && !outFacts.contains(checkLeft)) memory.insertStart(kn);
			}
		}
		
		newFacts.clear();newActions.clear();
	}
	
	public Vector<Point2D> getPositions() {
		Vector<Point2D> positions = new Vector<Point2D>();
		for (int j = 0; j < this.memory.getLimit(); j++) {
			LinkedList<KnowledgeNode> bucket = this.memory.getBucket(j);
			for (KnowledgeNode kn : bucket) {
				LinkedList<String> facts = kn.getOutFacts();
				for (String f : facts) {
					if (f.contains("VISITED")) {
						
						Point2D p = new Point();
						p.setLocation(Double.parseDouble(f.split(" ")[1]), Double.parseDouble(f.split(" ")[2].replace(')', ' ')));
						positions.add(p);
					}
				}
			}
		}
		return positions;
	}
	
	public static void main(String args[]) {
		//Memory mem = new Memory("aco_knowledge_nodes.txt");
		LinkedList<String> substitutions = new LinkedList<String>();
		substitutions.add("FX 1");
		substitutions.add("FY 2");
		substitutions.add("RDX 3");
		substitutions.add("RDY 4");
		substitutions.add("LDX 5");
		substitutions.add("LDY 6");
		
		Vector vec = new Vector();
		vec.add("(CAN WALK F)");
		vec.add("(CAN WALK RD)");
		vec.add("(CAN WALK LD)");
		
		//mem.Process(vec, substitutions);
		System.out.println(vec);
		vec.clear();
		vec.add("(CAN WALK F)");
		vec.add("(CAN WALK RD)");
		vec.add("(CAN WALK LD)");
		
		//mem.Process(vec, substitutions);
		System.out.println(vec);
		vec.clear();
		vec.add("(CAN WALK F)");
		vec.add("(CAN WALK RD)");
		vec.add("(CAN WALK LD)");

		//mem.Process(vec, substitutions);
		System.out.println(vec);
		
		vec.clear();
		vec.add("(CAN WALK F)");
		vec.add("(CAN WALK RD)");
		vec.add("(CAN WALK LD)");

		//mem.Process(vec, substitutions);
		System.out.println(vec);
	}
}
