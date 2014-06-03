
import java.util.LinkedList;
import java.util.Stack;
/**
 * A class that models a rule in the expert system.
 * A rule has the format IF * THEN **
 * Where * is either:
 * 	1) (AND f1 f2)
 * 	2) (NOT f1)
 * Note f* can be nested, ex: (AND (AND f1 f2) (NOT f3)) => (f1 AND f2) AND (NOT f3)
 * Where ** is either:
 * 	1) {FACT} - Where FACT is to be added to the fact list if * is true.
 * 	2) [CMD] - Where CMD is the name of a function to be executed if * is true.
 * 
 * @author Michael Noseworthy
 *
 */
public class Rule {
	private static int num_rules = 0;
	public int id;
	private String rule;
	
	private enum ConclusionType {FACT, COMMAND};
	
	/**
	 * Constructor that initializes the rule and gives it a unique id.
	 * @param r The rule which to represent.
	 */
	public Rule(String r) {
		this.rule = r;
		this.id = num_rules;
		num_rules++;
	}
	
	/**
	 * Generate a user friendly version of the rule.
	 * @return ret A user friendly String of the represented rule.
	 */
	public String DisplayRule() {
		String ret = rule;
		//TODO: Return a string representing the rule in a more user friendly format.
		return ret;
	}
	
	/**
	 * Based on a given facts list, Evaluate will check to see if the rule applies and if so
	 * apply the rule by adding new facts to the fact list or executing a command.
	 * @param facts Facts is a LinkedList of the facts that are currently known.
	 * @return execute True if the rule was executed, false otherwise.
	 */
	public boolean Evaluate(LinkedList<String> facts) {
		boolean execute = true;
		
		LinkedList<String> hypothesis = ExtractHypothesis();
			
		for (String condition : hypothesis) {	
			// First we check to see if we can evaluate any sub-expressions.
			if (!facts.contains(condition)) execute = false;
		}
		
		// If the hypothesis is true, execute the condition.
		if (execute) {
			String conclusion = ExtractConclusion();
			if (GetConclusionType() == ConclusionType.FACT) {
				facts.add(conclusion);
			} else {
				facts.add(conclusion);
			}
		}
		
		return execute;
	}
	
	/**
	 * Cleans up the rule and extracts the hypothesis that can be evaluated by using two stacks.
	 * @return result A string representing only the conditional part of the rule.
	 */
	private LinkedList<String> ExtractHypothesis() {
		LinkedList<String> result = new LinkedList<String>();
		String conditions = new String(this.rule.split("THEN")[0]);
		conditions = conditions.replace("IF", "");
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
				result.push(cond);
				cond = "";
			} else {
				if (add == true) cond += c;
			}
		}		
		
		return result;
	}
	
	/**
	 * Cleans up the rule and extracts the conclusion in which will will evaluate.
	 * @return The conclusion.
	 */
	private String ExtractConclusion() {
		String result = new String(this.rule.split("THEN")[1]);
		return result.trim();
	}
	
	/**
	 * Decides if the applied rule should add a fact to the fact list or execute a command.
	 * @return ConclusionType.FACT if the rule applies a fact and ConclusionType.COMMAND otherwise.
	 */
	ConclusionType GetConclusionType() {
		if (this.rule.contains("#")) return ConclusionType.COMMAND;
		else return ConclusionType.FACT;
	}
}