package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Container class for simple methods involving IO
 * @author nikhil
 *
 */
public class ReadWrite {
	
	/**
	 * Read an entire file with the given filename. All newlines are converted to \n
	 * @param fileName The filename of the file to read
	 * @return The file text
	 */
	public static String read(String fileName) {
		try {
		    BufferedReader in = new BufferedReader(new FileReader(fileName));
		    String str;
		    String end = "";
		    boolean first = true;
		    while ((str = in.readLine()) != null) {
		        end = end +(first?"":"\n")+ str;
		        first = false;
		    }
		    in.close();
		    return end;
		} catch (IOException e) {
			return "false";
		}
	}
	
	/**
	 * Write the given file text to the given location
	 * @param file The file text to write
	 * @param location The location to write to
	 * @return True if successful, false if error
	 */
	public static boolean write(String file, String location) {
		try {
		    BufferedWriter out = new BufferedWriter(new FileWriter(location));
		    out.write(file);
		    out.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Show a prompt and await text input.
	 * @param prompt The prompt to show
	 * @return The text input
	 */
	public static String prompt(String prompt) {
		System.out.print(prompt);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try {
			input = reader.readLine();
		} catch (IOException e) {
			input = "errror";
		}
		return input;
	}
}
