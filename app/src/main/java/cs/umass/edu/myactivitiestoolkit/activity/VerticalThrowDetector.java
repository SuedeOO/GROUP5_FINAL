package cs.umass.edu.myactivitiestoolkit.activity;

import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.style.DynamicDrawableSpan;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.processing.Filter;
import java.nio.channels.SeekableByteChannel;

/**
 * Created by mikeshi on 12/21/17.
 */

public class VerticalThrowDetector implements SensorEventListener {
    private ArrayList<OnVerticalThrowListener> mVThrowListeners;
    private Filter mFilter;
    private long startTimeStamp;
    private long endTimeStamp;
    private double max;
    private double previous;
    private int bestThrow;
    private int count;
    private int window_size;

    public VerticalThrowDetector(){
        mFilter = new Filter(3);
        mVThrowListeners = new ArrayList<>();
        startTimeStamp = 0;
        endTimeStamp = 0;
        max = 0;
        count = 0;
        window_size= 0;
        previous = 0;
    }

    public void registerVThrowListener(final OnVerticalThrowListener vThrowListener){
        mVThrowListeners.add(vThrowListener);
    }

    public void unregisterVThrowListener(final OnVerticalThrowListener vThrowListener){
        mVThrowListeners.remove(vThrowListener);
    }

    public void unregisterVThrowListener(){
        mVThrowListeners.clear();
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //System.out.println("1");
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double[] xyz = mFilter.getFilteredValues(sensorEvent.values);
            double magnitude = Math.sqrt(Math.pow(xyz[0],2)+Math.pow(xyz[1],2)+Math.pow(xyz[2],2));
            //System.out.println(magnitude);
            if(window_size<40){
                if(magnitude > max && magnitude > 9.8){
                    //System.out.println("3");
                    max = magnitude;
                    startTimeStamp = sensorEvent.timestamp;
                }
                if(magnitude < 2 && window_size >5){
                   // System.out.println("1");
                    endTimeStamp = sensorEvent.timestamp;
                    if(startTimeStamp != 0 && endTimeStamp !=0 && endTimeStamp > startTimeStamp) {
                        detectThrow(startTimeStamp, endTimeStamp);
                        window_size = 40;
                    }
                }
                System.out.println(window_size);
                window_size++;
            }else{
               // System.out.println("2");
                window_size =0;
                max = 0;
                startTimeStamp =0;
                endTimeStamp =0;
            }
        }
    }

    public void detectThrow(long startTimeStamp, long endTimeStamp){
        double time = (double)(Math.abs(startTimeStamp-endTimeStamp))/Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND;
        //System.out.println("time" + time);
        double distance = 0.00049 * time*time;
        if((int)distance ==0) return;
        //System.out.println(distance);
        //if(distance > 100) distance = 100;
        if(distance > bestThrow) bestThrow = (int)(distance);
        onThrowDectedted((int) (distance));

    }

    public void onThrowDectedted(int distance){
        for(OnVerticalThrowListener vThrowListener: mVThrowListeners){
            vThrowListener.onVThrowUpdated(distance);
            vThrowListener.onBestVThrowUpdated(bestThrow);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
