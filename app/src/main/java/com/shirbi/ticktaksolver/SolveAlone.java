package com.shirbi.ticktaksolver;

public class SolveAlone {
	
	TokenOperand mValues[][];
	int mTarget;

	private enum State
	{
		NOTHING_WAS_SELECTED,
		FIRST_OPERAND_SELECTED,
		OPERATOR_SELECTED,
		SECOND_OPERAND_SELECTED
	};
	
	private State mState;
	int mFirstOperandIndex;
	int mSecondOperandIndex;
	int mSelectedOperator; 
	
	TokenOperator mSelectedtokenOperator;
	
	int mCurrentLine;
	
	SolveAlone( int[] values, int target )
	{
		mValues = new TokenOperand[values.length][];
		for (int i = 0 ; i < values.length ; i++)
		{
			mValues[i]= new TokenOperand[values.length];
			mValues[0][i] = new TokenOperand(values[i]);
		}
		
		mTarget = target;
		
		mState = State.NOTHING_WAS_SELECTED;	
		
		mCurrentLine = 0;
	}
	
	public String[] GetButtonToShows()
	{
		String[] buttonsText = new String[mValues.length];
						
		for (int i = 0 ; i < mValues.length ; i++)
		{
			if ( mValues[mCurrentLine][i] == null )
			{
				buttonsText[i] = "";	
			}
			else
			{
				buttonsText[i] = mValues[mCurrentLine][i].toString();
			}
		}
		
		return buttonsText;
	}
	
	
	private void Calculate()
	{
		TokenOperand firstTokeOperand = mValues[mCurrentLine][mFirstOperandIndex];
		TokenOperand secondTokeOperand = mValues[mCurrentLine][mSecondOperandIndex];				
		
		TokenOperand newOperand = new TokenOperand(0);
		newOperand.mValue = mSelectedtokenOperator.Calculate( firstTokeOperand, secondTokeOperand);
		
		for (int i = 0 ; i < mValues.length ; i++)
		{
			mValues[mCurrentLine+1][i] = mValues[mCurrentLine][i];			
		}
		
		mValues[mCurrentLine+1][mFirstOperandIndex]=newOperand;
		mValues[mCurrentLine+1][mSecondOperandIndex]=null;
		
		
	}
	
	public boolean MoveToNextLineIfFinishe()
	{
		if ( mState == State.SECOND_OPERAND_SELECTED )
		{
			mCurrentLine++;				
			mState = State.NOTHING_WAS_SELECTED;
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean SelectOperand( int index )
	{
		if ( mState == State.NOTHING_WAS_SELECTED )
		{
			mFirstOperandIndex = index;
			mState = State.FIRST_OPERAND_SELECTED;
			
			return true;
		}
		else if ( mState == State.OPERATOR_SELECTED )
		{
			mSecondOperandIndex = index;
			
			try 
			{
				Calculate();
			}
			catch (Exception e) // probably divide by zero
			{
				return false;
			}

			mState = State.SECOND_OPERAND_SELECTED;
			
			return true;
		}
		
		return false;
	}
	
	public boolean SelectOperator( int index )
	{
		if ( mState == State.FIRST_OPERAND_SELECTED )
		{
			mSelectedOperator = index;
			mState = State.OPERATOR_SELECTED;
			
			switch (mSelectedOperator)
			{
			case 0:
				mSelectedtokenOperator = new OperatorPlus();
				break;
				
			case 1:
				mSelectedtokenOperator = new OperatorMinus();
				break;
				
			case 2:
				mSelectedtokenOperator = new OperatorMult();
				break;
				
			case 3:
				mSelectedtokenOperator = new OperatorDiv();
				break;
				
			default:
				throw new ArithmeticException("bad operator");				
			}			
			
			return true;
		}
		
		return false;
	}
	
	public String ToString()
	{		
		switch ( mState )
		{
		case NOTHING_WAS_SELECTED:
			return "";
			
		case FIRST_OPERAND_SELECTED:
			return mValues[mCurrentLine][mFirstOperandIndex].toString();
				
		case OPERATOR_SELECTED:
			return mValues[mCurrentLine][mFirstOperandIndex].toString() +  
			mSelectedtokenOperator.toString();
		
		case SECOND_OPERAND_SELECTED:
			return mValues[mCurrentLine][mFirstOperandIndex].toString() +  
			mSelectedtokenOperator.toString() + 
			mValues[mCurrentLine][mSecondOperandIndex].toString() + "="
			+ mSelectedtokenOperator.GetCalculatedValue().toString();
		default:
			return "";				
		}
	}
	
	public int GetLineNumber()
	{
		return mCurrentLine;
	}
	
	public void CancelLastMove()
	{
		if ( mState == State.NOTHING_WAS_SELECTED )
		{
			mCurrentLine= Math.max(0,mCurrentLine-1);
		}
		else
		{
			mState = State.NOTHING_WAS_SELECTED;
		}
	}
	
	public int GetNumOfOperators()
	{
		return mValues.length-1;
	}
	

}
