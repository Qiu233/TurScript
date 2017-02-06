package vm;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import parse.Constant;
import parse.Parser;
import type.Function;
import type.NativeFunction;
import type.ScriptFunction;
import type.VaribleType;

public class TurVM {

	private Constant consts[];
	private Parser parser;
	private Stack<Varible> stack;
	private LinkedList<Varible> varible;
	private Object undefined = null;
	private Context ctx;
	private Stack<Context> ctx_stack;
	private Varible ret_val;

	public TurVM(Parser p) {
		this.parser = p;
		this.parser.parse();
		this.ctx = new Context(p.getCode(), false, 0, 0);
		this.consts = p.getConsts();
		this.ctx_stack = new Stack<Context>();
		this.stack = new Stack<Varible>();
		this.varible = new LinkedList<Varible>();
		this.ret_val = new Varible(null, null, null);
		initFunc();
	}

	private void initFunc() {
		Iterator<String> i = parser.nFunc.keySet().iterator();
		while (i.hasNext()) {
			String str = i.next();
			NativeFunction o = parser.nFunc.get(str);
			varible.add(new Varible(str, VaribleType.FUNCTION, o));
		}
	}

	private void pushState() {
		ctx.stackSize = stack.size();
		ctx_stack.push(ctx);
	}

	private Context popState() {
		ctx = ctx_stack.pop();
		ctx.ip++;
		return ctx;
	}
	 
	private double optr_bin_num(String f,double a,double b)
	{
		switch(f)
		{
		case "+":
			return a+b;
		case "-":
			return a-b;
		case "*":
			return a*b;
		case "/":
			return a/b;
		}
		return 0;//no meaning
	}
	
	private boolean optr_bin_bool(String f,boolean a,boolean b)
	{
		switch(f)
		{
		case "||":
			return a||b;
		case "&&":
			return a&&b;
		}
		return false;//no meaning
	}
	
	private boolean optr_bin_cmp_num(String f,double a,double b)
	{
		switch(f)
		{
		case "==":
			return a==b;
		case ">=":
			return a>=b;
		case "<=":
			return a<=b;
		case ">":
			return a>b;
		case "<":
			return a<b;
		}
		return false;//no meaning
	}
	
	private boolean optr_bin_cmp_str(String f,String a,String b)
	{
		int r=a.compareTo(b);
		switch(f)
		{
		case "==":if(r==0)return true;
			break;
		case ">=":if(r==0||r>0)return true;
		case "<=":if(r==0||r<0)return true;
		case ">":if(r>0)return true;
		case "<":if(r<0)return true;
		}
		return false;
	}
	
	
	private boolean optr_bin_cmp(String f,Object a,Object b)
	{
		boolean c = false;
		if(a instanceof Boolean)if((boolean)a==(boolean)b)return true;
		switch(f)
		{
		case "==":case ">=":case "<=":case ">":case "<":
			if(a instanceof Double)//the same type for b
			{
				c=optr_bin_cmp_num(f,(double)a,(double)b);
			}
			else if(a instanceof String)
			{
				c=optr_bin_cmp_str(f,(String)a,(String)b);
			}
			else if(a instanceof Character)
			{
				c=optr_bin_cmp_num(f,(char)a,(char)b);
			}
			return c;
		}
		return false;//no meaning
	}
	
	private void optr(String f) {
		if(f.equals("!"))
		{
			Varible v=stack.pop();
			stack.push(new Varible(null, VaribleType.BOOL, !(boolean)v.value));
			return;
		}
		Varible v2 = stack.pop();
		Varible v1 = stack.pop();
		Object c;

		if(f.equals("+"))
		{
			if(v2.type==VaribleType.STRING)
			{
				c=v1.value.toString()+(String)v2.value;
				stack.push(new Varible(null, VaribleType.STRING, c));
				return;
			}
			else if(v1.type==VaribleType.STRING)
			{
				c=(String)v1.value+v2.value.toString();
				stack.push(new Varible(null, VaribleType.STRING, c));
				return;
			}
		}
		switch(f)
		{
		case "+":case "-":case "*":case "/":
			c=optr_bin_num(f,(double) v1.value,(double) v2.value);
			stack.push(new Varible(null, VaribleType.NUMBER, c));
			break;
		case "||":case "&&":
			c=optr_bin_bool(f,(boolean)v1.value,(boolean)v2.value);
			stack.push(new Varible(null, VaribleType.BOOL, c));
			break;
		case "==":case ">=":case "<=":case ">":case "<":
			if(v2.type!=v1.type)
			{
				System.out.println("the value with type:"+v1.type+" and "+v2.type+" cannot be operated by operator:"+f);
			}
			c=optr_bin_cmp(f,v1.value,v2.value);
			stack.push(new Varible(null, VaribleType.BOOL, c));
			break;
		}
	}

	private void load_Const(int index) {
		Constant c = this.consts[index];
		if (c.type == VaribleType.NUMBER) {
			stack.push(new Varible(null, VaribleType.NUMBER, Double
					.parseDouble((String) c.value)));
		} else if (c.type == VaribleType.STRING) {
			stack.push(new Varible(null, VaribleType.STRING, (String) c.value));
		} else if (c.type == VaribleType.CHAR) {
			stack.push(new Varible(null, VaribleType.CHAR, ((String) c.value)
					.charAt(0)));
		} else if (c.type == VaribleType.FUNCTION) {
			stack.push(new Varible(null, VaribleType.FUNCTION, c.value));
		}
	}

	private Varible getVaribleFromName(String name) {
		Iterator<Varible> it = varible.iterator();
		while (it.hasNext()) {
			Varible v = it.next();
			if (v.name.equals(name))
				return v;
		}
		return null;
	}

	private void load_Var(int index) {
		Constant v = this.consts[index];
		Varible c = getVaribleFromName((String) v.value);
		if (c.type == VaribleType.NUMBER || c.type == VaribleType.STRING
				|| c.type == VaribleType.CHAR || c.type == VaribleType.FUNCTION) {
			stack.push(new Varible(null, c.type, c.value));
		}
	}

	public Object getResult() {
		return stack.empty() ? undefined : stack.peek();
	}

	private void new_Var(int index) {
		Constant c = this.consts[index];
		varible.add(new Varible((String) c.value, null, null));
	}

	/*
	 * private void assign_err(VaribleType a) { System.out.println("");
	 * System.exit(0); }
	 */
	private void set_Var(int index) {
		Constant c = this.consts[index];
		Varible dst = getVaribleFromName((String) c.value);
		Varible src = stack.peek();
		dst.type = src.type;
		dst.value = src.value;
	}

	private void popn(int m) {
		for (int i = 0; i < m; i++)
			stack.pop();
	}

	public void exec() {
		// StringBuilder progress = new StringBuilder("");
		for (; this.ctx.ip < this.ctx.code.length;) {
			// if(this.ctx.ip==11)
			if (this.ctx.halt) {
				if (this.ctx.isFunc)// a function is exited
				{
					popState();
					popn(stack.size() - ctx.stackSize);
					stack.push(new Varible(ret_val.name, ret_val.type,
							ret_val.value));
					ret_val = new Varible(null, null, null);
				} else
					return;
			}
			// progress.append(this.ctx.ip + ",");
			ByteCode cmd = this.ctx.code[this.ctx.ip];
			switch (cmd.opcode) {
			case POP:
				stack.pop();
				break;
			case POPN:
				popn(cmd.arg);
				break;
			case OPTR:
				optr((String) cmd.extra);
				break;
			case LOAD_CONST:
				load_Const(cmd.arg);
				break;
			case LOAD_NULL:
				stack.push(new Varible(null, null, null));
				break;
			case NEW_VAR:
				new_Var(cmd.arg);
				break;
			case LOAD_VAR:
				load_Var(cmd.arg);
				break;
			case SET_VAR:
				set_Var(cmd.arg);
				break;
			case TEST:
				Varible v = stack.pop();
				if (!isTrue(v)) {
					this.ctx.ip += cmd.arg;
					continue;
				}
				break;
			case JMP:
				this.ctx.ip += cmd.arg;
				continue;
			case JMP_BACK:
				this.ctx.ip -= cmd.arg;
				continue;
			case HALT:
				this.ctx.halt = true;
				break;
			case LOAD_TRUE:
				stack.push(new Varible(null, VaribleType.BOOL, true));
				break;
			case LOAD_FALSE:
				stack.push(new Varible(null, VaribleType.BOOL, false));
				break;
			/*
			 * case LOAD_MARK: stack.push(new Varible(null, VaribleType.MARK,
			 * null)); break;
			 */
			case CALL:
				Function func = (Function) stack.pop().value;
				if (func.args_len != cmd.arg) {
					System.out.println("参数数量不匹配" + func.args_len + ","
							+ cmd.arg);
				}
				call(func);
				continue;
			case RET:
				this.ctx.halt = true;
				continue;
			case SET_RET_VAL:
				this.ret_val = stack.peek();
				break;
			case LOAD_LOCAL:
				// System.out.println(ctx.stackBase);
				stack.push(stack.get(ctx.stackBase + cmd.arg));
				break;
			case NEW_LOCAL:
				stack.push(new Varible(null, null, null));
				break;
			case SET_LOCAL:
				stack.get(ctx.stackBase + cmd.arg).type = stack.peek().type;
				stack.get(ctx.stackBase + cmd.arg).value = stack.peek().value;
				break;
			default:
				break;
			}
			this.ctx.ip++;
		}
		// System.out.println(progress);
	}

	private void call(Function f) {
		if (f instanceof ScriptFunction) {
			pushState();
			this.ctx = new Context(((ScriptFunction) f).code, true,
					stack.size(), stack.size() - f.args_len);
		} else if (f instanceof NativeFunction) {
			NativeFunction nf = (NativeFunction) f;
			Object args[] = new Object[nf.args_len];
			for (int i = 0; i < nf.args_len; i++) {
				args[i] = stack.get(stack.size() - nf.args_len + i).value;
			}
			try {
				((NativeFunction) f).m.invoke(null, args);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.ctx.ip++;// 因为call指令并没有改变ip的值，调用本地函数不需要跳转，所以这里要补上ip的自增，否则会死循环
		}
	}

	private boolean isTrue(Varible v) {
		if (v.value instanceof Boolean) {
			if ((boolean) v.value == true)
				return true;
		}
		return false;
	}

	public Object getVarible(String name) {
		Varible v = getVaribleFromName(name);
		return v == null ? undefined : v.value;
	}

	public void printInfo() {
		parser.printConsts();
		System.out.println();
		parser.printCode();
	}
}
