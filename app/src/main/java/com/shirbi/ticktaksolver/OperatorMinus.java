package com.shirbi.ticktaksolver;

public class OperatorMinus extends TokenOperator {

	@Override
	public Rational Calculate( Token operand1, Token operand2)	
	{
		Rational operand1Value = operand1.GetCalculatedValue();
		Rational operand2Value = operand2.GetCalculatedValue();
		
		mValueUntilHere = new Rational();
		
		mValueUntilHere.mNominator = 
				operand1Value.mNominator*operand2Value.mDenominator -
				operand2Value.mNominator*operand1Value.mDenominator;
		
		mValueUntilHere.mDenominator = operand1Value.mDenominator * operand2Value.mDenominator;

		// Forbid negative values. Any positive results with negative number can be reached without negative number.
		if ( mValueUntilHere.mNominator < 0 )
		{
			throw new ArithmeticException("Nominator < 0!");
		}

		return mValueUntilHere;
	}

	@Override
	public String toString() {
		return "-";
	}

	@Override
	public Token Clone() {
		return new OperatorMinus();		
	}

	@Override
	public boolean IsSymetricOperation() {
		return false;
	}

	@Override
	public int GetOrder() {
		return 2;
	}

}
