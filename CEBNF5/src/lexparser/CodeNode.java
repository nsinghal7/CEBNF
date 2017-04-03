package lexparser;

import general.ErrorHandler;


/**
 * CodeNodes represent a node in the parse tree after language code has been parsed. As this tree will only
 * ever be traversed in postfix order or its last element read from (and without removal), the datastructure
 * concatenates the values into a single comma separated list with additional values to keep track of properties.
 * 
 * @author nikhil
 *
 */
public class CodeNode {
	
	/**
	 * A blank codenode representing, perhaps, an optional group that wasn't matched.
	 */
	public static final CodeNode BLANK = new CodeNode();
	
	/**
	 * The value of the tree inside codenode.
	 */
	private String value;
	
	/**
	 * The number of children in codenode. -1 means that the node is forced to be a leaf.
	 */
	private int children;
	
	/**
	 * True if the value of this codenode should be added to its parent, false if it should be represented
	 * by a blank string (data thrown out). This may be ignored by forcePrint.
	 */
	private final boolean print;
	
	/**
	 * Escapes commas and backslashes in the data so that there is no confusion
	 * @param in The string to escape
	 * @return The escaped string
	 */
	public static String escapeCommas(String in) {
		return in.replace("\\", "\\\\").replace(",", "\\,");
	}
	
	/**
	 * Creates a new leaf codenode with the given string value, and print value. 
	 * @param value
	 * @param print
	 */
	public CodeNode(String value, boolean print) {
		this.value = escapeCommas(value);
		this.print = print;
		children = -1;
	}
	
	/**
	 * Creates a leaf codenode with the given string value that prints
	 * @param value
	 */
	public CodeNode(String value) {
		this.value = escapeCommas(value);
		children = -1;
		print = true;
	}
	
	/**
	 * Creates a new blank non-leaf codenode
	 */
	public CodeNode() {
		value = "";
		children = 0;
		print = true;
	}
	
	/**
	 * Creates a new blank non-leaf codenode with the given print value
	 * @param print
	 */
	public CodeNode(boolean print) {
		value = "";
		children = 0;
		this.print = print;
	}
	
	/**
	 * Gets the number of children
	 * @return children.
	 */
	public int children() {
		return children;
	}
	
	/**
	 * Adds a child to this code node. children cannot be added to leaves.
	 * @param in The child to add in
	 */
	public void addChild(CodeNode in) {
		if(children == -1) {
			ErrorHandler.error("Attempted to add child '" + in + "' to value codenode '" + this + "'", true);
		} else if(in.toString() != null && in.value.length() > 0) {
			value += (children == 0 ? "" : ",") + in.toString();
			children++;
		} else if(in.value != null && in.value.length() > 0) {
		}
	}
	
	/**
	 * Add this node to the input node
	 * @param parent
	 */
	public void addToParent(CodeNode parent) {
		parent.addChild(this);
	}
	
	/**
	 * Records this node's value in the input parser's map so that it can be used in @ statements
	 * @param recordName The name to record with this codenode
	 * @param parser The parser controlling this program
	 * @return This codenode
	 */
	public CodeNode recordSelf(String recordName, MetaParser parser) {
		if(recordName != null) {
			parser.recordStack.peek().put(recordName, this);
		}
		return this;
	}
	
	/**
	 * return this codenode's value if it is supposed to print. Can return null.
	 */
	public String toString() {
		return print && value != null ? value : null;
	}
	
	/**
	 * Return this codenode's value, where null is replaced by a blank string
	 * @return
	 */
	public String forcePrint() {
		return value == null ? "" : value;
	}
}
