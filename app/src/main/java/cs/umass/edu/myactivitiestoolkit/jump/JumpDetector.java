package cs.umass.edu.myactivitiestoolkit.jump;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.processing.Filter;

/**
 * Created by mikeshi on 12/11/17.
 */

public class JumpDetector implements SensorEventListener {
    private  static final String TAG = JumpDetector.class.getName();

    private ArrayList<OnJumpListener> mJumpListeners;

    private Filter mFilter;
    private long largestMagTimeStamp;
    private long smallestMagTimeStamp;
    private double Min;
    private double Max;
    private double highestJumpHeight;
    int count;

    public JumpDetector(){
        mFilter = new Filter(3);
        mJumpListeners = new ArrayList<>();
        highestJumpHeight = 0;
        largestMagTimeStamp = 0;
        smallestMagTimeStamp = Long.MAX_VALUE;
        Min = Double.MAX_VALUE;
        Max = 0;
        count = 0;
    }

    public void registerOnJumpListener(final OnJumpListener jumpListener){
        mJumpListeners.add(jumpListener);
    }

    public void unregisterOnJumpListener(final OnJumpListener jumpListener){
        mJumpListeners.remove(jumpListener);
    }

    public void unregisterOnJumpListener(){
        mJumpListeners.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        System.out.println(count);
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            double[] xyz = mFilter.getFilteredValues(event.values);
            double magnitude = Math.sqrt(Math.pow(xyz[0],2)+Math.pow(xyz[1],2)+Math.pow(xyz[2],2));
            //window_of_mag.put(magnitude,timestamp);

            if(magnitude > Max) {
                Max = magnitude;
                largestMagTimeStamp = event.timestamp;
            }
            if(magnitude < Min){
                Min = magnitude;
                smallestMagTimeStamp = event.timestamp;
            }
            count ++;
            if(count == 30){
                count = 0;
                if(Max - Min > 3)
                detectJump(largestMagTimeStamp,smallestMagTimeStamp, Min, Max);
            }
        }
    }


    public void detectJump(long largestMagTimeStamp, long smallestMagTimeStamp, double min, double max){
        double time = (double)(Math.abs(smallestMagTimeStamp-largestMagTimeStamp) / 1000);
        double distance = 4.9 * time*time;
        if(distance > highestJumpHeight) highestJumpHeight = distance;
        onJumpDetected(distance);
        Max = 0;
        Min = Double.MAX_VALUE;
    }

    public void onJumpDetected(double distance){
        System.out.println("Yeees");
        for(OnJumpListener jumpListener :mJumpListeners){
            jumpListener.onHighestJumpUpdated(highestJumpHeight);
            jumpListener.onJumpUpdated(distance);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
