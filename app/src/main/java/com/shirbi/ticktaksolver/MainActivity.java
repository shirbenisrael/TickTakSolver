package com.shirbi.ticktaksolver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import com.shirbi.ticktaksolver.TicktackSolver.MessageType;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;

public class MainActivity extends Activity {

	private static final String TAG = "ticktacksolver";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		for (int source: sources)
		{
			EditText editText = (EditText)findViewById(source);
			editText.setWidth( width/3 );			
		}	
				
		((Button)findViewById( R.id.tryAloneButton )).setWidth(width/2);
		((Button)findViewById( R.id.cancelLastMoveButton )).setWidth(width/2);
		((Button)findViewById( R.id.easyButton )).setWidth(width/3);
		((Button)findViewById( R.id.hardButton )).setWidth(width/3);
		((Button)findViewById( R.id.manualButton )).setWidth(width/3);
		((Button)findViewById( R.id.cleanButton )).setWidth(width/2);		
		((Button)findViewById( R.id.calculateButtonOneThread )).setWidth(width/2);
		((EditText)findViewById( R.id.target )).setWidth(width/2);
		((TextView)findViewById( R.id.bestResult )).setWidth(width/2);
		
		for ( int id: tryAloneOperandsButtons)
		{
			Button button = (Button)findViewById(id);
			SetButtonWidth( button, width/5);
		}
		
		for ( int id: tryAloneOperatorsButtons)
		{
			Button button = (Button) findViewById(id);
			SetButtonWidth( button, width/5);
		}
	}

	int sources[] = { R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4, R.id.editText5, R.id.editText6};
	static int partResultsTextViews[] = { R.id.partResult1, R.id.partResult2, R.id.partResult3, R.id.partResult4, R.id.partResult5 };
	
	static int tryAloneOperandsButtons[] = 
		{ R.id.tryAlone1, R.id.tryAlone2, R.id.tryAlone3, R.id.tryAlone4, R.id.tryAlone5, R.id.tryAlone6 };
		
	static int tryAloneOperatorsButtons[] =
		{R.id.tryAlonePlus, R.id.tryAloneMinus, R.id.tryAloneMult, R.id.tryAloneDiv };	
	
	static TicktackSolver mTicktackSolver;
	
	private SolveAlone mSolveAlone;

	private void SetButtonWidth( Button button, int width ) {
		ViewGroup.LayoutParams params = button.getLayoutParams();
		params.width = width;
		button.setLayoutParams(params);
	}

	private void stopClock()
	{
		Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);		
		chronometer.stop();
	}
	
	/**
	 * Show the remain operands that the user can use when he try to solve alone.
	 * Should be called when the user click on operand or when he click on on cancel last move. 
	 */
	private void showLeftOperandsForSolveAlone()
	{
		String[] buttonStrings = mSolveAlone.GetButtonToShows();
		
		int numOfOperandLeft = 0;
		String lastOperandValue = null;
		
		for ( int i = 0 ; i < buttonStrings.length ; i ++ )
		{
			Button operandButton = (Button)findViewById(tryAloneOperandsButtons[i]);
			operandButton.setText(buttonStrings[i]);
			if ( buttonStrings[i].equals("") )
			{
				operandButton.setVisibility(View.INVISIBLE);
			}
			else
			{
				operandButton.setVisibility(View.VISIBLE);
				numOfOperandLeft++;
				lastOperandValue = buttonStrings[i];
			}
		}
		
		if ( numOfOperandLeft == 1 )
		{
			String bestResultStr = getString(R.string.BestResult) + ": " + mSolveAlone.GetBestResult();;
			((TextView)findViewById(R.id.bestResult)).setText(bestResultStr);

			if ( lastOperandValue.equals( String.valueOf(mTarget) ) )
			{
				TextView myTextView = 
						(TextView)(findViewById(R.id.EquationResult));
				myTextView.setText(R.string.WinMessage);
				
				Button cancelLastMoveButton = (Button)findViewById(R.id.cancelLastMoveButton );
				cancelLastMoveButton.setEnabled( false );
				
				for ( int id : partResultsTextViews)
				{
					myTextView = (TextView)findViewById(id);
					myTextView.setTextColor(getResources().getColor(R.color.red));
				}

				Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);
				chronometer.setVisibility(View.VISIBLE);

				View shareButton = (View) findViewById(R.id.shareButton);
				shareButton.setVisibility(View.VISIBLE);

				stopClock();
			}
		}
	}
	
	private void showRandomNumbers( int numbersSources[], int target )
	{
		for ( int i = 0 ; i < sources.length ; i++)
		{					
			EditText editText = (EditText) findViewById( sources[i]);
			editText.setText(String.valueOf(numbersSources[i]));			
		}
		
		EditText editText = (EditText) findViewById( R.id.target);
		editText.setText( String.valueOf( target ) );
		
		cleanResults();
		
		setManualEnable( false );
	}
	
	private void enableTryAloneButton()
	{
		findViewById(R.id.tryAloneButton).setEnabled(true);
	}
	
	public void setManualEnable( boolean isEnable )
	{
		for ( int source : sources)
		{					
			EditText editText = (EditText) findViewById(source);
			editText.setEnabled(isEnable);									
		}
		
		EditText editText = (EditText)findViewById( R.id.target );
		editText.setEnabled(isEnable);

		showSourcesAndHideClock(true);		
	}
	
	public void onManualButtonClick( View view)
	{
		cleanResults();
		setManualEnable( true );	
		
		enableTryAloneButton();
	}
	
	public void onEasyButtonClick( View view)
	{
		Random rand = new Random();		
		
		int numbers[] = new int [sources.length];
		for ( int i = 0 ; i < numbers.length ; i++)
		{
			boolean foundNewRand = false;
			
			while (!foundNewRand)
			{
				foundNewRand = true;
				int  n = rand.nextInt(9) + 1;
				numbers[i] = n;
				
				for ( int j = 0 ; j < i ; j++ )
				{
					if ( numbers[j] == n )
					{
						foundNewRand = false;
					}						
				}
			}			
		}		
		
		int target =  rand.nextInt(90) + 10;
		
		showRandomNumbers(numbers, target);
		
		enableTryAloneButton();
	}
	
	public void onHardButtonClick( View view)
	{
		Random rand = new Random();		
		
		int numbers[] = new int [sources.length];
		for ( int i = 0 ; i < numbers.length-1 ; i++)
		{
			boolean foundNewRand = false;
			
			while (!foundNewRand)
			{
				foundNewRand = true;
				int  n = rand.nextInt(9) + 1;
				numbers[i] = n;
				
				for ( int j = 0 ; j < i ; j++ )
				{
					if ( numbers[j] == n )
					{
						foundNewRand = false;
					}						
				}
			}			
		}
		numbers[numbers.length-1 ] = ( rand.nextInt(4) + 1 ) * 25;
		
		int target =  rand.nextInt(900) + 100;
		
		showRandomNumbers(numbers, target);
		
		enableTryAloneButton();
	}
	
	public void onTryAloneOperandClick( View view)
	{
		if ( mSolveAlone.GetLineNumber() >= mSolveAlone.GetNumOfOperators() )
		{
			return;
		}
		
		int i;
		for ( i = 0 ; i < tryAloneOperandsButtons.length ; i ++ )
		{
			if ( view == findViewById(tryAloneOperandsButtons[i]))
			{
				break;
			}
		}
		
		if ( mSolveAlone.SelectOperand(i) == false )
		{
			return;
		}
		
		String formulaToShow = mSolveAlone.ToString();
		int lineIndex = mSolveAlone.GetLineNumber();
		
		TextView myTextView = (TextView) findViewById( partResultsTextViews[lineIndex]);
		
		myTextView.setText(formulaToShow);
		
		if (mSolveAlone.MoveToNextLineIfFinishe() )
		{		
			showLeftOperandsForSolveAlone();
		}
		else
		{
			view.setVisibility(View.INVISIBLE);
		}
				
		
		//view.setBackgroundColor(getResources().getColor(R.color.maroon));
	}
	
	public void onTryAloneOperatorClick( View view)
	{
		int i;
		for ( i = 0 ; i < tryAloneOperatorsButtons.length ; i ++ )
		{
			if ( view == findViewById(tryAloneOperatorsButtons[i]))
			{
				break;
			}
		}
		
		if (! mSolveAlone.SelectOperator(i) )
		{
			return;
		}
		
		String formulaToShow = mSolveAlone.ToString();
		int lineIndex = mSolveAlone.GetLineNumber();
		
		TextView myTextView = (TextView) findViewById( partResultsTextViews[lineIndex]);
		
		myTextView.setText(formulaToShow);						
		
		//view.setBackgroundColor(getResources().getColor(R.color.olive));
	}
	
	private void cleanResults()
	{
		((TextView)findViewById(R.id.bestResult)).setText("");

		for (int id :partResultsTextViews)
		{
			TextView myTextView = (TextView)findViewById(id);
			myTextView.setTextColor(getResources().getColor(R.color.white));
			myTextView.setText("");							
		}
		
		TextView myTextView = 
				(TextView)(findViewById(R.id.EquationResult));
		myTextView.setText(R.string.hello_world);
	
		findViewById( R.id.tryAloneFields).setVisibility(View.INVISIBLE);
		
		findViewById( R.id.cancelLastMoveButton).setEnabled(false);
		
		stopClock();
	}
	
	public void onCancelLastMoveClick( View view)
	{
		// Remove non finished line.
		TextView myTextView;
			
		if (mSolveAlone == null )
		{
			return;
		}
		
		mSolveAlone.CancelLastMove();
		
		myTextView = (TextView) findViewById( partResultsTextViews[ mSolveAlone.GetLineNumber() ] );
		myTextView.setText("");			
		
		showLeftOperandsForSolveAlone();
	}

	public void onTryAloneButtonClick( View view)
	{
		if (!readInputs())
		{
			return;
		}
		
		cleanResults();
		
		view.setEnabled(false);
		
		View tryAloneFields = findViewById(R.id.tryAloneFields);
		tryAloneFields.setVisibility(View.VISIBLE);
		
		showSourcesAndHideClock(false);
				
		View targetText = findViewById(R.id.target);
		targetText.setEnabled(false);
		
		Button cancelLastMoveButton = (Button)findViewById(R.id.cancelLastMoveButton );
		cancelLastMoveButton.setEnabled( true );
		
		mSolveAlone = new SolveAlone(mValues, mTarget);
		
		Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);

		View shareButton = (View) findViewById(R.id.shareButton);
		shareButton.setVisibility(View.INVISIBLE);
		
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
	}
	
	private void showSourcesAndHideClock( boolean isSourcesShow)
	{
		View sourcesFields = findViewById(R.id.sourcesLayout);
		Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer1);
		CheckBox checkBox = (CheckBox) findViewById(R.id.ShowStopper);
		View shareButton = (View) findViewById(R.id.shareButton);

		if ( isSourcesShow )
		{			
			sourcesFields.setVisibility(View.VISIBLE);
			chronometer.setVisibility(View.INVISIBLE);
			checkBox.setVisibility(View.INVISIBLE);
			shareButton.setVisibility(View.INVISIBLE);
		}
		else
		{	
			chronometer.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.INVISIBLE);
			checkBox.setVisibility(View.VISIBLE);
			sourcesFields.setVisibility(View.INVISIBLE);
			shareButton.setVisibility(View.INVISIBLE);
		}
	}

	public void onShowStopperClicked( View view)
	{
		CheckBox checkBox = (CheckBox) findViewById(R.id.ShowStopper);
		Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer1);

		chronometer.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.INVISIBLE);
	}

	public void onCleanButtonClick( View view)
	{
		for ( int source : sources)
		{
			EditText editText = (EditText) findViewById( source);
			editText.setText("");			
		}		
		
		View sourcesFields = findViewById(R.id.sourcesLayout);
		showSourcesAndHideClock(false);		
		
		findViewById(R.id.target).setEnabled(false);
		
		EditText editText = (EditText) findViewById( R.id.target);
		editText.setText("");
		
		cleanResults();
	}

	private int[] mValues;
	private int mTarget;

	private boolean readInputs()
	{
		int numOfValues = 0;
		for ( int i = 0 ;i < sources.length ; i ++)
		{
			Button operandButton = (Button)findViewById( tryAloneOperandsButtons[i] );			
			operandButton.setText("");
			operandButton.setVisibility(View.INVISIBLE);
			
			EditText editText = (EditText) findViewById( sources[i]);
			
			try
			{
				int currentValue = Integer.parseInt( editText.getText().toString() );
				if ( currentValue > 0 )
				{
					numOfValues++;
				}
			} catch(NumberFormatException e)
			{
				  
			}
		}
		
		if ( numOfValues == 0 )
		{
			return false;
		}
		mValues = new int[numOfValues];
		
		int nextIndex = 0;
		for (int source : sources) {
			EditText editText = (EditText) findViewById(source);

			try {
				int currentValue = Integer.parseInt(editText.getText().toString());

				if (currentValue > 0) {
					Button operandButton = (Button) findViewById(tryAloneOperandsButtons[nextIndex]);
					operandButton.setText(String.valueOf(currentValue));
					operandButton.setVisibility(View.VISIBLE);

					mValues[nextIndex] = currentValue;
					nextIndex++;
				}
			} catch (NumberFormatException e) {

			}
		}
		
		EditText editText = (EditText) findViewById( R.id.target);
					
		try 
		{  
			mTarget = Integer.parseInt( editText.getText().toString() );
		} catch(NumberFormatException e) 
		{ 
			return false;
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		
		return true;
	}				
	
	public void onCalculateButtonClick( View view)
	{						
		if (!readInputs())
		{
			return;
		}
		
		View tryAloneFields = findViewById(R.id.tryAloneFields);
		tryAloneFields.setVisibility(View.INVISIBLE);
		
		cleanResults();

		enableButtonOnCpuCalculation(false);

		mTicktackSolver = new TicktackSolver(mValues, mTarget, handler);			
		
		Runnable runnable = new Runnable() {
	        public void run() {    
	        	boolean resultIsGood = mTicktackSolver.FindResult(); 
	        	
	        	Bundle bundle = new Bundle();
	        	
	        	String resultString = resultIsGood ? mTicktackSolver.mResultFormula.ToString() : "אין פיתרון";
	        	
	        	bundle.putString("myKey", resultString );	        	        	
	        	bundle.putInt("otherKey", MessageType.CALCULATION_FINISHED.ordinal() );
	        	
	        	Message msg = handler.obtainMessage();
	        	
	        	msg.setData(bundle);
	        	
	        	handler.sendMessage(msg);   
	        }
		};
		
		Thread mythread = new Thread(runnable);
		mythread.start();
		
	}

	public void enableButtonOnCpuCalculation(boolean isEnable) {
		int buttonsToEnable[] = {
				R.id.calculateButtonOneThread,
				R.id.cleanButton,
				R.id.easyButton,
				R.id.hardButton,
				R.id.manualButton,
				R.id.tryAloneButton,
		};

		for (int id : buttonsToEnable) {
			Button button = (Button)findViewById(id);
			button.setEnabled(isEnable);
		}
	}

	static class MessageHandler extends Handler
	{
		private final WeakReference<MainActivity> mMainActivity;
		
		public MessageHandler( MainActivity mainActivity)
		{
			mMainActivity = new WeakReference<MainActivity>(mainActivity);					
		}	
		
		@Override
		public void handleMessage(Message msg) {
			
			Bundle bundle = msg.getData();
				
			String string = bundle.getString("myKey");
			
			MessageType messageType =  MessageType.values()[bundle.getInt("otherKey")];

			MainActivity activity = mMainActivity.get();
			
			TextView myTextView = 
					(TextView)(activity.findViewById(R.id.EquationResult));
			myTextView.setText(string);
			
			if ( messageType == MessageType.CALCULATION_FINISHED )
			{
				activity.enableButtonOnCpuCalculation(true);

				if ( mTicktackSolver.mResultFormula != null )
				{								
					String lastString = null;
					
					for ( int i = 0 ; i < partResultsTextViews.length ; i++ )
					{						
						TextView partResultTextView = 
								(TextView)(activity.findViewById(partResultsTextViews[i]));
						
						String newString = mTicktackSolver.mResultFormula.ToString(i+1);
						
						if ( (lastString == null ) || (! (newString.equals(lastString ) ) ) )
						{
							partResultTextView.setText( newString );
							lastString = newString;
						}						
						else
						{
							partResultTextView.setText( "" );
						}
					}
				}
			}
		}
	}

	private static final int REQUEST_WRITE_STORAGE = 112;
	private static final int REQUEST_READ_STORAGE = 3;

	MainActivity m_activity;

	int m_views_to_hide_for_share[] = { R.id.levelLayout, R.id.startPlayLayout, R.id.tryAloneFields,
            R.id.bottomButtonsLayout, R.id.shareButton, R.id.ShowStopper};

	public void onShareButtonClick(View button) throws IOException {
		for (int id : m_views_to_hide_for_share) {
			findViewById(id).setVisibility(View.GONE);
		}

		m_activity = this;
		final View view = findViewById(R.id.fullWindow);
		ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();

		viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			public void onGlobalLayout() {
				ShareImage shareImage = new ShareImage(m_activity);
				shareImage.shareView(view);

				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				for (int id : m_views_to_hide_for_share) {
					findViewById(id).setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private MessageHandler handler = new MessageHandler( this );
}
