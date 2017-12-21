//package cs.umass.edu.myactivitiestoolkit.services;
//
//import android.content.Intent;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Build;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import cs.umass.edu.myactivitiestoolkit.R;
//import cs.umass.edu.myactivitiestoolkit.communication.MHLClientFilter;
//import cs.umass.edu.myactivitiestoolkit.processing.Filter;
//import cs.umass.edu.myactivitiestoolkit.steps.OnStepListener;
//import cs.umass.edu.myactivitiestoolkit.constants.Constants;
//import cs.umass.edu.myactivitiestoolkit.steps.StepDetector;
//import edu.umass.cs.MHLClient.client.MessageReceiver;
//import edu.umass.cs.MHLClient.sensors.GyroscopeReading;
//
///**
// * Created by Jon on 12/10/2017.
// */
//
//public class GyroscopeService extends SensorService implements SensorEventListener{
//
//    /** Used during debugging to identify logs by class */
//    private static final String TAG = GyroscopeService.class.getName();
//
//    /** Sensor Manager object for registering and unregistering system sensors */
//    private SensorManager mSensorManager;
//
//    /** Manages the physical gyroscope sensor on the phone. */
//    private Sensor mGyroscopeSensor;
//
//    /** Android built-in step detection sensor **/
//    private Sensor mStepSensor;
//
//    /** Defines your step detection algorithm. **/
//    private final StepDetector stepDetector;
//
//    /**
//     * The step count as predicted by your server-side step detection algorithm.
//     */
//    private int serverStepCount = 0;
//
//    private Filter filter; // <SOLUTION/ A1>
//
//    public GyroscopeService(){
//        //<SOLUTION A1>
//        filter = new Filter(3);
//        //</SOLUTION A1>
//        stepDetector = new StepDetector();
//    }
//
//    @Override
//    protected void onServiceStarted() {
//        broadcastMessage(Constants.MESSAGE.GYROSCOPE_SERVICE_STARTED);
//    }
//
//    @Override
//    protected void onServiceStopped() {
//        broadcastMessage(Constants.MESSAGE.GYROSCOPE_SERVICE_STOPPED);
//        if (client != null)
//            client.unregisterMessageReceivers();
//    }
//
//    @Override
//    public void onConnected() {
//        super.onConnected();
//
//        client.registerMessageReceiver(new MessageReceiver(MHLClientFilter.AVERAGE_ACCELERATION) {
//            @Override
//            protected void onMessageReceived(JSONObject json) {
//                Log.d(TAG, "Received average acceleration from server.");
//                try {
//                    JSONObject data = json.getJSONObject("data");
//                    float average_X = (float)data.getDouble("average_X");
//                    float average_Y = (float)data.getDouble("average_Y");
//                    float average_Z = (float)data.getDouble("average_Z");
//                    broadcastAverageAcceleration(average_X, average_Y, average_Z);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        client.registerMessageReceiver(new MessageReceiver(MHLClientFilter.STEP) {
//            @Override
//            protected void onMessageReceived(JSONObject json) {
//                Log.d(TAG, "Received step update from server.");
//                try {
//                    JSONObject data = json.getJSONObject("data");
//                    long timestamp = data.getLong("timestamp");
//                    Log.d(TAG, "Step occurred at " + timestamp + ".");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                serverStepCount++;
//                broadcastServerStepCount(serverStepCount);
//            }
//        });
//        client.registerMessageReceiver(new MessageReceiver(MHLClientFilter.ACTIVITY) {
//            @Override
//            protected void onMessageReceived(JSONObject json) {
//                Log.d(TAG, "Received activity update from server.");
//                String activity;
//                try {
//                    JSONObject data = json.getJSONObject("data");
//                    activity = data.getString("activity");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                Log.d(TAG, "Activity is : " + activity);
//                broadcastActivity(activity);
//            }
//        });
//    }
//
//    /**
//     * Register gyroscope sensor listener
//     */
//    @Override
//    protected void registerSensors(){
//        // TODO (Assignment 0) : Register the gyroscope sensor using the sensor manager
//        //<SOLUTION A0>
//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//
//        if (mSensorManager == null){
//            Log.e(TAG, Constants.ERROR_MESSAGES.ERROR_NO_SENSOR_MANAGER);
//            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGES.ERROR_NO_SENSOR_MANAGER,Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//
//        if (mGyroscopeSensor != null) {
//            mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
//        } else {
//            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGES.ERROR_NO_GYROSCOPE, Toast.LENGTH_LONG).show();
//            Log.w(TAG, Constants.ERROR_MESSAGES.ERROR_NO_GYROSCOPE);
//        }
//        //</SOLUTION A0>
//
//        // TODO (Assignment 1) : Register the built-in Android step detector (API 19 or higher)
//        //<SOLUTION A1>
//        // built-in step detector only available for API level 19 and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
//            mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_GAME);
//        }
//        //</SOLUTION A1>
//
//        // TODO (Assignment 1) : Register gyroscope with step detector and register on step listener for sending steps to UI
//        //<SOLUTION A1>
//        // register a listener to receive step events
//        stepDetector.registerOnStepListener(new OnStepListener() {
//            @Override
//            public void onActionCountUpdated(int stepCount) {
//                broadcastLocalStepCount(stepCount);
//            }
//
//            @Override
//            public void onActionDetected(long timestamp, float[] values) {
//                broadcastStepDetected(timestamp, values);
//            }
//        });
//        mSensorManager.registerListener(stepDetector, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
//        //</SOLUTION A1>
//    }
//
//    /**
//     * Unregister the sensor listener, this is essential for the battery life!
//     */
//    @Override
//    protected void unregisterSensors() {
//        // TODO : Unregister sensors
//        //<SOLUTION A0/A1>
//        if (mSensorManager != null) {
//            mSensorManager.unregisterListener(this, mGyroscopeSensor);
//            mSensorManager.unregisterListener(stepDetector, mGyroscopeSensor);
//            mSensorManager.unregisterListener(this, mStepSensor);
//        }
//        //</SOLUTION A0/A1>
//    }
//
//    @Override
//    protected int getNotificationID() {
//        return Constants.NOTIFICATION_ID.GYROSCOPE_SERVICE;
//    }
//
//    @Override
//    protected String getNotificationContentText() {
//        return getString(R.string.activity_service_notification);
//    }
//
//    @Override
//    protected int getNotificationIconResourceID() {
//        return R.drawable.ic_running_white_24dp;
//    }
//
//    //This method is called when we receive a sensor reading. We will be interested in this method primarily.
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            //<SOLUTION A1>
//            double[] filteredValues = filter.getFilteredValues(event.values[0], event.values[1], event.values[2]);
//            client.sendSensorReading(new GyroscopeReading(userID, "MOBILE", "", event.timestamp, (float)filteredValues[0], (float)filteredValues[1], (float)filteredValues[2]));
//
//            float[] floatFilteredValues = new float[]{(float) filteredValues[0], (float) filteredValues[1], (float) filteredValues[2]};
//            broadcastGyroscopeReading(event.timestamp, floatFilteredValues);
//            //</SOLUTION A1>
//        } else {
//            Log.w(TAG, Constants.ERROR_MESSAGES.WARNING_SENSOR_NOT_SUPPORTED);
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.i(TAG, "Accuracy changed: " + accuracy);
//    }
//
//    /**
//     * Broadcasts the gyroscope reading to other application components, e.g. the main UI.
//     * @param gyroscopeReadings the x, y, and z gyroscope readings
//     */
//    public void broadcastGyroscopeReading(final long timestamp, final float[] gyroscopeReadings) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.TIMESTAMP, timestamp);
//        intent.putExtra(Constants.KEY.GYROSCOPE_DATA, gyroscopeReadings);
//        intent.setAction(Constants.ACTION.BROADCAST_GYROSCOPE_DATA);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts the step count computed by the Android built-in step detection algorithm
//     * to other application components, e.g. the main UI.
//     */
//    public void broadcastAndroidStepCount(int stepCount) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.STEP_COUNT, stepCount);
//        intent.setAction(Constants.ACTION.BROADCAST_ANDROID_STEP_COUNT);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts the step count computed by your step detection algorithm
//     * to other application components, e.g. the main UI.
//     */
//    public void broadcastLocalStepCount(int stepCount) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.STEP_COUNT, stepCount);
//        intent.setAction(Constants.ACTION.BROADCAST_LOCAL_STEP_COUNT);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts the step count computed by your server-side step detection algorithm
//     * to other application components, e.g. the main UI.
//     */
//    public void broadcastServerStepCount(int stepCount) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.STEP_COUNT, stepCount);
//        intent.setAction(Constants.ACTION.BROADCAST_SERVER_STEP_COUNT);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts a step event to other application components, e.g. the main UI.
//     */
//    public void broadcastStepDetected(long timestamp, float[] values) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.GYROSCOPE_PEAK_TIMESTAMP, timestamp);
//        intent.putExtra(Constants.KEY.GYROSCOPE_PEAK_VALUE, values);
//        intent.setAction(Constants.ACTION.BROADCAST_GYROSCOPE_PEAK);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts the step count computed by your server-side step detection algorithm
//     * to other application components, e.g. the main UI.
//     */
//    public void broadcastActivity(String activity) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.ACTIVITY, activity);
//        intent.setAction(Constants.ACTION.BROADCAST_ACTIVITY);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//    /**
//     * Broadcasts the step count computed by your server-side step detection algorithm
//     * to other application components, e.g. the main UI.
//     */
//    public void broadcastAverageAcceleration(float average_X, float average_Y, float average_Z) {
//        Intent intent = new Intent();
//        intent.putExtra(Constants.KEY.AVERAGE_ACCELERATION, new float[]{average_X, average_Y, average_Z});
//        intent.setAction(Constants.ACTION.BROADCAST_AVERAGE_ACCELERATION);
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//        manager.sendBroadcast(intent);
//    }
//
//
//}
