



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * SimpleParser reads a given file of rules and generates an object for each rule.
 * @author Michael Noseworthy
 */
public class SimpleParser {
	private String rulesFile = "";
	
	/**
	 * The constructor initializes the location of the rules file.
	 * @param file The file path in which to read the rules.
	 */
	public SimpleParser(String file) {
		this.rulesFile = file;
	}
	
	/**
	 * Generates a list of rules based from the given text file.
	 * @return rules A linked list of all the rules contained in the text file.
	 */
	public LinkedList<Rule> Parse() {
		LinkedList<Rule> rules = new LinkedList<Rule>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.rulesFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				rules.add(new Rule(line));
			}
		} catch(FileNotFoundException e) {
			System.out.println("File not found.");
		} catch(IOException e) {
			System.out.println("Failed IO operation.");
		}
		
		return rules;
	}
}
