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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.sp.moveee.R;



/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link StepServiceController}
 * and {@link StepServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class StepService extends Service {
	private static final String TAG = "pedometer.StepService";
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;

    private SharedPreferences mState;
    private SharedPreferences.Editor mStateEditor;

    private Utils mUtils;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private StepDetector mStepDetector;
    // private StepBuzzer mStepBuzzer; // used for debugging
    private StepDisplayer mStepDisplayer;


    private PowerManager.WakeLock wakeLock;
    private NotificationManager mNM;

    private int mSteps;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SERVICE: ","CREATE");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        
        // Load settings
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        mState = getSharedPreferences("state", 0);

        mUtils = Utils.getInstance();
        mUtils.setService(this);
        acquireWakeLock();
        
        // Start detecting
        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        // Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
        // code be called whenever the phone enters standby mode.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        mStepDisplayer = new StepDisplayer(mPedometerSettings, mUtils);
        mStepDisplayer.setSteps(mSteps = mState.getInt("steps", 0));
        mStepDisplayer.addListener(mStepListener);
        mStepDetector.addStepListener(mStepDisplayer);



        // Used when debugging:
        // mStepBuzzer = new StepBuzzer(this);
        // mStepDetector.addStepListener(mStepBuzzer);

        // Start voice
        reloadSettings();

    }
    
    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {

        Log.i("SERVICE: ","DESTROY");
        // Unregister our receiver.
        unregisterReceiver(mReceiver);
        unregisterDetector();
        
        mStateEditor = mState.edit();
        mStateEditor.putInt("steps", mSteps);
        mStateEditor.commit();
        
        mNM.cancel(R.string.app_name);

        wakeLock.release();
        
        super.onDestroy();
        
        // Stop detecting
        mSensorManager.unregisterListener(mStepDetector);

        /*Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);*/

    }

    private void registerDetector() {
        mSensor = mSensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER /*|
            Sensor.TYPE_MAGNETIC_FIELD | 
            Sensor.TYPE_ORIENTATION*/);
        mSensorManager.registerListener(mStepDetector,
            mSensor,
            SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);
    }
    
    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;

    }
    

    
    public void reloadSettings() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        if (mStepDetector != null) {
            mStepDetector.setSensitivity(
                    Float.valueOf(mSettings.getString("sensitivity", "20"))
            );
        }

        if (mStepDisplayer    != null) mStepDisplayer.reloadSettings();
    }
    
    public void resetValues() {
        mStepDisplayer.setSteps(0);
    }
    
    /**
     * Forwards pace values from PaceNotifier to the activity. 
     */
    private StepDisplayer.Listener mStepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            mSteps = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(mSteps);
            }
        }
    };


    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        Intent pedometerIntent = new Intent();
        pedometerIntent.setComponent(new ComponentName(this, Pedometer.class));
        pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                pedometerIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.eee60thlogo3)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_subtitle))
                        .setOngoing(true)
                        .setContentIntent(contentIntent);



        mNM.notify(R.string.app_name, mBuilder.build());
    }


    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                StepService.this.unregisterDetector();
                StepService.this.registerDetector();
                acquireWakeLock();
            }
        }
    };

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags;
        wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        wakeLock = pm.newWakeLock(wakeFlags, TAG);
        wakeLock.acquire();
    }

}

