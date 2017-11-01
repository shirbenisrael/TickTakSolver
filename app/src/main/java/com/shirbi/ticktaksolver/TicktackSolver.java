package com.shirbi.ticktaksolver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class TicktackSolver {
	private int mOperands[];
	private int mResult;	
			
	public Formula mResultFormula = null;		
	
	private Handler mMainActivityHandler;
	
	int equationCounter;
	
	enum MessageType
	{
		JUST_INFORMATION, CALCULATION_FINISHED;
	}			
	
	TicktackSolver( int operands[], int result, Handler mainActivityHandler)
	{
		mOperands = operands;
		mResult = result;		
		
		mMainActivityHandler = mainActivityHandler;
		
		equationCounter = 0;
		
		
	}
	
	
	public boolean FindResult() 
	{	
		Formula tempFormula = new Formula( mOperands.length * 2 - 1 );
			
		boolean operandsInUse[]= new boolean[ mOperands.length];
		
		for ( int index = 0 ; index < mOperands.length ; index++)
		{
			operandsInUse[index] = false;
		}
		
		TokenOperator allOperators[] = new TokenOperator[4];
		allOperators[0] = new OperatorPlus();
		allOperators[1] = new OperatorMinus();
		allOperators[2] = new OperatorMult();
		allOperators[3] = new OperatorDiv();
		
		TokenOperator onlyPlusAndMinus[] = new TokenOperator[2];
		onlyPlusAndMinus[0] = allOperators[0];
		onlyPlusAndMinus[1] = allOperators[1];
		
		TokenOperator plusAndMult[] = new TokenOperator[2];
		plusAndMult[0] = allOperators[0];
		plusAndMult[1] = allOperators[2];
		
		TokenOperator noDiv[] = new TokenOperator[3];
		noDiv[0] = allOperators[0];
		noDiv[1] = allOperators[1];
		noDiv[2] = allOperators[2];
		
		TokenOperator noMinus[] = new TokenOperator[3];
		noMinus[0] = allOperators[0];
		noMinus[1] = allOperators[2];
		noMinus[2] = allOperators[3];
		
		if ( FindResultRecursive( tempFormula, operandsInUse, onlyPlusAndMinus ) )
		{
			mResultFormula = tempFormula;
			return true;
		} 
		else if ( FindResultRecursive( tempFormula, operandsInUse, plusAndMult ) )
		{
			mResultFormula = tempFormula;
			return true;
		} 
		else if ( FindResultRecursive( tempFormula, operandsInUse, noDiv ) )
		{
			mResultFormula = tempFormula;
			return true;
		} 
		else if ( FindResultRecursive( tempFormula, operandsInUse, noMinus ) )
		{
			mResultFormula = tempFormula;
			return true;
		} 
		else if ( FindResultRecursive( tempFormula, operandsInUse, allOperators ) )
		{
			mResultFormula = tempFormula;
			return true;
		}
		else
		{
			return false;
		}
										
	}
	
	private boolean FindResultRecursive( Formula tempFormula, boolean operationInUse[], TokenOperator operators[] )
	{			
		int numOfOperands = tempFormula.GetNumOfOperands();
		int numOfOperators = tempFormula.GetNumOfOperators();
		int maxTokens = tempFormula.GetMaxNumOfTokens();
		
		boolean canInsertNewOperator = 
				( ( numOfOperators+1 < numOfOperands ) ||
				  ( (numOfOperators + numOfOperands == maxTokens - 1 ) ) && ( numOfOperands >= 2) );
				
		boolean canInsertNewOperand = 
				( numOfOperands * 2 - 1 < maxTokens );									  
		
		if ( canInsertNewOperator )
		{
			boolean isBreakingSymetry = tempFormula.IsBreakingSymetry(); 
			
			for ( int i = 0 ; i < operators.length; i ++ )
			{			
				if ( operators[i].IsSymetricOperation() ) // plus or multiply
				{
					if ( !isBreakingSymetry ) // equal to swap between the two sides of the operation.
					{
						continue;
					}
				}
				else // minus or divide
				{
					if ( tempFormula.IsSameOperator( operators[i] ) ) 					
					{
						// we don't want to put two minus (or divide) in a raw, since we can replace one of them 
						// with a plus (or multiply). 
						// ABC-- = (A-(B-C)) = (A-B)+C.
						// ABC// = (A/(B/C)) = (A/B)*C.						
						continue;
					}
				}								
						
				try
				{
					tempFormula.PushToken( operators[i]);
				}
				catch (Exception e) // probably divide by zero or negative
				{
					tempFormula.PopToken();
					continue;
				}
				
				if ( FindResultRecursive( tempFormula, operationInUse, operators ) )
				{
					return true;					
				}
				else
				{
					tempFormula.PopToken();
				}
			}
		}
		
		if ( canInsertNewOperand )
		{
			for ( int i = 0 ; i < mOperands.length ; i ++ )
			{
				if ( operationInUse[i] == false )
				{
					tempFormula.PushToken( new TokenOperand( mOperands[i] ) ) ;
					operationInUse[i] = true;
					if ( FindResultRecursive( tempFormula, operationInUse, operators ) )
					{
						return true;					
					}
					else
					{
						tempFormula.PopToken();
						operationInUse[i] = false;											
					}
				}
			}
		}

		if ( ( !canInsertNewOperator ) && ( !canInsertNewOperand ) )
		{
			Rational calculationResult = tempFormula.Calculate();

			equationCounter++;								
				
			if ( equationCounter % 1000 == 0)
			{
				Bundle bundle = new Bundle();		        	
				String resultString = String.valueOf(equationCounter);		        	
				bundle.putString("myKey", resultString );
				bundle.putInt("otherKey", MessageType.JUST_INFORMATION.ordinal()) ;
				
				
				Message msg = mMainActivityHandler.obtainMessage();
				
				
				msg.setData(bundle);		        	
				mMainActivityHandler.sendMessage(msg);   				
			}
				
			return calculationResult.Equals(mResult);					 
						
		}
		
		return false;
		
	}
	
}
