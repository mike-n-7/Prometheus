
import java.util.Enumeration;
import java.util.LinkedList;


public class OldKnowledgeNode {
	public LinkedList<String> outTags;
	public int active, threshold;
	public boolean used;
	public String inTag;
	public String inTagOrig;
	
	public OldKnowledgeNode(String input, String[] output) {
		outTags = new LinkedList<String>();
		active = 0; threshold = 1;
		used = false;
		inTag = input;
		for (String o : output) outTags.add(o);
	}
	
	void reset() {
		used = false;
	}
	
	/**
	 * Check if the knowledge node has been triggered, if so, return it's output.
	 * @return A LinkedList of the outTags only if active >= threshold, otherwise null.
	 */
	 public LinkedList<String> getOutFacts() {
		if (active >= threshold) {
			LinkedList<String> newFacts = new LinkedList<String>();
			for (String o : outTags) {
				if (!o.contains("#")) newFacts.add(o);
			}
			return newFacts;
		} else {
			return null;
		}
	}
	 
	 public LinkedList<String> getOutActions() {
		if (active >= threshold) {
			LinkedList<String> newActions = new LinkedList<String>();
			for (String o : outTags) {
				if (o.contains("#")) newActions.add(o.substring(1));
			}
			return newActions;
		} else {
			return null;
		}
	}
	
	/**
	 * Check if the fact has been triggered. That is active has become >= threshold.
	 * @return
	 */
	public boolean isTriggered() {
		if (active >= threshold && used == false) {
			used = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Check to see which inTags are known.
	 * @param facts A list of facts currently known to the agent.
	 */
	public void process(String fact) {
		active++;
	}
}
