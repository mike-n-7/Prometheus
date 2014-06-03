
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Knowledge node is a class that tries to completely merge rules and facts.
 * @author Michael Noseworthy
 *
 */
public class KnowledgeNode {
	public Hashtable<String, Boolean> inTagsOrig;
	public Hashtable<String, Boolean> inTags;
	public LinkedList<String> outTagsOrig;
	public LinkedList<String> outTags;
	public int active, threshold;
	public boolean used;
	
	public KnowledgeNode(LinkedList<String> input, LinkedList<String> output) {
		inTagsOrig = new Hashtable<String, Boolean>();
		inTags = new Hashtable<String, Boolean>();
		outTagsOrig = new LinkedList<String>();
		outTags = new LinkedList<String>();
		// Initialize the input where each value is set to false.
		for (String i : input) inTagsOrig.put(i.trim(), false);
		for (String i : input) inTags.put(i.trim(), false);
		// Initialize the output by copying the values from output.
		for (String o : output) outTagsOrig.add(o.trim());
		for (String o : output) outTags.add(o.trim());
		// Set active and threshold.
		active = 0;
		threshold = 1;
		used = false;
	}
	
	public KnowledgeNode(KnowledgeNode node) {
		inTagsOrig = new Hashtable<String, Boolean>();
		inTags = new Hashtable<String, Boolean>();
		outTagsOrig = new LinkedList<String>();
		outTags = new LinkedList<String>();
		// Initialize the input where each value is set to false.
		Enumeration<String> keys = node.inTagsOrig.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			inTagsOrig.put(k, false);
		}
		
		keys = node.inTags.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			inTags.put(k, false);
		}
		
		// Initialize the output by copying the values from output.
		for (String o : node.outTagsOrig) outTagsOrig.add(o.trim());
		for (String o : node.outTags) outTags.add(o.trim());
		// Set active and threshold.
		active = node.active;
		threshold = node.threshold;
		used = node.used;
	}
	/**
	 * Resets all the inTags to false.
	 */
	public void reset() {
		inTags = new Hashtable<String, Boolean>();
		Enumeration<String> keys = inTagsOrig.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			inTags.put(k, false);
		}
		
		outTags = new LinkedList<String>();
		for (String o : outTagsOrig) outTags.add(o);
		
		used = false;
	}
	
	// Go through the rules replacing any variables with their actual values.
	public void substitute(LinkedList<String> subs) {		
		Enumeration<String> keys = inTags.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			String newKey = k;
			for (String subPair : subs) {
				newKey = newKey.replace(subPair.split(" ")[0], subPair.split(" ")[1]);
			}
			inTags.remove(k);
			inTags.put(newKey, false);
			//System.out.println(newKey + " " + k);
		}
		
		Queue<String> removeTags = new LinkedList<String>();
		Stack<String> addTags = new Stack<String>();
		for (String o : outTags)  {
			String newTag = o;
			for (String subPair : subs) {
				newTag = newTag.replace(subPair.split(" ")[0], subPair.split(" ")[1]);
			}
			addTags.push(newTag);
			removeTags.add(o);
		}

		while (addTags.empty() == false) outTags.add(addTags.pop());
		for (String o : removeTags) outTags.remove(o);
		
		//System.out.println(this.toString());
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
				if (o.contains("#")) newActions.add(o);
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
		boolean factsTrue = true;
		Enumeration<String> keys = inTags.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			if (inTags.get(k) == false) factsTrue = false;
		}
		if (factsTrue) active++;
		if (active >= threshold && factsTrue == true && used == false) {
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
		if (inTags.containsKey(fact)) inTags.put(fact, true);
	}
	
	public String toString() {
		String ret = "";
		Enumeration<String> keys = inTags.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			ret += k + " ";
		}
		ret += " -> ";
		for (String o : outTags) {
			ret += o + " ";
		}
		return ret;
	}
	
}
