package general;

import java.util.Stack;

/**
 * General class for handling caught errors in grammar and language parsing, and possibly interpreting.
 * Includes an error stack that holds nested errors and either throws or cancels the one on top when signaled.
 * @author nikhil
 *
 */
public class ErrorHandler {
	/**
	 * A stack to hold errors if there will not be enough information to create the text after the error occurs
	 */
	private static Stack<String> errors = new Stack<>();
	
	/**
	 * Add an error message to the error stack
	 * @param message The message to add
	 */
	public static void register(String message) {
		errors.push(message);
	}
	
	/**
	 * Cancel the top error added to the error stack.
	 */
	public static void cancel() {
		errors.pop();
	}
	
	/**
	 * To be called when the error at the top of the stack was thrown. Print the message and perhaps exit.
	 * @param fatal True if the system should shut down due to a fatal error.
	 */
	public static void commit(boolean fatal) {
		System.err.println(errors.pop());
		if(fatal) {
			System.exit(1);
		}
	}
	
	/**
	 * Immediately throw an error with the given message.
	 * @param message The message to display
	 * @param fatal True if the system should shut down due to a fatal error
	 */
	public static void error(String message, boolean fatal) {
		register(message);
		commit(fatal);
	}
}
