package example_BIT;

import java.util.LinkedList;
import java.util.Stack;

public class BitVariable {
	private LinkedList<Boolean> base;
	private int subAddress;
	
	public BitVariable() {
		base = new LinkedList<>();
		subAddress = 0;
		base.add(false);
	}
	
	public BitVariable(boolean std) {
		base.add(std);
		subAddress = 0;
	}
	
	public boolean get() {
		return base.get(subAddress);
	}
	
	public void setConst(boolean value) {
		base.set(subAddress, value);
	}
	
	public void setValueOf(BitAddress in) {
		base = in.base();
		subAddress = in.subAddress();
		while(subAddress >= base.size()) {
			base.add(false);
		}
	}
	
	public void setValueBeyond(BitAddress in) {
		base = in.base();
		subAddress = in.subAddress() + 1;
		while(base.size() <= subAddress) {
			base.add(false);
		}
	}
	
	public int subAddress() {
		return subAddress;
	}
	
	public LinkedList<Boolean> base() {
		return base;
	}
	
	public void assignValue(BitVariable in) {
		this.subAddress = 0;
		base.set(0, in.get());
	}
	
	
	public static int translate(String in) {
		if(in.equals("ONE")) {
			return 1;
		} else if(in.equals("ZERO")) {
			return 0;
		} else {
			System.err.println("invalid input to bit translation");
			System.exit(1);
			return -1;
		}
	}
	
	public static String translate(boolean in) {
		return in ? "ONE" : "ZERO";
	}
	
	public void dump(Stack<Object> stack) {
		for(int i = subAddress; i < base.size() - 1; i++) {
			stack.push(translate(base.get(i)));
		}
	}
	
	public void unDump(Stack<Object> stack) {
		boolean first = true;
		while(!stack.isEmpty()) {
			if(!first) {
				subAddress++;
			} else {
				first = false;
			}
			if(subAddress == base.size()) {
				base.add(false);
			}
			base.set(subAddress, translate((String) stack.pop()) == 1);
		}
		subAddress = 0;
		base.add(false);
	}
}
