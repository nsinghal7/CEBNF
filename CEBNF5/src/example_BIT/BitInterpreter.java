package example_BIT;

import java.util.HashMap;
import java.util.Stack;

import lexparser.MetaParser;
import lexparser.TokenGenerator;
import general.ReadWrite;

public class BitInterpreter {
	public static void main(String[] args) {
		TokenGenerator generator = new TokenGenerator();
		generator.registerStandardRules(new String[] {"ONE", "ZERO", "READ", "EQUALS", "AT", "BEYOND"},
															false, true);
		generator.registerStandardRules(new String[] {"CODE", "LINE", "NUMBER", "PRINT", "EQUALS", "VARIABLE",
														"THE", "JUMP", "REGISTER", "VALUE",
														"ADDRESS", "OF", "NAND", "OPEN", "PARENTHESIS", "CLOSE",
														"GOTO", "IF", "IS", "EQUAL", "TO"},
												false, false);
		generator.registerRule("blank", "\\s+", true, false);
		MetaParser parser = new MetaParser(generator);
		parser.parseGrammar(ReadWrite.read("BIT"));
		String output = parser.parseCode(ReadWrite.read("BITCODE")).toString();
		String[] interim = output.split(",makeLine,");
		String[][] post = new String[interim.length - 1][];
		for(int i = 0; i < post.length; i++) {
			post[i] = interim[i].split(",");
		}
		Stack<Object> stack = new Stack<>();
		HashMap<Long, Object> memory = new HashMap<>();
		boolean jump = false;
		HashMap<Long, Integer> lineMap = new HashMap<>();
		// map out lines, remove line numbers
		for(int i = 0; i < post.length; i++) {
			int j = 0;
			long line = 0;
			while(!post[i][j].equals("makeLineNumber")) {
				line *= 2;
				line += BitVariable.translate(post[i][j++]);
			}
			lineMap.put(line, i);
			String[] update = new String[post[i].length - j - 1];
			for(int k = 0; k < update.length; k++) {
				update[k] = post[i][k + j + 1];
			}
			post[i] = update;
		}
		
		
		int line = 0;
		while(line < post.length) {
			for(int i = 0; line < post.length && i < post[line].length; i++) {
				switch(post[line][i]) {
				case "READ":
					jump = BitVariable.translate(ReadWrite.prompt("Enter a bit")) == 1;
					break;
				case "start": case "ONE": case "ZERO": case "EQUALS": case "value": case "address": case "multiBit":
					stack.push(post[line][i]);
					break;
				case "splitGoto":
					boolean comp = BitVariable.translate((String)stack.pop()) == 1;
					if(jump == comp) {
						line = lineMap.get(readBitNum(stack));
						i = Integer.MAX_VALUE - 1;
					}
					stack.clear();
					break;
				case "goto":
					line = lineMap.get(readBitNum(stack));
					i = Integer.MAX_VALUE - 1;
					stack.clear();
					break;
				case "makePrint":
					System.out.print(BitVariable.translate((String)stack.pop()));
					break;
				case "noGoTo":
					line = post.length;
					break;
				case "makeAssignment":
					Object latter = stack.pop();
					
					if("multiBit".equals(latter)) {
						Stack<Object> mini = new Stack<>();
						Object next;
						while(!(next = stack.pop()).equals("EQUALS")) {
							mini.push(next);
						}
						((BitVariable) stack.pop()).unDump(mini);
						stack.clear();
						break;
					}
					
					stack.pop();
					Object former = stack.pop();
					if(former instanceof BitVariable) {
						if(latter instanceof BitVariable) {
							((BitVariable) former).assignValue((BitVariable) latter);
						} else if(latter instanceof String) {
							((BitVariable) former).setConst(BitVariable.translate((String) latter) == 1);
						} else {
							System.err.println("Setting bit value with bit address");
							return;
						}
					} else if(former instanceof BitAddress) {
						if(latter instanceof BitAddress) {
							((BitAddress) former).duplicate((BitAddress) latter);
						} else {
							System.err.println("Settinb bit address with non address");
							return;
						}
					} else if(former instanceof String) {
						if(latter instanceof BitVariable) {
							jump = ((BitVariable) latter).get();
						} else if(latter instanceof BitAddress) {
							jump = ((BitAddress) latter).get();
						} else if(latter instanceof String) {
							jump = BitVariable.translate((String) latter) == 1;
						}
					}
					stack.clear();
					break;
				case "makeVariable":
					long name = readBitNum(stack);
					if(memory.containsKey(name)) {
						stack.push(memory.get(name));
					} else if("value".equals(stack.pop())) {
						memory.put(name, stack.push(new BitVariable()));
					} else {
						memory.put(name, stack.push(new BitAddress()));
					}
					break;
				case "jumpReg":
					stack.push(jump ? "ONE" : "ZERO");
					break;
				case "makeValueAT":
					BitVariable next = new BitVariable();
					next.setValueOf((BitAddress) stack.pop());
					stack.push(next);
					break;
				case "makeValueBEYOND":
					next = new BitVariable();
					next.setValueBeyond((BitAddress) stack.pop());
					stack.push(next);
					break;
				case "makeAddress":
					stack.push(new BitAddress((BitVariable) stack.pop()));
					break;
				case "makeNand":
					latter = stack.pop();
					former = stack.pop();
					boolean left;
					if(former instanceof BitVariable) {
						left = ((BitVariable) former).get();
					} else if(former instanceof BitAddress) {
						left = ((BitAddress) former).get();
					} else {
						left = BitVariable.translate((String) former) == 1;
					}
					if(latter instanceof BitVariable) {
						stack.push(BitVariable.translate(!(left & ((BitVariable) latter).get())));
					} else if(latter instanceof BitAddress) {
						stack.push(BitVariable.translate(!(left & ((BitAddress) latter).get())));
					} else {
						stack.push(BitVariable.translate(!(left & (BitVariable.translate((String) latter) == 1))));
					}
					break;
				case "gotoVar":
					former = stack.pop();
					if(former instanceof BitVariable) {
						((BitVariable) former).dump(stack);
					} else {
						((BitAddress) former).dump(stack);
					}
					break;
				default:
					System.err.println("i forgot to handle the case of: " + post[line][i]);
					return;
				}
			}
		}
	}
	
	public static long readBitNum(Stack<Object> stack) {
		long ans = 0;
		long mult = 1;
		String in;
		while(!(in = (String)stack.pop()).equals("start")) {
			if(BitVariable.translate(in) == 1) {
				ans += mult;
			}
			mult <<= 1;
		}
		return ans;
	}
}
