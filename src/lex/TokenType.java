package lex;

public enum TokenType {
	UNKNOWN,
	NONE,

	IDEN,
	VOID,
	CONST_NULL,
	CONST_CHAR,
	CONST_NUMBER,

	CONST_STRING,
	ANNO,
	ENDOFPROGRAM,

	VALUE,		//=


	LESS,		//<
	MORE,		//>
	LESS_EQUAL,	//<=
	MORE_EQUAL,	//>=
	EQUAL,		//==


	PLUS,		//+
	MINUS,		//-
	MULTI,		//*
	DIV,		///
	PPLUS,		//++
	MMINUS,		//--
	OR,			//|
	AND,		//&
	NOT,		//!

	LBRKT,
	RBRKT,
	LMBRKT,
	RMBRKT,
	LBBRKT,
	RBBRKT,
	COMMA,
	DIT,

	FUNC,
	RETURN,
	IF,
	ELSEIF,
	ELSE,
	WHILE,
	THEN,
	END,
	BREAK,
	CONTINUE,
	VAR,
	TABLE,
	TRUE,
	FALSE
}
