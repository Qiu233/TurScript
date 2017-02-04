package parse;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTable {

	private LinkedList<String> tab;
	@SuppressWarnings("unused")
	private boolean isInFunction = false;
	private HashMap<String, Integer> locals;

	public SymbolTable(boolean isInFunction) {
		this.isInFunction = isInFunction;
		this.locals = new HashMap<String, Integer>();
		this.tab = new LinkedList<String>();
	}

	public SymbolTable() {
		this.locals = new HashMap<String, Integer>();
		this.tab = new LinkedList<String>();
	}

	public void add(String name) {
		tab.add(name);
	}

	public void add(String name, int index) {
		tab.add(name);
		locals.put(name, index);
	}

	public int getLocalOffset(String name) {
		return locals.get(name);
	}

	public boolean isLocal(String name) {
		return locals.containsKey(name);
	}

	public boolean contains(String name) {
		return tab.contains(name);
	}

	public int getLength() {
		return tab.size();
	}

}
