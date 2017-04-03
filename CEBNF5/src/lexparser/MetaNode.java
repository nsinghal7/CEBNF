package lexparser;

import general.ErrorHandler;

import java.util.ArrayList;
import java.util.HashMap;

import lexparser.TokenGenerator.TokenSpec.Token;

/**
 * Represents a node in a grammar tree
 * @author nikhil
 *
 */
public class MetaNode {
	
	/**
	 * An identifying name or specific value of the node
	 */
	private String value;
	
	/**
	 * The name used on this node so that its print output can be captured. If non-null, shouldn't print
	 * to the postfix notation, but to a separate storage area
	 */
	private String recordName;
	
	/**
	 * The child nodes of this node
	 */
	private ArrayList<MetaNode> children;
	
	/**
	 * The parent node of this node. Set even before this node is added as the parent's child
	 */
	private MetaNode parent;
	
	/**
	 * The MetaParser associated with this tree so that terms can be stored and referenced
	 */
	private MetaParser parser;
	
	/**
	 * Creates a new MetaNode with the given parent and its parser, without adding it as a child
	 * @param parent The parent node. Cannot be null.
	 */
	public MetaNode(MetaNode parent) {
		this.parent = parent;
		this.parser = parent.parser;
		children = new ArrayList<>();
	}
	
	/**
	 * Creates a new MetaNode with the given parent and parser, without adding it as a child.
	 * @param parent The parent node. Can be null.
	 * @param parser The parser. Cannot be null
	 */
	public MetaNode(MetaNode parent, MetaParser parser) {
		this.parent = parent;
		this.parser = parser;
		children = new ArrayList<>();
	}
	
	/**
	 * Parses a postfix print code string in the grammar, and sets the value of the current node to that
	 * code preceded by a /
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	private int parseGrammarCodeString(String input, int start) {
		value = "/";
		boolean slash = false;
		while(start < input.length()) {
			char current = input.charAt(start);
			if(slash) {
				if(current == '\\') {
					value += "\\\\";
				} else if(current == 't') {
					value += '\t';
				} else if(current == 'n') {
					value += '\n';
				} else if(current == 'r') {
					value += '\r';
				} else if(current == 'f') {
					value += '\f';
				} else if(current == '\'') {
					value += '\'';
				} else if(current == '&') {
					value += "\\&";
				} else if(current == '#') {
					value += "\\#";
				} else {
					ErrorHandler.error("Unexpected code string character '" + current + "' at char " + start, false);
					return -1;
				}
				slash = false;
			} else {
				if(current == '\\') {
					slash = true;
				} else if(current == '/') {
					return start + 1;
				} else {
					value += current;
				}
			}
			start ++;
		}
		ErrorHandler.error("Unexpected end of file during code string", false);
		return -1;
	}
	
	/**
	 * Parses a single quote string in the grammar and sets the current node's value to that string,
	 * preceded by a "
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	private int parseGrammarSQString(String input, int start) {
		value = "\"";
		boolean slash = false;
		while(start < input.length()) {
			char current = input.charAt(start);
			if(slash) {
				if(current == '\\') {
					value += '\\';
				} else if(current == 't') {
					value += '\t';
				} else if(current == 'n') {
					value += '\n';
				} else if(current == 'r') {
					value += '\r';
				} else if(current == 'f') {
					value += '\f';
				} else if(current == '\'') {
					value += '\'';
				} else {
					ErrorHandler.error("Unexpected string character '" + current + "' at char " + start, false);
					return -1;
				}
				slash = false;
			} else {
				if(current == '\\') {
					slash = true;
				} else if(current == '\'') {
					return start + 1;
				} else {
					value += current;
				}
			}
			start ++;
		}
		ErrorHandler.error("Unexpected end of file during string", true);
		return -1;
	}
	
	/**
	 * Parses grammar groupings and assigns their values and children into the current node
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param next The character of the opening bracket
	 * @param canAdd True if the node should add itself to its parent node if parsing is successful
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	private int parseGrammarGroups(String input, int start, char next, boolean canAdd) {
		start = this.parseGrammarList(input, start, false, false);
		
		// get matching close bracket
		char close;
		if(next == '(') {
			close = ')';
		} else {
			close = (char) (next + 2);
		}
		start = Helper.parseWhiteSpaceAndComments(input, start);
		ErrorHandler.register("Expected ending bracket " + close + " at char " + start);
		start = Helper.parseConstant(input, start, Character.toString(close));
		if(start < 0) {
			ErrorHandler.commit(false);
			return -1;
		}
		ErrorHandler.cancel();
		this.value = Character.toString(next); // not just a list
		start = Helper.parseWhiteSpaceAndComments(input, start);
		parent.addChild(this, canAdd);
		return start;
	}
	
	/**
	 * Parses any grammar element of higher priority than |
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param canAdd True if the node should add itself to the parent node if parsing is successful
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	public int parseGrammarItems(String input, int start, boolean canAdd) {
		start = Helper.parseWhiteSpaceAndComments(input, start);
		char next = input.charAt(start++);
		if(next == '[' || next == '(' || next == '<' || next == '{') {
			start = this.parseGrammarGroups(input, start, next, false);
		} else if(next == '#' || next == '$') {
			// predef or token
			start = this.parseGrammarIdentifier(input, start, false);
			this.value = next + this.value;
			start = Helper.parseWhiteSpaceAndComments(input, start);
		} else if(next == '\'') {
			// sq string
			start = this.parseGrammarSQString(input, start);
			start = Helper.parseWhiteSpaceAndComments(input, start);
		} else if(next == '/') {
			// print group
			start = this.parseGrammarCodeString(input, start);
			start = Helper.parseWhiteSpaceAndComments(input, start);
		} else if (Character.isJavaIdentifierStart(next)) {
			start = this.parseGrammarIdentifier(input, start - 1, false);
			this.value = ":" + this.value;
			start = Helper.parseWhiteSpaceAndComments(input, start);
		} else {
			ErrorHandler.error("Unrecognized symbol for grammar item at char " + start, false);
			return -1;
		}
		// check for record name
		int temp = Helper.parseConstant(input, start, "@");
		if(temp < 0) {
			parent.addChild(this, canAdd);
			return start;
		}
		ErrorHandler.register("Expected identifier after record symbol @ at char " + start);
		start = this.parseGrammarIdentifier(input, temp, true);
		if(start < 0) {
			ErrorHandler.commit(false);
			return -1;
		}
		ErrorHandler.cancel();
		parent.addChild(this, canAdd);
		return Helper.parseWhiteSpaceAndComments(input, start);
	}
	
	/**
	 * Parses an | group or higher priority grammar elements
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param canAdd True if the node should add itself to the parent node if parsing is successful
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	public int parseGrammarWithOr(String input, int start, boolean canAdd) {
		while(true) {
			MetaNode temp = new MetaNode(this);
			ErrorHandler.register("Expected group or identifier while parsing or_list at char " + start);
			start = temp.parseGrammarItems(input, start, true);
			if(start < 0) {
				ErrorHandler.commit(false);
				return -1;
			}
			ErrorHandler.cancel();
			start = Helper.parseWhiteSpaceAndComments(input, start);
			int next = Helper.parseConstant(input, start, "|");
			if(next < 0) {
				if(children.size() == 1) {
					children.get(0).copyInto(this);
				} else {
					this.value = "|";
				}
				parent.addChild(this, canAdd);
				return start;
			} else {
				start = Helper.parseWhiteSpaceAndComments(input, next);
			}
		}
	}
	
	/**
	 * Parses a grammar list or higher priority elements
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param compress True if should replace itself with its child if there is only one
	 * @param canAdd True if the node should add itself to the parent node if parsing is successful
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	public int parseGrammarList(String input, int start, boolean compress, boolean canAdd) {
		while(true) {
			MetaNode temp = new MetaNode(this);
			ErrorHandler.register("Expected group or identifier while parsing grammar list at char " + start);
			start = temp.parseGrammarWithOr(input, start, true);
			if(start < 0) {
				ErrorHandler.commit(false);
				return -1;
			}
			ErrorHandler.cancel();
			start = Helper.parseWhiteSpaceAndComments(input, start);
			int next = Helper.parseConstant(input, start, ",");
			if(next < 0) {
				if(children.size() == 1 && compress) {
					children.get(0).copyInto(this);
				} else {
					this.value = "("; // same as paren group
				}
				parent.addChild(this, canAdd);
				return start;
			} else {
				start = Helper.parseWhiteSpaceAndComments(input, next);
			}
		}
	}
	
	/**
	 * Parses a term definition in the grammar
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param canAdd True if the node should add itself to the parent node if parsing is successful
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	public int parseGrammarLine(String input, int start, boolean canAdd) {
		start = Helper.parseWhiteSpaceAndComments(input, start);
		ErrorHandler.register("No identifier in grammar on start of line at char " + start);
		start = this.parseGrammarIdentifier(input, start, false);
		if(start < 0) {
			ErrorHandler.commit(false);
			return -1;
		}
		ErrorHandler.cancel();
		String temp = this.value;
		start = Helper.parseWhiteSpaceAndComments(input, start);
		ErrorHandler.register("Expected = in grammar at char " + start);
		start = Helper.parseConstant(input, start, "=");
		if(start < 0) {
			ErrorHandler.commit(false);
			return -1;
		}
		ErrorHandler.cancel();
		start = this.parseGrammarList(input, start, false, false);
		if(start < 0) {
			return -1;
		}
		ErrorHandler.register("Expected ; in grammar at char " + start);
		start = Helper.parseConstant(input, start, ";");
		if(start < 0) {
			ErrorHandler.commit(false);
			return -1;
		}
		ErrorHandler.cancel();
		this.value = temp;
		parser.terms.put(this.value, this);
		parent.addChild(this, canAdd);
		return Helper.parseWhiteSpaceAndComments(input, start);
	}
	
	/**
	 * Parses an identifier used in the grammar
	 * @param input The grammar string
	 * @param start The location to start parsing in the string
	 * @param record True if the parsed string should be added to the record field, false if the value
	 * field
	 * @return The character to start parsing at after this, or -1 if failed
	 */
	public int parseGrammarIdentifier(String input, int start, boolean record) {
		String id = "";
		char current = input.charAt(start);
		if(Character.isJavaIdentifierStart(current)) {
			id += current;
		} else {
			return -1;
		}
		for(++start; start < input.length(); start++) {
			current = input.charAt(start);
			if(Character.isJavaIdentifierPart(current)) {
				id += current;
			} else {
				break;
			}
		}
		if(record) {
			this.recordName = id;
		} else {
			this.value = id;
		}
		return start;
	}
	
	/**
	 * Parses an entire grammar string
	 * @param input The grammar string
	 * @return This node if successful, otherwise null
	 */
	public MetaNode parseGrammar(String input) {
		int current = 0;
		while(current < input.length()) {
			ErrorHandler.register("Unknown error in grammar");
			current = new MetaNode(this).parseGrammarLine(input, current, true);
			if(current < 0) {
				ErrorHandler.commit(true);
				return null;
			}
			ErrorHandler.cancel();
		}
		return this;
	}
	
	/**
	 * Copies all data of this node into the given node except the parent
	 * @param out The node to copy into
	 */
	public void copyInto(MetaNode out) {
		out.value = value;
		out.recordName = recordName;
		out.children = children;
	}
	
	/**
	 * Add a child to this node if the child isn't already in the list and canAdd is true
	 * @param child The child to add
	 * @param canAdd True if should actually add
	 */
	public void addChild(MetaNode child, boolean canAdd) {
		if((children.size() == 0 || children.get(children.size() - 1) != child) && canAdd) {
			children.add(child);
		}
	}
	
	/**
	 * Gets the string representation of this node and its children
	 */
	public String toString(int tabs) {
		String pre = "";
		for(int i = 0; i < tabs; i++) {
			pre += "\t";
		}
		String ans  = pre + "'" + value + "'" + (recordName == null ? "" : ("@" + recordName));
		
		if (children.size() > 0) {
			ans += "{\n";
			for (int i = 0; i < children.size() - 1; i++) {
				ans += children.get(i).toString(tabs + 1) + ",\n";
			}
			ans = ans + children.get(children.size() - 1).toString(tabs + 1) + "}";
		}
		return ans;
	}
	
	/**
	 * Prints the string with tabs to represent children.
	 */
	public String toString() {
		return toString(0);
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Parses code using this node's information, and creates a CodeNode tree to represent it
	 * @param input The code string to parse
	 * @param start The location at which to start parsing
	 * @param parent The parend CodeNode to add any generated CodeNodes to
	 * @return The location to start further parsing at
	 */
	public int parseCode(Token[] input, int start, CodeNode parent) {
		char firstChar = value.charAt(0);
		int ans;
		if(Character.isJavaIdentifierStart(firstChar) || firstChar == '(') {
			// parse as single pass list
			ans = parseCodeSinglePassList(input, start, parent, null);
		} else if(firstChar == ':') {
			// parse linked term
			parser.recordStack.push(new HashMap<String, CodeNode>());
			ans = parser.terms.get(value.substring(1)).parseCodeSinglePassList(input, start, parent, recordName);
			parser.recordStack.pop();
		} else if(firstChar == '$' || firstChar == '#') {
			// parse linked custom token or builtin term
			if(start >= input.length) {
				ans = -1;
			} else {
				ans = input[start].isOfType(value.substring(1)) ? start + 1 : -1;
				if(ans >= 0) {
					new CodeNode(input[start].value(), input[start].print()).recordSelf(recordName, parser).addToParent(parent);
				}
			}
		} else if(firstChar == '"') {
			// parse constant (must be a single token)
			if(start >= input.length) {
				ans = -1;
			} else {
				ans = input[start].value().equals(value.substring(1)) ? start + 1 : -1;
				if(ans >= 0) {
					new CodeNode(input[start].value(), input[start].print()).recordSelf(recordName, parser).addToParent(parent);
				}
			}
		} else if(firstChar == '|') {
			// parse Or group
			ans = parseCodeOrGroup(input, start, parent);
		} else if(firstChar == '[') {
			// parse as optional
			ans = parseCodeOptionalGroup(input, start, parent);
		} else if(firstChar == '{' || firstChar == '<') {
			// parse as loop group
			ans = parseCodeLoopGroup(input, start, parent, firstChar == '<');
		} else if(firstChar == '/') {
			// add code group. Doesn't parse anything, and can't fail
			parseCodeCodeGroup(parent);
			ans = start;
		} else {
			ErrorHandler.error("Internal Error: Unknown MetaNode Value '" + firstChar + "'. I messed up", true);
			return -1;
		}
		
		if(ans < 0) {
			// assume already handled adding self to parent or not
			return -1;
		}
		
		// return success
		return ans;
	}
	
	
	/**
	 * Parse a single pass list of code where the structure is defined by this metanode and the parseable
	 * data is stored in input
	 * @param input The input to parse
	 * @param start The location in the input list to start parsing at
	 * @param parent The parent codenode to add to if successful in parsing
	 * @param superRecordName The record name of a defined term controlling this list. Should be updated if not
	 * null
	 * @return The location to start at after parsing, or -1 if failed.
	 */
	private int parseCodeSinglePassList(Token[] input, int start, CodeNode parent, String superRecordName) {
		CodeNode self = new CodeNode(recordName == null);
		for(MetaNode child : children) {
			start = child.parseCode(input, start, self);
			if(start < 0) {
				return -1;
			}
		}
		self.recordSelf(recordName, parser).addToParent(parent);
		if(superRecordName != null) {
			parser.recordStack.get(parser.recordStack.size() - 2).put(superRecordName, self);
		}
		return start;
	}
	
	/**
	 * Parse an or group using this node's information
	 * @param input The tokens to parse
	 * @param start The location in the token list to start parsing at
	 * @param parent The parent codenode to add to if successful
	 * @return The index to start at after parsing, or -1 if successful.
	 */
	private int parseCodeOrGroup(Token[] input, int start, CodeNode parent) {
		// cannot have a record name
		for(MetaNode child : children) {
			int ans = child.parseCode(input, start, parent);
			if(ans >= 0) {
				return ans;
			}
		}
		return -1;
	}
	
	/**
	 * Parses code as an optional group using information from this node
	 * @param input The input tokens
	 * @param start The location in the token list to start parsing at
	 * @param parent The codenode to add to if successful
	 * @return The location to start at after parsing, or -1 if failed
	 */
	private int parseCodeOptionalGroup(Token[] input, int start, CodeNode parent) {
		final int savedStart = start;
		CodeNode self = new CodeNode(recordName == null);
		for(MetaNode child : children) {
			start = child.parseCode(input, start, parent);
			if(start < 0) {
				CodeNode.BLANK.recordSelf(recordName, parser);
				return savedStart;
			}
		}
		self.recordSelf(recordName, parser).addToParent(parent);
		return start;
	}
	
	/**
	 * Parse a loop using the information in this node
	 * @param input The input list to parse
	 * @param start The location to start at
	 * @param parent The codenode to add to if successful
	 * @param canBeEmpty True if the list can be empty, false if requires at least 1 element
	 * @return The location to start at after parsing, or -1 if failed.
	 */
	private int parseCodeLoopGroup(Token[] input, int start, CodeNode parent, boolean canBeEmpty) {
		CodeNode loop = new CodeNode(recordName == null);
		while(true) {
			int current = start;
			CodeNode pass = new CodeNode(true);
			boolean failed = false;
			for(MetaNode child : children) {
				current = child.parseCode(input, current, pass);
				if(current < 0) {
					failed = true;
					break;
				}
			}
			if(!failed) {
				start = current;
				pass.addToParent(loop);
			} else {
				break;
			}
		}
		if(loop.children() == 0 && !canBeEmpty) {
			return -1;
		} else {
			loop.recordSelf(recordName, parser).addToParent(parent);
			return start;
		}
	}
	
	/**
	 * Adds a code group to the parent output
	 * @param parent The parent to add to
	 */
	private void parseCodeCodeGroup(CodeNode parent) {
		boolean slash = false;
		boolean reference = false;
		boolean referenceVal = false;
		String answer = "";
		String buffer = "";
		for(int i = 1; i < value.length(); i++) {
			char current = value.charAt(i);
			if(reference) {
				if(Character.isJavaIdentifierPart(current)) {
					buffer += current;
				} else {
					if(referenceVal) {
						answer += parser.recordStack.peek().get(buffer).forcePrint();
					} else {
						answer += Integer.toString(parser.recordStack.peek().get(buffer).children());
					}
					reference = false;
					buffer = "";
				}
			} else if(slash) {
				if(current == '\\') {
					buffer += "\\";
				} else if(current == '&' || current == '#') {
					buffer += current;
				} else {
					ErrorHandler.error("Invalid escape sequence passed initial code group compile. " 
													+ "Check with developer.", true);
					return;
				}
				slash = false;
			} else {
				if(current == '\\') {
					slash = true;
				} else if(current == '&' || current == '#') {
					answer += buffer;
					reference = true;
					referenceVal = current == '&';
					buffer = "";
				} else {
					buffer += current;
				}
			}
		}
		if(slash) {
			ErrorHandler.error("Unfinished escape sequence passed initial code group compile. "
												+ "Check with developer.", true);
			return;
		}
		if(buffer.length() != 0) {
			if(reference) {
				if(referenceVal) {
					answer += parser.recordStack.peek().get(buffer).forcePrint();
				} else {
					answer += Integer.toString(parser.recordStack.peek().get(buffer).children());
				}
			} else {
				answer += buffer;
			}
		}
		new CodeNode(answer, recordName == null).recordSelf(recordName, parser).addToParent(parent);
	}
}
