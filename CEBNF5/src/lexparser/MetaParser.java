package lexparser;

import general.ErrorHandler;

import java.util.HashMap;
import java.util.Stack;

import lexparser.TokenGenerator.TokenSpec.Token;

/**
 * Represents an instance of a parser of grammars and code
 * @author nikhil
 *
 */
public class MetaParser {
	
	/**
	 * A map of all terms in the grammar
	 */
	public HashMap<String, MetaNode> terms = new HashMap<>();
	
	/**
	 * The stack of maps of records. Each level of the stack represents a call of a defined term. The map
	 * contains recorded terms within that defined term.
	 */
	public Stack<HashMap<String, CodeNode>> recordStack = null;
	
	/**
	 * The token generator associated with this parser, provided by the user
	 */
	TokenGenerator generator;	
	
	/**
	 * Creates a new MetaParser with the given token generator
	 * @param generator The generator
	 */
	public MetaParser(TokenGenerator generator) {
		this.generator = generator;
	}
	
	/**
	 * Parses a grammar and creates tree representations of its structure
	 * @param input The string containing the grammar
	 */
	public void parseGrammar(String input) {
		new MetaNode(null, this).parseGrammar(input);
	}
	
	/**
	 * Parses code using the parsed grammar and creates tree representations of its structure
	 * @param input The string containing the code
	 * @return The tree representation of the code
	 */
	public CodeNode parseCode(String input) {
		Token[] tokens = generator.lexan(input);
		CodeNode master = new CodeNode();
		recordStack = new Stack<>();
		recordStack.push(new HashMap<String, CodeNode>());
		int used = terms.get("INPUT").parseCode(tokens, 0, master);
		recordStack = null;
		if(used < tokens.length) {
			ErrorHandler.error("Unknown error in code. Ignoring all code after line " + tokens[used].line(), false);
		}
		return master;
	}
}




/*
 * old interpreter code for calculator
 * 
 * 
 * TokenGenerator generator = new TokenGenerator();
		// register rules
		generator.registerStandardRules(new String[] {";", "\\+", "-", "\\*", "/"}, false, true);
		generator.registerRule("pow", "\\^", false, false);
		generator.registerRule("DECIMAL", false, true);
		generator.registerRule("blank", " ", true, false);
		MetaParser parser = new MetaParser(generator);
		parser.parseGrammar(ReadWrite.read("test grammar.txt"));
		String output = parser.parseCode("1^2*3+4;").toString();
		System.out.println("Postfix string: " + output);
		String[] postfix = output.split(",");
		Stack<Number> values = new Stack<>(); 
		for(String current : postfix) {
			switch(current) {
			case "n":
				int neg = values.pop().intValue();
				if(neg == 1) {
					values.push(-values.pop().doubleValue());
				}
				break;
			case "^":
				int pow = values.pop().intValue();
				for(int i = 0; i < pow; i++) {
					double power = values.pop().doubleValue();
					values.push(Math.pow(values.pop().doubleValue(), power));
				}
				break;
			case "*":
				values.push(values.pop().doubleValue() * values.pop().doubleValue());
				break;
			case "/":
				double den = values.pop().doubleValue();
				values.push(values.pop().doubleValue() / den);
				break;
			case "+":
				values.push(values.pop().doubleValue() + values.pop().doubleValue());
				break;
			case "-":
				double sub = values.pop().doubleValue();
				values.push(values.pop().doubleValue() - sub);
				break;
			case ";":
				System.out.println(values.pop());
				if(values.size() > 0) {
					System.err.println("incomplete pop error. may allow overpop error to pass later");
				}
				break;
			default:
				boolean decimal = false;
				for(char c : current.toCharArray()) {
					if(c == '.') {
						decimal = true;
					} else if(!Character.isDigit(c)) {
						System.err.println("Unrecognized symbol: " + current);
						return;
					}
				}
				if(decimal) {
					values.push(Double.parseDouble(current));
				} else {
					values.push(Integer.parseInt(current));
				}
			}
		}
*/