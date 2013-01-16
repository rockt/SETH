package edu.uchsc.ccp.nlp.ei.mutation;
import org.apache.oro.text.regex.Pattern;

/**
 * This class contains an apache Pattern object and a pattern Id
 * Especially useful to enable later which regex found which entity 
 */
public class MyPattern {
	private Pattern pattern;	//The jakarta oro-pattern
	private String regex;		//The pattern used to build the pattern 
	private int id;			//The identifier (line count) of the pattern 
	
	public MyPattern(Pattern pattern, String regex, int id) {
		super();
		this.pattern = pattern;
		this.regex = regex;
		this.id = id;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public int getId() {
		return id;
	}

	public String getRegex() {
		return regex;
	}
}
