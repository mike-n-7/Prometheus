


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class OldExpertSystem {
	public static void main(String[] args) {
		SimpleParser parser = new SimpleParser("rules.txt");
		LinkedList<Rule> rules = parser.Parse();
		LinkedList<String> facts = new LinkedList<String>();
		LinkedList<Integer> executed_rules = new LinkedList<Integer>();
		
		try {
			BufferedReader facts_file = new BufferedReader(new FileReader("demo_facts1.txt"));
			String line = null;
			while ((line = facts_file.readLine()) != null) {
				facts.add(line);
			}
		} catch(FileNotFoundException e) {
			System.out.println("File not found.");
		} catch(IOException e) {
			System.out.println("Failed IO operation.");
		}
		
		boolean fact_executed = true;
		while (fact_executed) {
			fact_executed = false;
			for (Rule rule : rules) {
				if (!executed_rules.contains(rule.id) && rule.Evaluate(facts) ) {
					fact_executed = true;
					executed_rules.add(rule.id);
				}
			}
		}
			
		for (int id : executed_rules) {
			for (Rule r : rules) {
				if(r.id == id) {
					System.out.println("Applied rule: " + r.DisplayRule());
				}
			}
		}
		
		for (String fact : facts) {
			System.out.println(fact);
		}
	}
}
