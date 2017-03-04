package vm;

import type.VaribleType;

public class Varible implements Cloneable {
	public String name;
	public VaribleType type;
	public Object value;

	public Varible(String name, VaribleType t, Object value) {
		this.name = name;
		this.type = t;
		this.value = value;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
