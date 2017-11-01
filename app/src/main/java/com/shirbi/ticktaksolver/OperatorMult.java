package com.shirbi.ticktaksolver;

public class OperatorMult extends TokenOperator {

	@Override
	public Rational Calculate( Token operand1, Token operand2)	
	{
		Rational operand1Value = operand1.GetCalculatedValue();
		Rational operand2Value = operand2.GetCalculatedValue();
		
		mValueUntilHere = new Rational();
		
		mValueUntilHere.mNominator = operand1Value.mNominator*operand2Value.mNominator;				
		
		mValueUntilHere.mDenominator = operand1Value.mDenominator * operand2Value.mDenominator;
						
		return mValueUntilHere;		
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	public Token Clone() {
		return new OperatorMult();
	}

	@Override
	public boolean IsSymetricOperation() {
		return true;
	}

	@Override
	public int GetOrder() {
		return 1;
	}

}
