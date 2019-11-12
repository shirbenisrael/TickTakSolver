package com.shirbi.ticktaksolver;

import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

public class FrontEndHandler {
    MainActivity m_activity;

    static int partResultsTextViews[] = {R.id.partResult1, R.id.partResult2, R.id.partResult3, R.id.partResult4, R.id.partResult5};
    static int tryAloneOperandsButtons[] =
            {R.id.tryAlone1, R.id.tryAlone2, R.id.tryAlone3, R.id.tryAlone4, R.id.tryAlone5, R.id.tryAlone6};
    static int tryAloneOperatorsButtons[] =
            {R.id.tryAlonePlus, R.id.tryAloneMinus, R.id.tryAloneMult, R.id.tryAloneDiv};
    static TicktackSolver mTicktackSolver;
    int sources[] = {R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4, R.id.editText5, R.id.editText6};
    int m_views_to_hide_for_share[] = {R.id.levelLayout, R.id.startPlayLayout, R.id.tryAloneFields,
            R.id.bottomButtonsLayout, R.id.shareButton};

    private View findViewById(int id) {
        return m_activity.findViewById(id);
    }

    private void setSquareSizeView(View view, int size) {
        view.getLayoutParams().width = size;
        view.getLayoutParams().height = size;
    }

    private void SetButtonWidth(Button button, int width) {
        ViewGroup.LayoutParams params = button.getLayoutParams();
        params.width = width;
        button.setLayoutParams(params);
    }

    public FrontEndHandler(MainActivity activity) {
        m_activity = activity;

        m_activity.setContentView(R.layout.activity_main);

        Display display = m_activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        for (int source : sources) {
            EditText editText = (EditText)findViewById(source);
            editText.setWidth(width / 3);
        }

        ((Button) findViewById(R.id.tryAloneButton)).setWidth(width / 2);
        ((Button) findViewById(R.id.cancelLastMoveButton)).setWidth(width / 2);
        ((Button) findViewById(R.id.easyButton)).setWidth(width / 3);
        ((Button) findViewById(R.id.hardButton)).setWidth(width / 3);
        ((Button) findViewById(R.id.manualButton)).setWidth(width / 3);
        ((Button) findViewById(R.id.cleanButton)).setWidth(width / 3);
        ((Button) findViewById(R.id.calculateButtonOneThread)).setWidth(width / 3);
        ((Button) findViewById(R.id.settingButton)).setWidth(width / 3);

        setSquareSizeView(findViewById(R.id.target_icon), width / 6);
        ((EditText) findViewById(R.id.target)).setWidth(width / 6);
        setSquareSizeView(findViewById(R.id.steps_icon), width / 6);
        ((TextView) findViewById(R.id.playerCalcCounter)).setWidth(width / 6);
        setSquareSizeView(findViewById(R.id.best_result_icon), width / 6);
        ((TextView) findViewById(R.id.bestResult)).setWidth(width / 6);

        for (int id : tryAloneOperandsButtons) {
            Button button = (Button) findViewById(id);
            SetButtonWidth(button, width / 5);
        }

        for (int id : tryAloneOperatorsButtons) {
            Button button = (Button) findViewById(id);
            SetButtonWidth(button, width / 5);
        }
    }

    public void showLeftOperandsForSolveAlone(String[] buttonStrings) {
        for (int i = 0; i < buttonStrings.length; i++) {
            Button operandButton = (Button) findViewById(tryAloneOperandsButtons[i]);
            operandButton.setText(buttonStrings[i]);
            if (buttonStrings[i].equals("")) {
                operandButton.setVisibility(View.INVISIBLE);
            } else {
                operandButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showBestResult(Rational bestResult) {
        String bestResultStr = bestResult.toStringWithoutBrackets();
        Boolean isFraction = bestResult.IsFraction();
        TextView textView = ((TextView) findViewById(R.id.bestResult));

        Typeface typeface = ResourcesCompat.getFont(m_activity, R.font.nutso2);
        textView.setTypeface(typeface);

        if (isFraction) {
            textView.setFontFeatureSettings("afrc");
        } else {
            textView.setFontFeatureSettings("");
        }
        textView.setText(bestResultStr);
    }

    public void showWinMessage() {
        TextView myTextView =
                (TextView) (findViewById(R.id.EquationResult));
        myTextView.setText(R.string.WinMessage);

        Button cancelLastMoveButton = (Button) findViewById(R.id.cancelLastMoveButton);
        cancelLastMoveButton.setEnabled(false);

        for (int id : partResultsTextViews) {
            myTextView = (TextView) findViewById(id);
            myTextView.setTextColor(m_activity.getResources().getColor(R.color.red));
        }

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);
        chronometer.setVisibility(View.VISIBLE);

        View shareButton = (View) findViewById(R.id.shareButton);
        shareButton.setVisibility(View.VISIBLE);
    }

    private void setTryAloneFieldsVisibility(boolean is_visible) {
        View tryAloneFields = findViewById(R.id.tryAloneFields);
        tryAloneFields.setVisibility(is_visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void cleanResults() {
        ((TextView) findViewById(R.id.bestResult)).setText("-");
        ((TextView) findViewById(R.id.playerCalcCounter)).setText("0");

        for (int id : partResultsTextViews) {
            TextView myTextView = (TextView) findViewById(id);
            myTextView.setTextColor(m_activity.getResources().getColor(R.color.white));
            myTextView.setText("");
        }

        TextView myTextView =
                (TextView) (findViewById(R.id.EquationResult));
        myTextView.setText(R.string.hello_world);

        setTryAloneFieldsVisibility(false);

        findViewById(R.id.cancelLastMoveButton).setEnabled(false);
    }
}
