package com.shirbi.ticktaksolver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
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

    public void showLoseMessage(String[] strArray) {
        for (int id : partResultsTextViews) {
            TextView myTextView = (TextView)findViewById(id);
            myTextView.setTextColor(m_activity.getResources().getColor(R.color.white));
            myTextView.setText("");
        }

        for (int i = 1; i < strArray.length; i++) {
            TextView partResultTextView = (TextView)(findViewById(partResultsTextViews[i-1]));
            partResultTextView.setText(strArray[i]);
            partResultTextView.setTextColor(m_activity.getResources().getColor(R.color.yellow));
        }

        TextView myTextView = (TextView)(findViewById(R.id.EquationResult));
        myTextView.setText(R.string.lose);

        setTryAloneFieldsVisibility(false);
        findViewById(R.id.cancelLastMoveButton).setEnabled(false);
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

    public void showSourcesAndTargetNumbers(int numbersSources[], int target) {
        for (int i = 0; i < sources.length; i++) {
            EditText editText = (EditText) findViewById(sources[i]);
            if (i < numbersSources.length) {
                editText.setText(String.valueOf(numbersSources[i]));
            } else {
                editText.setText("");
            }
        }

        EditText editText = (EditText) findViewById(R.id.target);
        editText.setText(String.valueOf(target));
    }

    public void setManualFieldsEnable(boolean isEnable) {
        for (int source : sources) {
            EditText editText = (EditText) findViewById(source);
            editText.setEnabled(isEnable);
        }

        EditText editText = (EditText) findViewById(R.id.target);
        editText.setEnabled(isEnable);
    }

    public void showExitDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(m_activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(m_activity);
        }
        builder.setTitle(m_activity.getString(R.string.exit_game));
        builder.setPositiveButton(m_activity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_activity.Exit();
            }
        });
        builder.setNegativeButton(m_activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        //builder.setIcon(R.drawable.new_game_icon); // TODO: Add this
        builder.show();
    }

    public void setSettingShow(boolean isShow) {
        if (isShow) {
            findViewById(R.id.gameLayout).setVisibility(View.GONE);
            findViewById(R.id.setting_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.gameLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.setting_layout).setVisibility(View.GONE);
        }
    }

    public void setWindowForShare(boolean isShare) {
        if (isShare) {
            for (int id : m_views_to_hide_for_share) {
                findViewById(id).setVisibility(View.GONE);
            }
        } else {
            for (int id : m_views_to_hide_for_share) {
                findViewById(id).setVisibility(View.VISIBLE);
            }
            setTryAloneFieldsVisibility(false);
        }
    }

    public void setEnabledButtonOnCpuCalculation(boolean isEnable) {
        int buttonsToEnable[] = {
                R.id.calculateButtonOneThread,
                R.id.cleanButton,
                R.id.easyButton,
                R.id.hardButton,
                R.id.manualButton,
                R.id.tryAloneButton,
                R.id.settingButton,
        };

        for (int id : buttonsToEnable) {
            Button button = (Button) findViewById(id);
            button.setEnabled(isEnable);
        }
    }

    public int[] readInputsForEditTexts() {
        int numOfValues = 0;
        int values[];
        for (int i = 0; i < sources.length; i++) {
            Button operandButton = (Button) findViewById(tryAloneOperandsButtons[i]);
            operandButton.setText("");
            operandButton.setVisibility(View.INVISIBLE);

            EditText editText = (EditText) findViewById(sources[i]);

            try {
                int currentValue = Integer.parseInt(editText.getText().toString());
                if (currentValue > 0) {
                    numOfValues++;
                }
            } catch (NumberFormatException e) {

            }
        }

        if (numOfValues == 0) {
            return null;
        }

        values = new int[numOfValues];

        int nextIndex = 0;
        for (int source : sources) {
            EditText editText = (EditText) findViewById(source);

            try {
                int currentValue = Integer.parseInt(editText.getText().toString());

                if (currentValue > 0) {
                    Button operandButton = (Button) findViewById(tryAloneOperandsButtons[nextIndex]);
                    operandButton.setText(String.valueOf(currentValue));
                    operandButton.setVisibility(View.VISIBLE);

                    values[nextIndex] = currentValue;
                    nextIndex++;
                }
            } catch (NumberFormatException e) {

            }
        }

        EditText editText = (EditText) findViewById(R.id.target);

        return values;
    }
}
