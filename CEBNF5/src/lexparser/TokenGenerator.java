package lexparser;

import general.ErrorHandler;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Allows the user to input order of token checking and rules about token ignoring and printing.
 * Then, breaks input text into tokens.
 */
public class TokenGenerator {
	
	/**
	 * The list of rules, in order or priority and input
	 */
	private LinkedList<TokenSpec> rules = new LinkedList<>();
	
	/**
	 * Registers rules with all the same parameters
	 * @param regexs A list of regular expressions to register as rules. Also considered the names
	 * @param ignore True if all should be ignored after tokenization (aka token will not be created, but
	 * value will be parsed through)
	 * @param print True if the value should print when added to parent nodes, false if it should be
	 * represented by blanks
	 */
	public void registerStandardRules(String[] regexs, boolean ignore, boolean print) {
		for(String regex : regexs) {
			registerRule(regex, regex, ignore, print);
		}
	}
	
	
	/**
	 * Registers a rule in the rule list as the next priority rule. This rule is a custom definition
	 * @param name The name of the token type to be created
	 * @param regex The regular expression to match the token
	 * @param ignore True if the token type represents non-coding symbols, and shouldn't make tokens
	 * @param print True if the token should print to the postfix representation
	 */
	public void registerRule(String name, String regex, boolean ignore, boolean print) {
		rules.add(new TokenSpec(name, regex, ignore, print));
	}
	
	/**
	 * Registers a rule in the rule list as the next priority rule. This rule matches a builtin definition
	 * @param builtin The builtin definition name (including the #)
	 * @param ignore True if the token type represents non-coding symbols, and shouldn't make tokens
	 * @param print True if the token should print to the postfix representation
	 */
	public void registerRule(String builtin, boolean ignore, boolean print) {
		rules.add(new TokenSpec(builtin, ignore, print));
	}
	
	/**
	 * Parses the input string to break it into tokens based on the registered rules
	 * @param input The string to parse
	 * @return An array of tokens representing the significant portions of the input
	 */
	public TokenSpec.Token[] lexan(String input) {
		int start = 0;
		int line = 1;
		LinkedList<TokenSpec.Token> ans = new LinkedList<>();
		while(start < input.length()) {
			boolean success = false;
			for(TokenSpec rule : rules) {
				Matcher match = rule.regex.matcher(input);
				if(match.find(start) && match.start() == start) { // POTENTIAL ERROR maybe should match substring, not start in middle
					success = true;
					if(!rule.ignore) {
						ans.add(rule.new Token(input.substring(start, match.end()), line));
					}
					line += lineInc(input, start, match.end());
					start = match.end();
					break;
				}
			}
			if(!success) {
				ErrorHandler.error("Unrecognized token '" + input.charAt(start) +"' on line " + line, true);
			}
		}
		
		return ans.toArray(new TokenSpec.Token[ans.size()]);
	}
	
	/**
	 * Counts the number of line breaks in a given stretch in order to increment the line counter
	 * @param input The input string to look in
	 * @param start The start of the segment
	 * @param end The end of the segment
	 */
	public static int lineInc(String input, int start, int end) {
		int count = 0;
		for(int i = start; i < end; i++) {
			if(input.charAt(i) == '\n') {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Holds information about a type of token
	 */
	public class TokenSpec {
		/**
		 * The token type's name
		 */
		public String name;
		
		/**
		 * The regular expression that matches and identifies the token
		 */
		public Pattern regex;
		
		/**
		 * If the token should be ignored
		 */
		public boolean ignore;
		
		/**
		 * If the token should print to postfix
		 */
		public boolean print;
		
		
		/**
		 * Creates a new TokenSpec for a custom definition
		 */
		public TokenSpec(String name, String regex, boolean ignore, boolean print) {
			this.name = name;
			this.regex = Pattern.compile(regex);
			this.ignore = ignore;
			this.print = print;
		}
		
		/**
		 * Creates a new TokenSpec for a builtin definition
		 */
		public TokenSpec(String builtin, boolean ignore, boolean print) {
			name = builtin;
			this.ignore = ignore;
			this.print = print;
			String regex = "";
			if(builtin == "INTEGER") {
				regex = "[0-9_]+";
			} else if(builtin == "DECIMAL") {
				regex = "([0-9_]+(\\.[0-9_]*)?)|(\\.[0-9_]+)";
			} else if(builtin == "DIGIT") {
				regex = "[0-9]";
			} else if(builtin == "SQ_STRING") {
				regex = "'((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\')|[^\\\\'])*'";
			} else if(builtin == "DQ_STRING") {
				regex = "\"((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\\")|[^\\\\\"])*\"";
			} else if(builtin == "STRING") {
				regex = "('((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\')|[^\\\\'])*')|(\"((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\\")|[^\\\\\"])*\")";
			} else if(builtin == "SQ_CHAR_STRING") {
				regex = "'((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\')|[^\\\\'])'";
			} else if(builtin == "DQ_CHAR_STRING") {
				regex = "\"((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\\")|[^\\\\\"])\"";
			} else if(builtin == "CHAR_STRING") {
				regex = "('((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\')|[^\\\\'])')|(\"((\\\\\\\\)|(\\\\t)|(\\\\n)|(\\\\r)|(\\\\f)|(\\\\\")|[^\\\\\"])\")";
			} else if(builtin == "LC_LETTER") {
				regex = "[a-z]";
			} else if(builtin == "UC_LETTER") {
				regex = "[A-Z]";
			} else if(builtin == "LETTER") {
				regex = "[a-zA-Z]";
			} else if(builtin == "ANY_CHAR") {
				regex = ".";
			} else if(builtin == "PRINTING_CHAR") {
				regex = "\\S";
			} else if(builtin == "WHITE_SPACE") {
				regex = "\\s";
			} else if(builtin == "LINE_COMMENT") {
				regex = "//[^\\n]\\n";
			} else if(builtin == "BLOCK_COMMENT") {
				regex = "/\\*((\\*[^/])|[^\\*])*\\*/";
			} else {
				ErrorHandler.error("Unknown Builtin term " + builtin, true);
			}
			
			this.regex = Pattern.compile(regex);
		}
		
		/**
		 * Represents a single instance of a token and holds relevant information about it.
		 * Contains references to its containing TokenSpec's variables so it can determine its type
		 * @author nikhil
		 *
		 */
		public class Token {
			
			/**
			 * The string value of the segment of code the token represents
			 */
			private final String value;
			
			/**
			 * The line on which the token occurs
			 */
			private final int line;
			
			/**
			 * Creates a new token
			 * @param value The value of the token
			 * @param line The line of the token
			 */
			public Token(String value, int line) {
				this.value = value;
				this.line = line;
			}
			
			/**
			 * Gets the token value
			 * @return The value
			 */
			public String value() {
				return value;
			}
			
			/**
			 * Gets the token line
			 * @return The line
			 */
			public int line() {
				return line;
			}
			
			/**
			 * Gets whether the token should print
			 * @return True/false
			 */
			public boolean print() {
				return print;
			}
			
			/**
			 * Gets the token type name
			 * @return The name of the TokenSpec
			 */
			public String name() {
				return name;
			}
			
			/**
			 * Checks if the token has a type given by the name inputted
			 * @param type The name of the type
			 * @return True if the name matches this token's type
			 */
			public boolean isOfType(String type) {
				return type.equals(name);
			}
		}
	}
}
