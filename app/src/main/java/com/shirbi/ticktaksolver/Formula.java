package com.shirbi.ticktaksolver;

import com.shirbi.ticktaksolver.Token.TokenType;

public class Formula {

	private int mMaxNumberOfTokens;
	private Token mTokenArray[];	
	private int mCurrentCapacity;
	
	private int mNumOfOperands;
	private int mNumOfOperators;
	
	public Formula( int maxNumberOfTokens)
	{
		mMaxNumberOfTokens = maxNumberOfTokens;
		mCurrentCapacity = 0;
		mTokenArray = new Token[maxNumberOfTokens];		
		
		mNumOfOperands = 0;
		mNumOfOperators = 0;
		
	}
	
	public boolean PushToken( Token newToken)
	{
		if ( mCurrentCapacity >= mMaxNumberOfTokens)
		{
			return false;
		}		
		
		// We must not point from two cells in the array to the same object, since we change its 
		// internal value mValueUntilHere.
		
		Token internalToken = newToken.Clone();
		
		mTokenArray[mCurrentCapacity] = internalToken;
		mCurrentCapacity++;
		
		internalToken.mTokenBefore = ( mCurrentCapacity == 1 ) ? null: mTokenArray[mCurrentCapacity-2];
		
		if ( internalToken.mTokenType == TokenType.OPERAND )
		{
			mNumOfOperands++;			
		}
		else
		{
			mNumOfOperators++;
			Token firstTokenBefore = mTokenArray[mCurrentCapacity-1].mTokenBefore;
			Token secondTokenBefore = firstTokenBefore.mTokenBefore;
			TokenOperator tokenOperator = (TokenOperator)internalToken;
			
			tokenOperator.mValueUntilHere = tokenOperator.Calculate( secondTokenBefore , firstTokenBefore );
			tokenOperator.mTokenBefore = secondTokenBefore.mTokenBefore;
		}
		
		return true;
	}
	
	public boolean IsSameOperator( TokenOperator nextOperator )
	{
		Token firstTokenBefore = mTokenArray[mCurrentCapacity-1];
		
		if ( firstTokenBefore.mTokenType  == TokenType.OPERAND )
		{
			return false;
		}
		
		return ( ((TokenOperator)firstTokenBefore).GetOrder() == nextOperator.GetOrder() );
	}
	
	public boolean IsBreakingSymetry()
	{
		Token firstTokenBefore = mTokenArray[mCurrentCapacity-1];
		Token secondTokenBefore = firstTokenBefore.mTokenBefore;
		
		if ( ( firstTokenBefore.mTokenType  == TokenType.OPERAND ) && 
			 ( secondTokenBefore.mTokenType == TokenType.OPERAND ) )
		{

			return ( ((TokenOperand)firstTokenBefore).mValue.mNominator * 
					((TokenOperand)secondTokenBefore).mValue.mDenominator >= 
					((TokenOperand)secondTokenBefore).mValue.mNominator *
					((TokenOperand)firstTokenBefore).mValue.mDenominator);
		}
		
		if ( firstTokenBefore.mTokenType != secondTokenBefore.mTokenType )
		{
			return ( firstTokenBefore.mTokenType  == TokenType.OPERATOR );
		}
		
		// Both operators.
		return ( ((TokenOperator)firstTokenBefore).GetOrder() >= ((TokenOperator)secondTokenBefore).GetOrder() );
				
	}
	
	public void PopToken()
	{
		if ( mCurrentCapacity > 0 )
		{				
			mCurrentCapacity--;
			if ( mTokenArray[mCurrentCapacity].mTokenType == TokenType.OPERAND )
			{
				mNumOfOperands--;
			}
			else
			{
				mNumOfOperators--;
			}
			mTokenArray[mCurrentCapacity] = null;
		}
	}
	
	public int GetNumOfOperands() 
	{
		return mNumOfOperands;
	}
	
	public int GetNumOfOperators()
	{
		return mNumOfOperators;
	}
	
	public int GetMaxNumOfTokens()
	{
		return mMaxNumberOfTokens;
	}
	
	public Rational Calculate()
	{		
		return mTokenArray[mCurrentCapacity-1].GetCalculatedValue(); 		
	}
	
			
	public String ToString()
	{
		return ToString( 0 );
	}
	
	public String ToString( int maxNumOfMerge )
	{
		int nextIndex[] = new int[1];		
		int mergeUnder[]  = new int[1];
		return ToStringFromIndex ( mCurrentCapacity-1, nextIndex, false, maxNumOfMerge, mergeUnder );
	}
	
	private String ToStringFromIndex(  int index, int nextIndex[], boolean withBrackets, 
			int maxNumOfMerge , int mergeUnder[] )
	{
		Token currentToken = mTokenArray[index];
		if ( currentToken.mTokenType == TokenType.OPERAND )
		{
			nextIndex[0] = index-1;
			mergeUnder[0] = 0;
			return currentToken.toString();
		}
		else
		{	
			TokenOperator currentTokenOperator = ((TokenOperator)currentToken);
			
			boolean nextWithBrackets = true;
			int rightIndex = index-1;
			Token nextToken = mTokenArray[rightIndex];
			
			if ( nextToken.mTokenType == TokenType.OPERATOR )
			{
				if ( ((TokenOperator)nextToken).GetOrder() == currentTokenOperator.GetOrder() )
				{
					if ( currentTokenOperator.IsSymetricOperation() )
					{
						nextWithBrackets = false;
					}
				}
			}			
			
			int rightMergeUnder[] = new int[1];
			
			String rightString = ToStringFromIndex( rightIndex, nextIndex, nextWithBrackets,
					maxNumOfMerge ,  rightMergeUnder );
			
			nextWithBrackets = true;
			int leftIndex = nextIndex[0];
			
			int leftMergeUnder[] = new int[1];
			
			nextToken = mTokenArray[leftIndex];
			
			if ( nextToken.mTokenType == TokenType.OPERATOR )
			{
				if ( ((TokenOperator)nextToken).GetOrder() == currentTokenOperator.GetOrder() )
				{									
					nextWithBrackets = false;					
				}
			}				
			
			String leftString = ToStringFromIndex(leftIndex, nextIndex, nextWithBrackets,
					maxNumOfMerge ,  leftMergeUnder );
			
			mergeUnder[0] = Math.max( rightMergeUnder[0], leftMergeUnder[0] );
			
			if ( mergeUnder[0] < maxNumOfMerge )
			{
				mergeUnder[0]++;
				
				return currentTokenOperator.GetCalculatedValue().toString();
								
			}
			else if ( withBrackets )
			{			
				return "(" + leftString + currentToken.toString() + rightString + ")";
			}
			else
			{
				return leftString + currentToken.toString() + rightString;
			}
		}
	}
}
