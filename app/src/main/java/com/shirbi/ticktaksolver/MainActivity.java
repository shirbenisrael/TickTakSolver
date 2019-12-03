package com.shirbi.ticktaksolver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shirbi.ticktaksolver.TicktackSolver.MessageType;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.shirbi.ticktaksolver.BluetoothChatService.TOAST;

public class MainActivity extends Activity {

    private static final String TAG = "ticktacksolver";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_READ_STORAGE = 3;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    static int partResultsTextViews[] = {R.id.partResult1, R.id.partResult2, R.id.partResult3, R.id.partResult4, R.id.partResult5};
    static int tryAloneOperandsButtons[] =
            {R.id.tryAlone1, R.id.tryAlone2, R.id.tryAlone3, R.id.tryAlone4, R.id.tryAlone5, R.id.tryAlone6};
    static int tryAloneOperatorsButtons[] =
            {R.id.tryAlonePlus, R.id.tryAloneMinus, R.id.tryAloneMult, R.id.tryAloneDiv};
    static TicktackSolver mTicktackSolver;
    int sources[] = {R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4, R.id.editText5, R.id.editText6};
    MainActivity m_activity;
    int m_views_to_hide_for_share[] = {R.id.startPlayLayout, R.id.tryAloneFields,
            R.id.bottomButtonsLayout, R.id.shareButton};
    private SolveAlone mSolveAlone;
    private int[] mValues;
    private int mTarget;
    private int mPlayerCalcCounter;
    private MessageHandler handler = new MessageHandler(this);
    private BluetoothAdapter mBluetoothAdapter = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    private BluetoothChatService mChatService = null;
    private IncomingHandler mHandler = new IncomingHandler(this);
    private Boolean mTwoPlayerGame = false;
    private FrontEndHandler mFrontEndHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFrontEndHandler = new FrontEndHandler(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        RestoreState();
    }

    private void stopClock() {
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);
        chronometer.stop();
    }

    /**
     * Show the remain operands that the user can use when he try to solve alone.
     * Should be called when the user click on operand or when he click on on cancel last move.
     */
    private void showLeftOperandsForSolveAlone() {
        String[] buttonStrings = mSolveAlone.GetButtonToShows();

        mFrontEndHandler.showLeftOperandsForSolveAlone(buttonStrings);

        int numOfOperandLeft = 0;
        String lastOperandValue = null;

        for (int i = 0; i < buttonStrings.length; i++) {
            if (!buttonStrings[i].equals("")) {
                numOfOperandLeft++;
                lastOperandValue = buttonStrings[i];
            }
        }

        setTryAloneFieldsVisibility(numOfOperandLeft != 1);

        if (numOfOperandLeft == 1) {
            Rational bestResult = mSolveAlone.GetBestResult();
            mFrontEndHandler.showBestResult(bestResult);

            if (lastOperandValue.equals(String.valueOf(mTarget))) {
                mFrontEndHandler.showWinMessage();

                stopClock();

                if (mTwoPlayerGame) {
                    sendWinMessageToOtherPlayer();
                }

                PlaySound(R.raw.win);
            }
        } else {
            if (mSolveAlone.IsLastPartResultMatchTarget()) {
                mFrontEndHandler.ShowToast(R.string.use_all_numbers);
            }
        }
    }

    private void showRandomNumbers(int numbersSources[], int target) {
        mFrontEndHandler.showSourcesAndTargetNumbers(numbersSources, target);

        cleanResults();

        mFrontEndHandler.setManualFieldsEnable(false);

        showSourcesAndHideClock(true);
    }

    private void enableTryAloneButton() {
        findViewById(R.id.startPlayingButton).setEnabled(true);
    }

    public void startManualGame() {
        cleanResults();
        mFrontEndHandler.setManualFieldsEnable(true);
        showSourcesAndHideClock(true);
        enableTryAloneButton();
    }

    public void onNewGameButtonClick(View view) {
        mFrontEndHandler.showNewGameDialog();
    }

    public void startEasyGame() {
        Random rand = new Random();

        int numbers[] = new int[sources.length];
        for (int i = 0; i < numbers.length; i++) {
            boolean foundNewRand = false;

            while (!foundNewRand) {
                foundNewRand = true;
                int n = rand.nextInt(9) + 1;
                numbers[i] = n;

                for (int j = 0; j < i; j++) {
                    if (numbers[j] == n) {
                        foundNewRand = false;
                    }
                }
            }
        }

        int target = rand.nextInt(90) + 10;

        showRandomNumbers(numbers, target);

        enableTryAloneButton();

        onStartPlayingButtonClick(findViewById(R.id.startPlayingButton));
    }

    public void startHardGame() {
        Random rand = new Random();

        int numbers[] = new int[sources.length];
        for (int i = 0; i < numbers.length - 1; i++) {
            boolean foundNewRand = false;

            while (!foundNewRand) {
                foundNewRand = true;
                int n = rand.nextInt(9) + 1;
                numbers[i] = n;

                for (int j = 0; j < i; j++) {
                    if (numbers[j] == n) {
                        foundNewRand = false;
                    }
                }
            }
        }
        numbers[numbers.length - 1] = (rand.nextInt(4) + 1) * 25;

        int target = rand.nextInt(900) + 100;

        showRandomNumbers(numbers, target);

        enableTryAloneButton();

        onStartPlayingButtonClick(findViewById(R.id.startPlayingButton));
    }

    public void onTryAloneOperandClick(View view) {
        if (mSolveAlone.GetLineNumber() >= mSolveAlone.GetNumOfOperators()) {
            return;
        }

        int i;
        for (i = 0; i < tryAloneOperandsButtons.length; i++) {
            if (view == findViewById(tryAloneOperandsButtons[i])) {
                break;
            }
        }

        if (mSolveAlone.SelectOperand(i) == false) {
            return;
        }

        String formulaToShow = mSolveAlone.ToString();
        int lineIndex = mSolveAlone.GetLineNumber();

        mFrontEndHandler.showPartResult(lineIndex, formulaToShow);
        mFrontEndHandler.animatePartResult(lineIndex);

        if (mSolveAlone.MoveToNextLineIfFinishe()) {
            showLeftOperandsForSolveAlone();

            PlaySound(R.raw.simple_equation);

            mPlayerCalcCounter++;
            ((TextView) findViewById(R.id.playerCalcCounter)).setText(Integer.toString(mPlayerCalcCounter));
        } else {
            view.setVisibility(View.INVISIBLE);
        }

        //view.setBackgroundColor(getResources().getColor(R.color.maroon));
    }

    public void onTryAloneOperatorClick(View view) {
        int i;
        for (i = 0; i < tryAloneOperatorsButtons.length; i++) {
            if (view == findViewById(tryAloneOperatorsButtons[i])) {
                break;
            }
        }

        if (!mSolveAlone.SelectOperator(i)) {
            return;
        }

        String formulaToShow = mSolveAlone.ToString();
        int lineIndex = mSolveAlone.GetLineNumber();

        TextView myTextView = (TextView) findViewById(partResultsTextViews[lineIndex]);

        myTextView.setText(formulaToShow);
        mFrontEndHandler.animatePartResult(lineIndex);

        //view.setBackgroundColor(getResources().getColor(R.color.olive));
    }

    private void cleanResults() {
        mFrontEndHandler.cleanResults();

        stopClock();

        mPlayerCalcCounter = 0;
    }

    public void onCancelLastMoveClick(View view) {
        // Remove non finished line.
        if (mSolveAlone == null) {
            return;
        }

        mSolveAlone.CancelLastMove();

        mFrontEndHandler.cancelPartResult(mSolveAlone.GetLineNumber());

        String[] buttonStrings = mSolveAlone.GetButtonToShows();

        mFrontEndHandler.showLeftOperandsForSolveAlone(buttonStrings);

        setTryAloneFieldsVisibility(true);
    }

    private void sendWinMessageToOtherPlayer() {
        String message = String.valueOf(BLUETOOTH_MESSAGES.END_GAME);

        for (int i : partResultsTextViews) {
            TextView partResultTextView = (TextView) (findViewById(i));
            message = message + "," + partResultTextView.getText();
        }

        sendMessage(message);
    }

    private void sendNewGameMessage() {
        String message = String.valueOf(BLUETOOTH_MESSAGES.START_GAME);

        // Add the target.
        EditText editText = (EditText) findViewById(R.id.target);
        String numberStr = editText.getText().toString();
        message = message + "," + numberStr;

        // Add the sources.
        for (int source : sources) {
            editText = (EditText) findViewById(source);

            try {
                numberStr = editText.getText().toString();
                int currentValue = Integer.parseInt(numberStr);
                if (currentValue > 0) {
                    message = message + "," + numberStr;
                }
            } catch (NumberFormatException e) {

            }
        }

        sendMessage(message);
    }

    public void onStartPlayingButtonClick(View view) {
        if (startGame(view)) {
            if (mTwoPlayerGame) {
                sendNewGameMessage();
            }
        }
    }

    private void setTryAloneFieldsVisibility(boolean is_visible) {
        View tryAloneFields = findViewById(R.id.tryAloneFields);
        tryAloneFields.setVisibility(is_visible ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean startGame(View view) {
        if (!readInputs()) {
            return false;
        }

        cleanResults();

        view.setEnabled(false);

        setTryAloneFieldsVisibility(true);

        showSourcesAndHideClock(false);

        View targetText = findViewById(R.id.target);
        targetText.setEnabled(false);

        mSolveAlone = new SolveAlone(mValues, mTarget);

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);
        chronometer.setBase(SystemClock.elapsedRealtime());

        View shareButton = (View) findViewById(R.id.shareButton);
        shareButton.setVisibility(View.INVISIBLE);

        mFrontEndHandler.animateOperandsAndOperators();

        return true;
    }

    private Boolean IsStopperShown() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.ShowStopper);
        return checkBox.isChecked();
    }

    private void showSourcesAndHideClock(boolean isSourcesShow) {
        View sourcesFields = findViewById(R.id.sourcesLayout);
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer1);
        View shareButton = (View) findViewById(R.id.shareButton);

        if (isSourcesShow) {
            sourcesFields.setVisibility(View.VISIBLE);
            chronometer.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
        } else {
            chronometer.setVisibility(IsStopperShown() ? View.VISIBLE : View.INVISIBLE);
            sourcesFields.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
        }
    }

    private void cleanInputs() {
        for (int source : sources) {
            EditText editText = (EditText) findViewById(source);
            editText.setText("");
        }
    }

    public void onCleanButtonClick(View view) {
        cleanInputs();

        View sourcesFields = findViewById(R.id.sourcesLayout);
        showSourcesAndHideClock(false);

        findViewById(R.id.target).setEnabled(false);

        EditText editText = (EditText) findViewById(R.id.target);
        editText.setText("");

        cleanResults();
    }

    private boolean readInputs() {
        mValues = mFrontEndHandler.readInputsForEditTexts();

        if (mValues == null) {
            return false;
        }

        EditText editText = (EditText) findViewById(R.id.target);

        try {
            mTarget = Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            return false;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        return true;
    }

    public void onCalculateButtonClick(View view) {
        if (!readInputs()) {
            return;
        }

        setTryAloneFieldsVisibility(false);

        cleanResults();

        mFrontEndHandler.setEnabledButtonOnCpuCalculation(false);

        mTicktackSolver = new TicktackSolver(mValues, mTarget, handler);

        Runnable runnable = new Runnable() {
            public void run() {
                boolean resultIsGood = mTicktackSolver.FindResult();

                Bundle bundle = new Bundle();

                String resultString = resultIsGood ? mTicktackSolver.mResultFormula.ToString() :
                        getString(R.string.NoSolution);

                bundle.putString("myKey", resultString);
                bundle.putInt("otherKey", MessageType.CALCULATION_FINISHED.ordinal());

                Message msg = handler.obtainMessage();

                msg.setData(bundle);

                handler.sendMessage(msg);
            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    public void onShareButtonClick(View button) throws IOException {
        mFrontEndHandler.setWindowForShare(true);

        m_activity = this;
        final View view = findViewById(R.id.fullWindow);
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();

        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                ShareImage shareImage = new ShareImage(m_activity);
                shareImage.shareView(view);

                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mFrontEndHandler.setWindowForShare(false);
            }
        });
    }

    public void onSettingButtonClick(View view) {
        mFrontEndHandler.setSettingShow(true);
    }

    public void onBackFromSettingClick(View view) {
        mFrontEndHandler.setSettingShow(false);
    }

    private Boolean VerifyBlueToothEnabled() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return false;
        } else {
            return true;
        }
    }

    private void RunConnectActivity() {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void onConnectClick(View view) {
        if (VerifyBlueToothEnabled()) {
            RunConnectActivity();
        }
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void discoverable(View view) {
        ensureDiscoverable();
    }

    public void disconnect(View view) {
        // todo:
        // set disconnect. Copy from DownFall.
    }

    private void readInputsFromSecondPlayer( int intArray[]) {
        cleanInputs();

        int source_numbers[] = new int[intArray.length - 2];
        System.arraycopy(intArray, 2, source_numbers, 0, source_numbers.length);
        int target_number = intArray[1];

        showRandomNumbers(source_numbers, target_number);

        enableTryAloneButton();

        startGame(findViewById(R.id.startPlayingButton));
    }

    private void showLoseMessage(String[] strArray) {
        mFrontEndHandler.showLoseMessage(strArray);
        stopClock();

        PlaySound(R.raw.lose);
    }

    private void ParseMessage(String message) {
        String[] strArray = message.split(",");

        int messageType = Integer.parseInt(strArray[0]);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        switch (messageType) {
            // TODO: Handle messages
            case BLUETOOTH_MESSAGES.START_GAME:
                int[] intArray = new int[strArray.length];
                for (int i = 0; i < strArray.length; i++) {
                    intArray[i] = Integer.parseInt(strArray[i]);
                }
                readInputsFromSecondPlayer(intArray);
                break;
            case BLUETOOTH_MESSAGES.END_GAME:
                showLoseMessage(strArray);
                break;
            case BLUETOOTH_MESSAGES.GET_NUMBERS:
                break;
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BluetoothChatService.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
            case BluetoothChatService.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                ParseMessage(readMessage);
                break;
            case BluetoothChatService.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                mTwoPlayerGame = true;
                break;
            case BluetoothChatService.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device

                    if (mChatService == null) {
                        setupChat();
                    }

                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    RunConnectActivity();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void Exit() {
        super.onBackPressed();
    }

    public void onBackPressed() {
        if (findViewById(R.id.setting_layout).getVisibility() == View.VISIBLE) {
            onBackFromSettingClick(null);
            return;
        }

        mFrontEndHandler.showExitDialog();
    }

    private Boolean IsSoundEnable() {
        CheckBox enable_sound_check_box = (CheckBox)findViewById(R.id.enable_sound_checkbox);
        return enable_sound_check_box.isChecked();
    }

    // Set of all media players which are currently working. Used to prevent garbage collector from
    // clean them and stop the sounds.
    private Set<MediaPlayer> m_media_players = new HashSet<MediaPlayer>();

    public void PlaySound(int sound_id) {
        if (!IsSoundEnable()) {
            return;
        }

        MediaPlayer media_player;
        media_player = MediaPlayer.create(this, sound_id);
        m_media_players.add(media_player);
        media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                m_media_players.remove(mp);
            }
        });
        media_player.start();
    }

    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void RestoreState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        CheckBox enable_sound_check_box = (CheckBox)findViewById(R.id.enable_sound_checkbox);
        enable_sound_check_box.setChecked(sharedPref.getBoolean(getString(R.string.enable_sound), true));

        CheckBox show_stopper_check_box = (CheckBox)findViewById(R.id.ShowStopper);
        show_stopper_check_box.setChecked(sharedPref.getBoolean(getString(R.string.show_stopper), true));
    }

    private void StoreState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.enable_sound), IsSoundEnable());
        editor.putBoolean(getString(R.string.show_stopper), IsStopperShown());
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        StoreState();
        super.onDestroy();
    }

    static class MessageHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivity;

        public MessageHandler(MainActivity mainActivity) {
            mMainActivity = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            String string = bundle.getString("myKey");

            MessageType messageType = MessageType.values()[bundle.getInt("otherKey")];

            MainActivity activity = mMainActivity.get();

            TextView myTextView =
                    (TextView) (activity.findViewById(R.id.EquationResult));
            myTextView.setText(string);

            if (messageType == MessageType.CALCULATION_FINISHED) {
                activity.mFrontEndHandler.setEnabledButtonOnCpuCalculation(true);

                if (mTicktackSolver.mResultFormula != null) {
                    String lastString = null;

                    for (int i = 0; i < partResultsTextViews.length; i++) {
                        TextView partResultTextView =
                                (TextView) (activity.findViewById(partResultsTextViews[i]));

                        String newString = mTicktackSolver.mResultFormula.ToString(i + 1);

                        if ((lastString == null) || (!(newString.equals(lastString)))) {
                            partResultTextView.setText(newString);
                            lastString = newString;
                        } else {
                            partResultTextView.setText("");
                        }
                    }
                }
            }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> m_activity;

        IncomingHandler(MainActivity activity) {
            m_activity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = m_activity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    class BLUETOOTH_MESSAGES {
        static final int START_GAME = 0;
        static final int END_GAME = 1;
        static final int GET_NUMBERS = 2;
    }

}
