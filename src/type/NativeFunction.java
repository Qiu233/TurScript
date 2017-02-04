package type;

import java.lang.reflect.Method;

public class NativeFunction extends Function{
	public Method m;
	public NativeFunction(Method m,int args_len)
	{
		this.m=m;
		this.args_len=args_len;
	}
}
