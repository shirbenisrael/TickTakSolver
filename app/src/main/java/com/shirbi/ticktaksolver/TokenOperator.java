package com.shirbi.ticktaksolver;
public abstract class TokenOperator extends Token {
	TokenOperator()
	{
		mTokenType = TokenType.OPERATOR;
	}
	
	public Rational mValueUntilHere;
	
	public abstract Rational Calculate( Token operand1, Token operand2);
	
	public Rational GetCalculatedValue()
	{
		return mValueUntilHere;
	}
	
	public abstract int GetOrder(); 
	
	public abstract boolean IsSymetricOperation();
}
