package com.shirbi.ticktaksolver;

public class TokenOperand extends Token {
	public Rational mValue;
	
	TokenOperand( int value )
	{
		mTokenType = TokenType.OPERAND;
		mValue = new Rational( value );
	}

	@Override
	public String toString() {		
		return mValue.toString();
	}
	
	public Rational GetCalculatedValue()
	{
		return mValue;
	}

	@Override
	public Token Clone() {
		return this; //I don't care it is the same .
	}
}
