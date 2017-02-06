package lex;

import java.util.HashMap;
import java.util.Stack;

public class Lexxer {
	private char[] code;
	private Token tok;
	public HashMap<String,TokenType> keys=new HashMap<String,TokenType>();
	public Stack<Integer> l=new Stack<Integer>();
	private int i=0,line=0;
	public Lexxer(String code)
	{
		this.code=(code+"\0").toCharArray();
		init();
	}
	public Token get()
	{
		if(tok.type==null)
			tok.value="EOF";
		return tok;
	}
	public void prev()
	{
		l.pop();
		i=l.pop();
		next();
	}
	public void next()
	{
		tok=new Token();
		l.push(i);
		char c=code[i];
		while(true)
		{
			c=code[i];
			if (c == '\r' || c == ' ' || c == '\t')
			{
				i++;
				continue;
			}
			else if (c == '\n')
			{
				++line;
				i++;
				continue;
			}
			else
				break;
		}
		
		if (c == '\"')
			getConstString();
		else if(Character.isDigit(c))
			getConstNumber();
		else if(c=='\'')
			getConstChar();
		else if(Character.isLetter(c))
			getLetter();
		else
			getOthers();
	}
	private char getSC(char t)
	{
		switch (t)
		{
		case 'N':
		case 'n':
			return '\n';
		case 'T':
		case 't':
			return '\t';
		case '\"':
			return '\"';
		case '\'':
			return '\'';
		case 'R':
		case 'r':
			return '\r';
		case '\\':
			return '\\';
		}
		return 0;
	}
	private void getConstString()
	{
		i++;
		tok.line=line;
		tok.type=TokenType.CONST_STRING;
		tok.value="";
		while(i<code.length&&code[i]!='\"')
		{
			if(code[i]=='\\')
			{
				tok.value+=getSC(code[i+1]);
				i+=2;
				continue;
			}
			tok.value+=code[i];
			i++;
		}
		i++;
		if(i>=code.length&&code[i-1]!='\"')
		{
			System.out.println("字符串没有结尾["+line+"]");
			System.exit(0);
		}
	}
	private void getConstNumber()
	{
		tok.line=line;
		tok.value="";
		tok.type=TokenType.CONST_NUMBER;
		while(i<code.length&&(Character.isDigit(code[i])||code[i]=='.'))
		{
			tok.value+=code[i];
			i++;
		}
	}
	private void getConstChar()
	{
		i++;
		if(i>=code.length)
		{
			System.out.println("字符串没有结尾["+line+"]");
			System.exit(0);
		}
		tok.line=line;
		tok.type=TokenType.CONST_CHAR;
		tok.value="";
		if(code[i]=='\\')
		{
			if(i+1>=code.length)
			{
				System.out.println("字符串没有结尾["+line+"]");
				System.exit(0);
			}
			tok.value=String.valueOf(getSC(code[i+1]));
			i++;
		}
		else
		{
			tok.value+=code[i];
		}
		if(code[i+1]!='\'')
		{
			System.out.println("字符串没有结尾["+line+"]");
			System.exit(0);
		}
		i+=2;
	}
	private void getLetter()
	{
		tok.line=line;
		tok.type=TokenType.IDEN;
		tok.value="";
		while(true)
		{
			if(i<code.length&&(Character.isDigit(code[i])||Character.isLetter(code[i])||code[i]=='_'))
				tok.value+=code[i];
			else
			{
				if(keys.containsKey(tok.value))
				{
					tok.type=keys.get(tok.value);
				}
				break;
			}
			i++;
		}
	}
	private void getOthers()
	{
		tok.line=line;
		tok.value="";
		if (i < code.length)
		{
			switch (code[i])
			{
			case '=':
				if (code[i + 1] == '=')
				{
					tok.value = "==";
					tok.type = TokenType.EQUAL;
					i += 2;
				}
				else
				{
					tok.value = "=";
					tok.type = TokenType.VALUE;
					++i;
				}
				break;
			case '+':
				if (code[i + 1] == '+')
				{
					tok.value = "++";
					tok.type = TokenType.PPLUS;
					i += 2;
					break;
				}
				tok.value = "+";
				tok.type = TokenType.PLUS;
				++i;
				break;
			case '-':
				if (code[i + 1] == '-')
				{
					tok.value = "--";
					tok.type = TokenType.MMINUS;
					i += 2;
					break;
				}
				tok.value = "-";
				tok.type = TokenType.MINUS;
				++i;
				break;
			case '*':
				tok.value = "*";
				tok.type = TokenType.MULTI;
				++i;
				break;
			case '/':
				if (code[i + 1] == '/')
				{
					++i;
					while (code[i] != '\n' && code[i] != '\0')++i;
					tok.type = TokenType.ANNO;
				}
				else if (code[i + 1] == '*')
				{
					++i;
					while (true)
					{
						if (i + 1 >= code.length)
						{
							System.out.println("注释没有结尾["+line+"]");
							System.exit(0);
						}
						else if (code[i] == '*'&&code[i + 1] == '/')
						{
							tok.type = TokenType.ANNO;
							i += 2;
							break;
						}
						++i;
					}
				}
				else
				{
					tok.value = "/";
					tok.type = TokenType.DIV;
					++i;
				}
				break;
			case '(':
				tok.value = "(";
				tok.type = TokenType.LBRKT;
				++i;
				break;
			case ')':
				tok.value = ")";
				tok.type = TokenType.RBRKT;
				++i;
				break;
			case '[':
				tok.value = "[";
				tok.type = TokenType.LMBRKT;
				++i;
				break;
			case ']':
				tok.value = "]";
				tok.type = TokenType.RMBRKT;
				++i;
				break;
			case '{':
				tok.value = "{";
				tok.type = TokenType.LBBRKT;
				++i;
				break;
			case '}':
				tok.value = "}";
				tok.type = TokenType.RBBRKT;
				++i;
				break;
			case '>':
				if (code[i + 1] == '=')
				{
					tok.value = ">=";
					tok.type = TokenType.MORE_EQUAL;
					i += 2;
				}
				else
				{
					tok.value = ">";
					tok.type = TokenType.MORE;
					++i;
				}
				break;
			case '<':
				if (code[i + 1] == '=')
				{
					tok.value = "<=";
					tok.type = TokenType.LESS_EQUAL;
					i += 2;
				}
				else
				{
					tok.value = "<";
					tok.type = TokenType.LESS;
					++i;
				}
				break;
			case '|':
				tok.value = "&";
				if (code[i + 1] == '|')
				{
					tok.value = "||";
					tok.type = TokenType.LOGICAL_OR;
					i += 2;
					break;
				}
				tok.type = TokenType.LOGICAL_OR;
				++i;
				break;
			case '&':
				tok.value = "&";
				if (code[i + 1] == '&')
				{
					tok.value = "&&";
					tok.type = TokenType.LOGICAL_AND;
					i += 2;
					break;
				}
				tok.type = TokenType.AND;
				++i;
				break;
			case '!':
				tok.value = "!";
				tok.type = TokenType.NOT;
				++i;
				break;
			case ',':
				tok.value = ",";
				tok.type = TokenType.COMMA;
				++i;
				break;
			case '.':
				tok.value = ".";
				tok.type = TokenType.DIT;
				++i;
				break;
			case '\0':
				return;
			default:
				System.out.println("无法辨析字符["+line+"]:'"+code[i]+"'");
				System.exit(0);
				break;
			}
		}
		if(tok.type==TokenType.ANNO)
		{
			next();
		}
	}
	private void init()
	{
		keys.put("function",TokenType.FUNC);
		keys.put("return",TokenType.RETURN);
		keys.put("if",TokenType.IF);
		keys.put("while",TokenType.WHILE);
		keys.put("then",TokenType.THEN);
		keys.put("end",TokenType.END);
		keys.put("break",TokenType.BREAK);
		keys.put("continue", TokenType.CONTINUE);
		keys.put("null",TokenType.CONST_NULL);
		keys.put("void", TokenType.VOID);
		keys.put("var",TokenType.VAR);
		keys.put("true",TokenType.TRUE);
		keys.put("false", TokenType.FALSE);
		keys.put("table", TokenType.TABLE);
		keys.put("elif",TokenType.ELSEIF);
		keys.put("else",TokenType.ELSE);
	}
}
