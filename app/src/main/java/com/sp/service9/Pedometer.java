/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sp.service9;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.sp.moveee.R;


public class Pedometer extends Activity implements AppCompatCallback {
    private static final String TAG = "Pedometer";
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private Utils mUtils;

    private TextView theusername;
    private TextView mStepValueView;
    private int mStepValue;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    public int count = 0;
    ProgressBar mprogressBar;

    String name;
    private AppCompatDelegate delegate;

    final Context context = this;
    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    private SharedPreferences mState;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i("ACTIVITY: ","CREATE");
        mState = getSharedPreferences("state", 0);
        mStepValue = mState.getInt("steps", 0);


        mUtils = Utils.getInstance();

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_main);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        mprogressBar = (ProgressBar) findViewById(R.id.circular_progress_bar);

        sharedPreferences = getSharedPreferences("counter",Context.MODE_PRIVATE);

        if(mStepValue >= sharedPreferences.getInt("max",60))
        {
            if(count == 0)
            {
                fab.setVisibility(View.VISIBLE);
            }
            else
            {
                fab.setVisibility(View.INVISIBLE);
            }

        }



    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i("ACTIVITY: ","RESUME");
        super.onResume();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);


        mIsRunning = mPedometerSettings.isServiceRunning();

        mStepValueView     = (TextView) findViewById(R.id.step_value);

        TextView steptracker   = (TextView)findViewById(R.id.StepTracker);
        String text1 = "<font COLOR=\'BLACK\'><b>" + "MOV" + "</b></font>"
                + "<font COLOR=\'#00aeef\'><b>" + "EEE" + "</b></font>";
        steptracker.setText(Html.fromHtml(text1));

        theusername = (TextView)findViewById(R.id.textView2);
        sharedPreferences = getSharedPreferences("counter",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        name = sharedPreferences.getString("KEY","");
        count = sharedPreferences.getInt("claim",0);
        theusername.setText(name+".");

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        mprogressBar = (ProgressBar) findViewById(R.id.circular_progress_bar);

        mprogressBar.setMax(sharedPreferences.getInt("max",60));

        if (mStepValue >= sharedPreferences.getInt("max",60))
        {
            mStepValueView.setText(String.valueOf(sharedPreferences.getInt("max",60)));
            mprogressBar.setProgress(sharedPreferences.getInt("max",60));
            if(count == 0)
            {
                fab.setVisibility(View.VISIBLE);
            }
            else
            {
                fab.setVisibility(View.INVISIBLE);
            }

        }
        else
        {
            mStepValueView.setText(String.valueOf(mStepValue));
            mprogressBar.setProgress(mStepValue);
            fab.setVisibility(View.INVISIBLE);
        }

        final ToggleButton start = (ToggleButton) findViewById(R.id.button);
        if(mIsRunning)
        {
            start.setBackgroundColor(getResources().getColor(R.color.red));
            start.setChecked(true);
            startStepService();
            bindStepService();
        }
        else
        {
            start.setBackgroundColor(getResources().getColor(R.color.green));
            start.setChecked(false);
        }




        start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startStepService();
                    bindStepService();
                    start.setBackgroundColor(getResources().getColor(R.color.red));


                } else {
                    unbindStepService();
                    stopStepService();
                    start.setBackgroundColor(getResources().getColor(R.color.green));


                }
            }
        });



    }

    @Override
    protected void onPause() {
        Log.i("ACTIVITY: ","PAUSE");
        if (mIsRunning) {
            unbindStepService();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {

        Log.i("ACTIVITY: ","STOP");
        editor.putInt("claim", count);
        editor.commit();
        editor.putInt("max", sharedPreferences.getInt("max",60));
        editor.commit();
        editor.putString("KEY",name);
        editor.commit();

        mPedometerSettings.clearServiceRunning(mIsRunning);

        super.onStop();
    }

    protected void onDestroy() {
        Log.i("ACTIVITY: ","DESTROY");
        super.onDestroy();

        if(mIsRunning)
        {
            stopStepService();
            startStepService();
        }

    }

    protected void onRestart() {

        super.onDestroy();
    }

    private StepService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();

            mService.registerCallback(mCallback);
            mService.reloadSettings();

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    private void startStepService() {
        if (! mIsRunning) {

            mIsRunning = true;
            startService(new Intent(Pedometer.this,
                    StepService.class));
        }
    }

    private void bindStepService() {

        bindService(new Intent(Pedometer.this,
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {

        unbindService(mConnection);
    }

    private void stopStepService() {

        if (mService != null) {

            stopService(new Intent(Pedometer.this,
                    StepService.class));
        }
        mIsRunning = false;
    }

    private void resetValues(boolean updateDisplay) {

        editor.putInt("claim", count);
        editor.commit();

        if (mService != null && mIsRunning) {
            mService.resetValues();
            mprogressBar.setProgress(0);
        }
        else {
            mStepValueView.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.commit();
            }
            mprogressBar.setProgress(0);
        }
    }

    private static final int MENU_SETTINGS = 8;
    // private static final int MENU_QUIT     = 9;

    // private static final int MENU_PAUSE = 1;
    // private static final int MENU_RESUME = 2;
    private static final int MENU_RESET = 3;
    private static final int MENU_CREDITS = 4;

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        /*if (mIsRunning) {
            menu.add(0, MENU_PAUSE, 0, R.string.pause)
                    .setIcon(android.R.drawable.ic_media_pause)
                    .setShortcut('1', 'p');
        }
        else {
            menu.add(0, MENU_RESUME, 0, R.string.resume)
                    .setIcon(android.R.drawable.ic_media_play)
                    .setShortcut('1', 'p');
        }*/
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setShortcut('2', 'r');
        menu.add(0, MENU_SETTINGS, 0, R.string.settings)
                .setIcon(R.drawable.settings)
                .setShortcut('8', 's');
        menu.add(0,MENU_CREDITS,0,R.string.credits)
                .setIcon(R.drawable.iconinfo)
                .setShortcut('4','c');

        return true;
    }


    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          /*  case MENU_PAUSE:
                unbindStepService();
                stopStepService();
                mScreen = (LinearLayout) findViewById(R.id.main);
                mScreen.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                return true;
            case MENU_RESUME:
                startStepService();
                bindStepService();
                mScreen = (LinearLayout) findViewById(R.id.main);
                mScreen.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                return true;*/

            case MENU_SETTINGS:
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        String value1 = userInput.getText().toString();
                                        int finalValue = Integer.parseInt(value1);
                                        sharedPreferences = getSharedPreferences("counter", Context.MODE_PRIVATE);
                                        editor = sharedPreferences.edit();

                                        editor.putInt("max", finalValue);
                                        editor.commit();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            case MENU_RESET:
                resetValues(true);
                return true;
            case MENU_CREDITS:
                AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
                myAlert.setMessage("Done by : Chan Chung Loong and Min Hein Aung from DCPE").setPositiveButton("Continue..", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }) .setTitle("Credits") .setIcon(R.drawable.iconinfoblack).create();
                myAlert.show();
                return true;

        }
        return false;
    }

    /* Creates the menu items */
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
    };

    private static final int STEPS_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    mprogressBar.setProgress(mStepValue);
                    mprogressBar.setMax(sharedPreferences.getInt("max",60));
                    sharedPreferences = getSharedPreferences("counter",Context.MODE_PRIVATE);
                    if (mStepValue == sharedPreferences.getInt("max",60))
                    {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        if(count == 0)
                        {
                            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                            fab.setVisibility(View.VISIBLE);
                        }

                    }
                    else if (mStepValue >= sharedPreferences.getInt("max",60))
                    {
                        mStepValueView.setText(String.valueOf(sharedPreferences.getInt("max",60)));
                        mprogressBar.setProgress(sharedPreferences.getInt("max",60));

                        if(count == 0)
                        {
                            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                            fab.setVisibility(View.VISIBLE);
                        }

                    }
                    else
                    {
                        mStepValueView.setText(String.valueOf(mStepValue));
                        mprogressBar.setProgress(mStepValue);
                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };


    public void showAlert(View view)
    {
        if(count == 0)
        {
            ImageView image = new ImageView(this);
            image.setImageResource(R.drawable.tshirt);

            AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
            myAlert.setMessage("\n \nShow this message to collect your free T-Shirt! \n \n (*NOTE* Only press claim if you are at the T-shirt booth) \n \n").setPositiveButton("Claim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
                    fab.setVisibility(View.GONE);
                    count++;
                    editor.putInt("claim",count).commit();
                    dialog.dismiss();
                }
            }) .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setTitle("T-Shirt Collection") .setView(image) .setIcon(R.drawable.tshirt).create();
            myAlert.show();
        }

    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}