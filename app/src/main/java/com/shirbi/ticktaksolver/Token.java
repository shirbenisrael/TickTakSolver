package com.shirbi.ticktaksolver;

public abstract class Token {
	public enum TokenType {
	    OPERAND, OPERATOR
	}
	
	public TokenType mTokenType;		
	
	public Token mTokenBefore;
	
	public abstract Token Clone();
	
	public abstract Rational GetCalculatedValue();
	
	public abstract String toString();
		
}
