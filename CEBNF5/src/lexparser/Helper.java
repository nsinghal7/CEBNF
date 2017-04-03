package lexparser;

/**
 * A class containing helper parsing methods
 * @author nikhil
 *
 */
public class Helper {
	
	/**
	 * Parses whitespace and java style comments, starting at the given index of the given string.
	 * @param input The string to parse
	 * @param start The index to start at
	 * @return The index of the first non-white-space-or-comments character at or after start.
	 */
	public static int parseWhiteSpaceAndComments(String input, int start) {
		int guaranteed = start;
		boolean blockComment = false;
		boolean lineComment = false;
		boolean star = false;
		boolean slash = false;
		while(start < input.length()) {
			char current = input.charAt(start);
			if(blockComment) {
				if(star && current == '/') {
					blockComment = false;
				} else if(current == '*') {
					star = true;
				} else {
					star = false;
				}
			} else if(lineComment) {
				if(current == '\n') {
					lineComment = false;
				}
			} else {
				if(slash && current == '*') {
					blockComment = true;
					slash = false;
				} else if(slash && current == '/') {
					lineComment = true;
					slash = false;
				} else if(current == '/') {
					slash = true;
				} else if(slash) {
					return guaranteed;
				} else if(!Character.isWhitespace(current)) {
					return start;
				} else {
					guaranteed = start + 1;
				}
			}
			start++;
		}
		return start;
	}
	
	/**
	 * Identifies whitespace starting at start
	 * @param input The string to parse
	 * @param start The starting index.
	 * @return The first non-white-space index after start
	 */
	public static int parseOnlyWhiteSpace(String input, int start) {
		while(Character.isWhitespace(input.charAt(start))) {
			start++;
		}
		return start;
	}
	
	/**
	 * Match a constant string in the input string at the location given
	 * @param input The string to parse within
	 * @param start The location to start at
	 * @param constant The constant string to parse
	 * @return The first index after the constant.
	 */
	public static int parseConstant(String input, int start, String constant) {
		if(input.startsWith(constant, start)) {
			return start + constant.length();
		} else {
			return -1;
		}
	}
}
