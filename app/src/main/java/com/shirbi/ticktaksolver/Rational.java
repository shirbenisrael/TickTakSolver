package com.shirbi.ticktaksolver;

public class Rational {
	public int mNominator;
	public int mDenominator;
	
	Rational( Rational other)
	{
		this.mNominator = other.mNominator;
		this.mDenominator = other.mDenominator;
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
	
	public int GCD(int a, int b) 
	{
		if (b==0) return a;
		return GCD(b,a%b);
	}	
	
	public String toString() {
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
				
		
		if ( mDenominator == 1)
		{
			resultString = String.valueOf( mNominator );
		}
		else
		{
			return "("+String.valueOf( mNominator ) + "/" + String.valueOf( mDenominator )+")";
		}
		
		if ( mNominator < 0 )
		{							
			return "("+resultString+")";
		}
		else
		{
			return resultString;
		}		
		
	}
	 
}
