package example_BIT;

import java.util.LinkedList;
import java.util.Stack;

public class BitAddress {
	private LinkedList<Boolean> base;
	private int subAddress;
	
	public BitAddress() {
		base = null;
		subAddress = -1;
	}
	
	public BitAddress(BitVariable in) {
		this.base = in.base();
		subAddress = 0;
	}
	
	public void duplicate(BitAddress in) {
		this.base = in.base;
		this.subAddress = in.subAddress;
	}
	
	public LinkedList<Boolean> base() {
		return base;
	}
	
	public int subAddress() {
		return subAddress;
	}
	
	public void setAddressOf(BitVariable in) {
		this.base = in.base();
		this.subAddress = in.subAddress();
	}
	
	public boolean get() {
		return base.get(subAddress);
	}
	
	public void dump(Stack<Object> stack) {
		for(int i = subAddress; i < base.size(); i++) {
			stack.push(BitVariable.translate(base.get(i)));
		}
	}
}
