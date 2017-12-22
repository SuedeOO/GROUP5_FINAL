package cs.umass.edu.myactivitiestoolkit.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.constants.Constants;
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
    private int highestJumpHeight;
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
        //System.out.println("la");
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            //System.out.println("here");
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
            if(count == 20){
               // System.out.println("!");
                count = 0;
                if(Max - Min > 15 && largestMagTimeStamp > smallestMagTimeStamp){
                    //System.out.println("call");
                    detectJump(largestMagTimeStamp,smallestMagTimeStamp, Min, Max);

                }
                Max = 0;
                Min = Double.MAX_VALUE;

            }
        }
    }


    public void detectJump(long largestMagTimeStamp, long smallestMagTimeStamp, double min, double max){
        largestMagTimeStamp = (long) ((double) largestMagTimeStamp / Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND);
        smallestMagTimeStamp= (long) ((double) smallestMagTimeStamp / Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND);
        double time = (double)(Math.abs(smallestMagTimeStamp-largestMagTimeStamp));
        System.out.println(time);
        double distance = 0.00049 * time*time;
        if(distance > 100) distance = 100;
        if(distance > highestJumpHeight) highestJumpHeight = (int)(distance);
        onJumpDetected((int)(distance));

    }

    public void onJumpDetected(int distance){
        //System.out.println("Yeees");
        for(OnJumpListener jumpListener :mJumpListeners){
            jumpListener.onHighestJumpUpdated(highestJumpHeight);
            jumpListener.onJumpUpdated(distance);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
