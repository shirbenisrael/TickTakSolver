package com.shirbi.ticktaksolver;

public class Rational {
	public int mNominator;
	public int mDenominator;
	
	Rational( Rational other)
	{
		this.mNominator = other.mNominator;
		this.mDenominator = other.mDenominator;
	}

	Rational( int nominator, int denominator) {
		this.mNominator = nominator;
		this.mDenominator = denominator;
	}
	
	public void Copy( Rational other )
	{
		this.mNominator = other.mNominator;
		this.mDenominator = other.mDenominator;
	}
	
	public Rational( int num ) 
	{
		mNominator = num ;
		mDenominator = 1;
	}
	
	public Rational() 
	{
		mNominator = 0;
		mDenominator = 1;
	}
	
	public boolean Equals( int other)
	{		
		return ( ( other * mDenominator ) == mNominator );
	}

	public Rational Abs() {
		return new Rational(Math.abs(mNominator), Math.abs(mDenominator));
	}

	public boolean AbsSmallerThan( Rational other) {
		Rational thisAbs = this.Abs();
		Rational otherAbs = other.Abs();

		return (thisAbs.mDenominator * other.mNominator) > (otherAbs.mDenominator * this.mNominator);
	}
	
	public int GCD(int a, int b) 
	{
		if (b==0) return a;
		return GCD(b,a%b);
	}	

	public Boolean IsFraction() {
		return ((mDenominator != 1) && (mDenominator != -1));
	}

	public String toString() {
		String withoutBrackets = toStringWithoutBrackets();
		Boolean needBrackets = false;

		if ( mNominator < 0 ) {
			needBrackets = true;
		}

		if (IsFraction()) {
			needBrackets = true;
		}

		if (needBrackets) {
			return "(" + withoutBrackets + ")";
		} else {
			return withoutBrackets;
		}
	}

	public String toStringWithoutBrackets() {
		if ( mNominator == 0)
		{ 
			return "0";
		}				
				
		if ( mDenominator < 0 )
		{
			mDenominator*=(-1);
			mNominator*=(-1);
		}
		
		String resultString;
					
		int gcdValue = GCD( Math.abs(mNominator), mDenominator );
		mNominator = mNominator / gcdValue;
		mDenominator = mDenominator / gcdValue;

		if ( mDenominator == 1) {
			return String.valueOf( mNominator );
		} else {
			return String.valueOf( mNominator ) + "/" + String.valueOf( mDenominator );
		}
	}
	 
}
