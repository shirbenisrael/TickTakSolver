package com.shirbi.ticktaksolver;

public class OperatorDiv extends TokenOperator {

	@Override
	public Rational Calculate( Token operand1, Token operand2)	
	{
		Rational operand1Value = operand1.GetCalculatedValue();
		Rational operand2Value = operand2.GetCalculatedValue();
		
		mValueUntilHere = new Rational();
		
		mValueUntilHere.mNominator = operand1Value.mNominator*operand2Value.mDenominator;				
		
		mValueUntilHere.mDenominator = operand1Value.mDenominator * operand2Value.mNominator;
		
		if ( operand2Value.mNominator == 0 )
		{
			throw new ArithmeticException("denominator == 0!");			
		}
						
		return mValueUntilHere;		
	}

	@Override
	public String toString() {		
		return "/";
	}

	@Override
	public Token Clone() {
		return new OperatorDiv();
	}

	@Override
	public boolean IsSymetricOperation() {
		return false;
	}

	@Override
	public int GetOrder() {
		return 3;
	}

}
