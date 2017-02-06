package parse;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import type.NativeFunction;
import type.ScriptFunction;
import type.VaribleType;
import vm.ByteCode;
import vm.CmdType;
import lex.Lexxer;
import lex.Token;
import lex.TokenType;

public class Parser {
	private LinkedList<ByteCode> result;
	private LinkedList<Constant> consts;
	private Lexxer lex;
	private Stack<SymbolTable> symtab;

	private LinkedList<ByteCode> jmp_list;
	private int code_index = 0;
	private int func_local_count = 0;
	private int a_arg_count = 0;
	private boolean inFunc = false;
	public HashMap<String, NativeFunction> nFunc;

	public Parser(Lexxer l) {
		this.result = new LinkedList<ByteCode>();
		this.consts = new LinkedList<Constant>();
		this.lex = l;
		this.symtab = new Stack<SymbolTable>();
		symtab_new();
		this.jmp_list = new LinkedList<ByteCode>();
		this.nFunc = new HashMap<String, NativeFunction>();
	}

	public void parse() {
		lex.next();
		P();
		if (lex.get().type == null) {
			System.out.println("Sync completed");
		}
		write_code(CmdType.HALT, 0, null);
	}

	public ByteCode[] getCode() {
		return result.toArray(new ByteCode[result.size()]);
	}

	public Constant[] getConsts() {
		return consts.toArray(new Constant[consts.size()]);
	}

	private boolean match(TokenType t) {
		if (lex.get().type == t) {
			return true;
		}
		return false;
	}

	private void token_err() {
		System.out.println("unexpected token at[" + lex.get().line + "]:'"
				+ lex.get().value + "'");
		System.exit(0);
	}

	private void should_be_err(TokenType t) {
		System.out.println("it should be token with type: " + t + " at["
				+ lex.get().line + "]:'" + lex.get().value + "'");
		System.exit(0);
	}

	private void redeclared_err(String name) {
		System.out.println("the symbol[" + lex.get().line + "]:'" + name
				+ "'was redeclared");
		System.exit(0);
	}

	private void undeclared_err(String name) {
		System.out.println("the symbol[" + lex.get().line + "]:'" + name
				+ "'was undeclared");
		System.exit(0);
	}

	private void accept(TokenType t) {
		if (lex.get().type == t) {
			lex.next();
			return;
		}
		should_be_err(t);
	}

	private void accept() {
		lex.next();
	}

	private void symtab_add(String name) {
		symtab.peek().add(name);
	}

	private void symtab_add(String name, int index) {
		symtab.peek().add(name, index);
	}

	private void symtab_add(String name, NativeFunction m) {
		symtab.peek().add(name);
		nFunc.put(name, m);
	}

	private void symtab_new() {
		symtab.push(new SymbolTable());
	}

	private void symtab_pop() {
		symtab.pop();
	}

	private boolean symtab_isLocal(String name) {
		Iterator<SymbolTable> it = symtab.iterator();
		while (it.hasNext()) {
			SymbolTable s = it.next();
			if (s.isLocal(name))
				return true;
		}
		return false;
	}

	public void registerFunc(String name, Method m) {
		symtab_add(name, new NativeFunction(m, m.getParameterTypes().length));
	}

	private int symtab_getLocal_Offset(String name) {
		Iterator<SymbolTable> it = symtab.iterator();
		while (it.hasNext()) {
			SymbolTable s = it.next();
			if (s.isLocal(name))
				return s.getLocalOffset(name);
		}
		return -1;
	}

	private boolean symtab_contains(String name) {
		Iterator<SymbolTable> it = symtab.iterator();
		while (it.hasNext()) {
			SymbolTable s = it.next();
			if (s.contains(name))
				return true;
		}
		return false;
	}

	public void printConsts() {
		Iterator<Constant> i = consts.iterator();
		int r = 0;
		while (i.hasNext()) {
			Constant y = i.next();
			Object o=y.value;
			if(y.type==VaribleType.STRING)
			{
				String s=(String)o;
				s=s.replace("\\", "\\\\");
				s=s.replace("\n","\\n");
				s=s.replaceAll("\r", "\\r");
				s=s.replaceAll("\t", "\\t");
				o="\'"+s+"\'";
			}
			System.out.println(r + "\t" + y.type.toString() + "\t" + o);
			r++;
		}
	}

	public void printCode() {
		Parser.printCode(this.result);
	}

	public static void printCode(LinkedList<ByteCode> b) {
		Iterator<ByteCode> i = b.iterator();
		int r = 0;
		while (i.hasNext()) {
			ByteCode y = i.next();
			System.out.print(r + "\t");
			switch (y.opcode) {
			case LOAD_CONST:
				System.out.println("LOAD_CONST " + y.arg);
				break;
			case OPTR:
				System.out.println("OPTR " + y.extra.toString());
				break;
			case POP:
				System.out.println("POP");
				break;
			case POPN:
				System.out.println("POPN " + y.arg);
				break;
			case NEW_VAR:
				System.out.println("NEW_VAR " + y.arg);
				break;
			case SET_VAR:
				System.out.println("SET_VAR " + y.arg);
				break;
			case LOAD_VAR:
				System.out.println("LOAD_VAR " + y.arg);
				break;
			case TEST:
				System.out.println("TEST " + y.arg);
				break;
			case JMP:
				System.out.println("JMP " + y.arg);
				break;
			case JMP_BACK:
				System.out.println("JMP_BACK " + y.arg);
				break;
			case HALT:
				System.out.println("HALT");
				break;
			case LOAD_TRUE:
				System.out.println("LOAD_TRUE");
				break;
			case LOAD_FALSE:
				System.out.println("LOAD_FALSE");
				break;
			case CALL:
				System.out.println("CALL " + y.arg);
				break;
			case SET_RET_VAL:
				System.out.println("SET_RET_VAL");
				break;
			case RET:
				System.out.println("RET");
				break;
			case LOAD_LOCAL:
				System.out.println("LOAD_LOCAL " + y.arg);
				break;
			case NEW_LOCAL:
				System.out.println("NEW_LOCAL");
				break;
			case SET_LOCAL:
				System.out.println("SET_LOCAL " + y.arg);
				break;
			default:
				break;
			}
			r++;
		}
	}

	private void P() {
		if (match(TokenType.VAR)) {
			accept();
			if (match(TokenType.IDEN)) {
				Token t = lex.get();
				if (symtab_contains(t.value)) {
					redeclared_err(t.value);
				}
				int i = getConstant(VaribleType.STRING, t.value);
				if (!inFunc) {
					write_code(CmdType.NEW_VAR, i, null);
					symtab_add(t.value);
				} else {
					write_code(CmdType.NEW_LOCAL, 0, null);
					symtab_add(t.value, func_local_count);
					func_local_count++;
				}
				accept(TokenType.IDEN);
				if (!inFunc)
					V();
				else
					V_Func();
				P();
			} else {
				should_be_err(TokenType.IDEN);
			}
		} else if (match(TokenType.IDEN)) {
			Token t = lex.get();
			accept(TokenType.IDEN);
			if (match(TokenType.VALUE)) {
				if (!inFunc)
					V();
				else
					V_Func();
				P();
			} else if (match(TokenType.LBRKT)) {
				expr_FUNC_CALL(t.value);
				P();
			}
		}
		/*
		 * else if (match(TokenType.CONST_CHAR) || match(TokenType.CONST_STRING)
		 * || match(TokenType.CONST_NULL) || match(TokenType.CONST_NUMBER)) {
		 * E(); P(); }
		 */else if (match(TokenType.IF)) {
			jmp_list.clear();
			expr_IF();
			setJmpArg(code_index);
			P();
		} else if (match(TokenType.WHILE)) {
			expr_WHILE();
			P();
		} else if (match(TokenType.FUNC)) {
			if (inFunc) {
				System.out.println("函数内部不允许定义函数[" + lex.get().line + "]");
				System.exit(0);
			}
			expr_FUNC();
			P();
		} else if (match(TokenType.RETURN)) {
			expr_RETURN();
			P();
		}
	}

	private void expr_RETURN() {
		accept();

		E();
		write_code(CmdType.SET_RET_VAL, 0, null);
		write_code(CmdType.RET, 0, null);
	}

	private void V_Func() {
		if (match(TokenType.VALUE)) {
			lex.prev();
			Token t = lex.get();
			lex.next();
			accept(TokenType.VALUE);
			E();
			if (!symtab_isLocal(t.value)) {
				int i = getConstant(VaribleType.STRING, t.value);
				write_code(CmdType.SET_VAR, i, null);
			} else {
				int i = symtab_getLocal_Offset(t.value);
				write_code(CmdType.SET_LOCAL, i, null);
			}
			write_code(CmdType.POP, 0, null);
		}
		if (match(TokenType.COMMA)) {
			accept(TokenType.COMMA);
			accept(TokenType.IDEN);
			V_Func();
		}
	}

	private void expr_FUNC_CALL(String name) {
		accept();
		int tmp = a_arg_count;
		a_arg_count = 0;
		if (!match(TokenType.RBRKT))
			expr_actual_arg();
		accept(TokenType.RBRKT);
		int name_id = getConstant(VaribleType.STRING, name);
		write_code(CmdType.LOAD_VAR, name_id, null);
		write_code(CmdType.CALL, a_arg_count, null);
		a_arg_count = tmp;
	}

	private void expr_FUNC() {
		func_local_count = 0;
		inFunc = true;
		accept();
		Token t = lex.get();

		if (symtab_contains(t.value)) {
			redeclared_err(t.value);
		}

		int var_id = getConstant(VaribleType.STRING, t.value);
		if (!symtab_contains(t.value)) {
			write_code(CmdType.NEW_VAR, var_id, null);
			symtab_add(t.value);
		}
		symtab_new();

		accept(TokenType.IDEN);
		accept(TokenType.LBRKT);
		expr_virtual_arg();
		int vc = func_local_count;
		accept(TokenType.RBRKT);
		LinkedList<ByteCode> tmp_code = this.result;
		int tmp_code_index = this.code_index;
		this.result = new LinkedList<ByteCode>();
		this.code_index = 0;
		P_block();
		LinkedList<ByteCode> func_code = this.result;
		if (func_code.get(func_code.size() - 1).opcode != CmdType.RET)
			func_code.add(new ByteCode(CmdType.RET, 0, null));
		/*
		 * Iterator<ByteCode> it = func_code.iterator(); while (it.hasNext()) {
		 * ByteCode v = it.next();
		 * System.out.println(v.opcode.toString()+" "+v.arg); }
		 */
		this.result = tmp_code;
		this.code_index = tmp_code_index;
		ScriptFunction func = new ScriptFunction(
				func_code.toArray(new ByteCode[func_code.size()]), vc);
		symtab_pop();

		int func_id = getConstant(VaribleType.FUNCTION, func);
		write_code(CmdType.LOAD_CONST, func_id, null);
		write_code(CmdType.SET_VAR, var_id, null);
		write_code(CmdType.POP, 0, null);
		inFunc = false;

		System.out.println(func);
		Parser.printCode(func_code);
		System.out.println();
	}

	private void expr_actual_arg() {
		E();
		a_arg_count++;
		expr_actual_arg1();
	}

	private void expr_actual_arg1() {
		if (match(TokenType.COMMA)) {
			a_arg_count++;
			accept();
			E();
			expr_actual_arg1();
		}
	}

	private void expr_virtual_arg() {
		if (match(TokenType.IDEN)) {
			expr_virtual_arg1();
		}
	}

	private void expr_virtual_arg1() {
		if (match(TokenType.IDEN)) {
			symtab_add(lex.get().value, func_local_count);
			accept();
			func_local_count++;
			if (match(TokenType.COMMA)) {
				accept();
				expr_virtual_arg1();
			}
		} else {
			should_be_err(TokenType.IDEN);
		}
	}

	private void expr_WHILE() {
		int begin = code_index;
		accept();
		accept(TokenType.LBRKT);
		E();
		accept(TokenType.RBRKT);
		ByteCode c = write_code(CmdType.TEST, code_index, null);
		symtab_new();
		P_block();
		symtab_pop();
		write_code(CmdType.JMP_BACK, code_index - begin, null);
		c.arg = code_index - c.arg;
	}

	private void setJmpArg(int i) {
		Iterator<ByteCode> it = jmp_list.iterator();
		while (it.hasNext()) {
			ByteCode c = it.next();
			c.arg = i - c.arg;
		}
	}

	// include:if|elif
	private void expr_IF() {
		accept();
		accept(TokenType.LBRKT);
		E();
		accept(TokenType.RBRKT);
		ByteCode c = write_code(CmdType.TEST, code_index, null);
		symtab_new();
		P_block();
		symtab_pop();
		jmp_list.add(write_code(CmdType.JMP, code_index, null));// save all the
																// jmp code
																// address,code_index
																// is used to
																// work out the
																// offset
		c.arg = code_index - c.arg;
		if (match(TokenType.ELSEIF)) {
			expr_IF();
		} else if (match(TokenType.ELSE)) {
			expr_ELSE();
		}
	}

	private void expr_ELSE() {
		accept();
		symtab_new();
		P_block();
		symtab_pop();
	}

	private void P_block() {
		int i = func_local_count;
		LinkedList<ByteCode> tmp = jmp_list;
		jmp_list = new LinkedList<ByteCode>();
		accept(TokenType.LBBRKT);
		P();
		accept(TokenType.RBBRKT);
		if (inFunc) {
			int s = func_local_count - i;
			write_code(CmdType.POPN, s, null);
			func_local_count = i;
		}
		jmp_list = tmp;
	}

	private void V() {
		if (match(TokenType.VALUE)) {
			lex.prev();
			Token t = lex.get();
			lex.next();
			accept(TokenType.VALUE);
			E();
			int i = getConstant(VaribleType.STRING, t.value);
			write_code(CmdType.SET_VAR, i, null);
			write_code(CmdType.POP, 0, null);
		}
		if (match(TokenType.COMMA)) {
			accept(TokenType.COMMA);
			accept(TokenType.IDEN);
			V();
		}
	}

	private void E() {
		D();
		E1();
	}

	private void E1() {
		if(match(TokenType.LOGICAL_AND)||match(TokenType.LOGICAL_OR))
		{
			Token t = lex.get();
			accept();
			D();
			write_code(CmdType.OPTR, 0, getSymbolFromType(t.type));
			E1();
		}
	}
	
	private void D()
	{
		R();
		D1();
	}
	
	private void D1()
	{
		if(match(TokenType.EQUAL) || match(TokenType.MORE_EQUAL)
				|| match(TokenType.LESS_EQUAL) || match(TokenType.MORE)
				|| match(TokenType.LESS))
		{
			Token t = lex.get();
			accept();
			R();
			write_code(CmdType.OPTR, 0, getSymbolFromType(t.type));
			D1();
		}
	}
	
	private void R()
	{
		T();
		R1();
	}
	
	private void R1()
	{

		if (match(TokenType.PLUS) || match(TokenType.MINUS)) {
			Token t = lex.get();
			accept();
			T();
			write_code(CmdType.OPTR, 0, getSymbolFromType(t.type));
			R1();
		}

	}

	private void T() {
		F();
		T1();
	}

	private String getSymbolFromType(TokenType t) {
		switch (t) {
		case MULTI:
			return "*";
		case DIV:
			return "/";
		case PLUS:
			return "+";
		case MINUS:
			return "-";
		case EQUAL:
			return "==";
		case MORE_EQUAL:
			return ">=";
		case LESS_EQUAL:
			return "<=";
		case MORE:
			return ">";
		case LESS:
			return "<";
		case LOGICAL_AND:
			return "&&";
		case LOGICAL_OR:
			return "||";
		case PPLUS:
			return "++";
		case MMINUS:
			return "--";
		default:
			break;
		}
		return null;
	}

	private void T1() {
		if (match(TokenType.MULTI) || match(TokenType.DIV)) {
			Token t = lex.get();
			accept();
			F();
			write_code(CmdType.OPTR, 0, getSymbolFromType(t.type));
			T1();
		}
	}

	private void F() {
		if (match(TokenType.LBRKT)) {
			accept(TokenType.LBRKT);
			E();
			accept(TokenType.RBRKT);
		} else if (match(TokenType.MINUS)) {
			accept();
			F();
			write_code(CmdType.LOAD_CONST,
					getConstant(VaribleType.NUMBER, "-1"), null);
			write_code(CmdType.OPTR, 0, "*");
		} else if (match(TokenType.CONST_NUMBER) || match(TokenType.CONST_CHAR)
				|| match(TokenType.CONST_STRING)) {
			Token t = lex.get();
			accept();
			int i = getConstant(getConstType(t.type), t.value);
			write_code(CmdType.LOAD_CONST, i, null);
		} else if (match(TokenType.IDEN)) {
			Token t = lex.get();
			if (!symtab_contains(t.value)) {
				undeclared_err(t.value);
			}
			accept();
			if (match(TokenType.LBRKT)) {
				expr_FUNC_CALL(t.value);
			} else {
				if (!symtab_isLocal(t.value)) {
					int i = getConstant(VaribleType.STRING, t.value);
					write_code(CmdType.LOAD_VAR, i, null);
				} else {
					int i = symtab_getLocal_Offset(t.value);
					write_code(CmdType.LOAD_LOCAL, i, null);
				}
			}
		} else if (match(TokenType.CONST_NULL)) {
			accept();
			write_code(CmdType.LOAD_NULL, 0, null);
		} else if (match(TokenType.TRUE)) {
			accept();
			write_code(CmdType.LOAD_TRUE, 0, null);
		} else if (match(TokenType.TRUE)) {
			accept();
			write_code(CmdType.LOAD_FALSE, 0, null);
		} else {
			token_err();
		}
	}

	private VaribleType getConstType(TokenType t) {
		if (t == TokenType.CONST_NUMBER) {
			return VaribleType.NUMBER;
		} else if (t == TokenType.CONST_CHAR) {
			return VaribleType.CHAR;
		} else if (t == TokenType.CONST_STRING) {
			return VaribleType.STRING;
		}
		return null;
	}

	private ByteCode write_code(CmdType opcode, int arg, Object extra) {
		ByteCode c = new ByteCode(opcode, arg, extra);
		result.add(c);
		code_index++;
		return c;
	}

	private int getConstant(VaribleType t, Object value) {
		Iterator<Constant> i = consts.iterator();
		int r = 0;
		while (i.hasNext()) {
			Constant y = i.next();
			if (y.type.equals(t) && y.value.equals(value)) {
				return r;
			}
			r++;
		}
		consts.add(new Constant(t, value));
		return consts.size() - 1;
	}
}