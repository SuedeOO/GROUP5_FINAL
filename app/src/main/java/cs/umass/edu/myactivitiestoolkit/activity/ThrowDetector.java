package cs.umass.edu.myactivitiestoolkit.activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.style.DynamicDrawableSpan;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.processing.Filter;

/**
 * Created by mikeshi on 12/21/17.
 */

public class ThrowDetector implements SensorEventListener {

    private ArrayList<OnThrowListener> mThrowListeners;
    private Filter mFilter;
    private long startTimeStamp;
    private long endTimeStamp;
    private double max;
    private double previous;
    private int bestThrow;
    private int count;
    private double sum;
    private int window_size;
    public ThrowDetector(){
        mFilter = new Filter(3);
        mThrowListeners = new ArrayList<>();
        startTimeStamp = 0;
        endTimeStamp = 0;
        max = 0;
        count = 0;
        previous = 9.8;
        sum = 0;
        window_size= 0;
    }

    public void registerOnJumpListener(final OnThrowListener throwListener){
        mThrowListeners.add(throwListener);
    }

    public void unregisterOnJumpListener(final OnThrowListener throwListener){
        mThrowListeners.remove(throwListener);
    }

    public void unregisterOnJumpListener(){
        mThrowListeners.clear();
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //System.out.println("here");
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(window_size < 30){
                double[] xyz = mFilter.getFilteredValues(sensorEvent.values);
                double magnitude = Math.sqrt(Math.pow(xyz[0], 2) + Math.pow(xyz[1], 2) + Math.pow(xyz[2], 2));
                //magnitude = xyz[0];
                //System.out.println(magnitude);
                if (magnitude - previous > 1) {
                    sum += magnitude;
                    count++;
                    previous = magnitude;
                }
                if (magnitude - previous < -1 && count > 5) {
                    sum = sum / count;
                    detectTrow(count * 0.02, sum * 3);
                    System.out.println(count + "times");
                    count = 0;
                }
                window_size ++;
            }else{
                count = 0;
                sum = 0;
                previous = 9.8;
                window_size = 0;
            }
        }

    }

    public void detectTrow(double time, double average_acceleration){
        //System.out.println("here");

        double speed = time * average_acceleration;
        double xy_speed = Math.sin(Math.toRadians(45)) * speed;
        //System.out.println(Math.sin(45));

        double half_time = xy_speed / 9.8;
        int distance = (int)(half_time * xy_speed *2);
        System.out.println(distance);
        if(distance > bestThrow) bestThrow = distance;
        onThrowDectedted(distance);

    }

    public void onThrowDectedted(int distance){
        for(OnThrowListener throwListener: mThrowListeners){
            throwListener.OnThrowUpadated(distance);
            throwListener.OnBestThrow(bestThrow);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
