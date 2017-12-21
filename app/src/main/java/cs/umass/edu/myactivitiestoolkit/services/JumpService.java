package cs.umass.edu.myactivitiestoolkit.services;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.communication.MHLClientFilter;
import cs.umass.edu.myactivitiestoolkit.jump.JumpDetector;
import cs.umass.edu.myactivitiestoolkit.jump.OnJumpListener;
import cs.umass.edu.myactivitiestoolkit.processing.Filter;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import edu.umass.cs.MHLClient.client.MessageReceiver;
import edu.umass.cs.MHLClient.client.MobileIOClient;
import edu.umass.cs.MHLClient.sensors.AccelerometerReading;

/**
 * Created by mikeshi on 12/21/17.
 */

public class JumpService extends SensorService implements  SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private final JumpDetector jumpDetector;
    private double lastJump = 0;
    private double highestJump = 0;
    private Filter filter;

    public JumpService(){
        filter = new Filter(3);
        jumpDetector = new JumpDetector();
    }

    @Override
    protected  void onServiceStarted(){
        broadcastMessage(Constants.MESSAGE.ACCELEROMETER_SERVICE_STARTED);
    }

    @Override
    protected void onServiceStopped() {
        broadcastMessage(Constants.MESSAGE.ACCELEROMETER_SERVICE_STOPPED);
        if (client != null)
            client.unregisterMessageReceivers();
    }

    @Override
    public void onConnected(){
        super.onConnected();
        client.registerMessageReceiver(new MessageReceiver(MHLClientFilter.AVERAGE_ACCELERATION) {
            @Override
            protected void onMessageReceived(JSONObject json) {
                Log.d(TAG, "Received average acceleration from server.");
                try {
                    JSONObject data = json.getJSONObject("data");
                    float average_X = (float)data.getDouble("average_X");
                    float average_Y = (float)data.getDouble("average_Y");
                    float average_Z = (float)data.getDouble("average_Z");
                    broadcastAverageAcceleration(average_X, average_Y, average_Z);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //<SOLUTION A1>
            double[] filteredValues = filter.getFilteredValues(event.values[0], event.values[1], event.values[2]);
            client.sendSensorReading(new AccelerometerReading(userID, "MOBILE", "", event.timestamp, (float)filteredValues[0], (float)filteredValues[1], (float)filteredValues[2]));

            float[] floatFilteredValues = new float[]{(float) filteredValues[0], (float) filteredValues[1], (float) filteredValues[2]};
            broadcastAccelerometerReading(event.timestamp, floatFilteredValues);
            //</SOLUTION A1>
        }else{
            Log.w(TAG, Constants.ERROR_MESSAGES.WARNING_SENSOR_NOT_SUPPORTED);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "Accuracy changed: " + i);
    }

    @Override
    protected void registerSensors() {
        if (mSensorManager == null){
            Log.e(TAG, Constants.ERROR_MESSAGES.ERROR_NO_SENSOR_MANAGER);
            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGES.ERROR_NO_SENSOR_MANAGER,Toast.LENGTH_LONG).show();
            return;
        }

        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGES.ERROR_NO_ACCELEROMETER, Toast.LENGTH_LONG).show();
            Log.w(TAG, Constants.ERROR_MESSAGES.ERROR_NO_ACCELEROMETER);
        }
        jumpDetector.registerOnJumpListener(new OnJumpListener() {
            @Override
            public void onJumpUpdated(double distance) {
                broadcastlastJump(distance);
            }

            @Override
            public void onHighestJumpUpdated(double distance) {
                broadcasthighestJump(distance);
            }
        });
        mSensorManager.registerListener(jumpDetector,mAccelerometerSensor,SensorManager.SENSOR_DELAY_GAME );
    }

    @Override
    protected void unregisterSensors() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mAccelerometerSensor);
            mSensorManager.unregisterListener(jumpDetector, mAccelerometerSensor);
        }
    }

    @Override
    protected int getNotificationID() {
        return Constants.NOTIFICATION_ID.ACCELEROMETER_SERVICE;
    }

    @Override
    protected String getNotificationContentText() {
        return getString(R.string.activity_service_notification);
    }

    @Override
    protected int getNotificationIconResourceID() {
        return R.drawable.ic_running_white_24dp;
    }

    public void broadcastAccelerometerReading(final long timestamp, final float[] accelerometerReadings) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.TIMESTAMP, timestamp);
        intent.putExtra(Constants.KEY.ACCELEROMETER_DATA, accelerometerReadings);
        intent.setAction(Constants.ACTION.BROADCAST_ACCELEROMETER_DATA);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }

    public void broadcastlastJump(double distance){
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.LAST_JUMP, distance);
        intent.setAction(Constants.ACTION.BROADCAST_LAST_JUMP);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }
    public void broadcasthighestJump(double distance){
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.HIGHEST_JUMP, distance);
        intent.setAction(Constants.ACTION.BROADCAST_HIGHEST_JUMP);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }

    public void broadcastAverageAcceleration(float average_X, float average_Y, float average_Z) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.AVERAGE_ACCELERATION, new float[]{average_X, average_Y, average_Z});
        intent.setAction(Constants.ACTION.BROADCAST_AVERAGE_ACCELERATION);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }

}
