package parse;

import type.VaribleType;


public class Constant {
	public VaribleType type;
	public Object value;
	public Constant(VaribleType type,Object value)
	{
		this.type=type;
		this.value=value;
	}
}
