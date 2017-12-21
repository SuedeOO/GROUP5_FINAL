package cs.umass.edu.myactivitiestoolkit.steps;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.processing.Filter;

/**
 * Created by Jon on 12/20/2017.
 */

public class JumpDetector implements OnActionListener {
    /** Maintains the set of listeners registered to handle step events. **/
    private ArrayList<OnActionListener> mStepListeners;

    //TODO: Remove some fields from starter code
    private final Filter mFilter;

    private double previousMagnitude = -1000000;

    private boolean previousPositive;

    /** Stores the timestamp of the previous step. **/
    private long previousStepTimestamp;

    /**
     * The number of steps taken.
     */
    private int jumpCount;

    public JumpDetector(){
        mFilter = new Filter(3);
        mStepListeners = new ArrayList<>();
        jumpCount = 0;
    }
    @Override
    public void onActionCountUpdated(int stepCount) {

    }

    @Override
    public void onActionDetected(long timestamp, float[] values)
    {
        jumpCount++;
        for (OnActionListener stepListener : mStepListeners){
            stepListener.onActionDetected(timestamp, values);
            stepListener.onActionCountUpdated(jumpCount);
        }
    }
    public void detectJump(long timestamp_in_milliseconds, float... values)
    {
        // TODO: Forgot to convert timestamp in starter code. At least it should be consistent with Python code...
        // TODO: Also forgot to filter data sent to the server the same way as this data stream

        if(timestamp_in_milliseconds - previousStepTimestamp < 500) return;

        double magnitudeSq = 0;
        for (double v : values){
            magnitudeSq += v * v;
        }
        double currentMagnitude = Math.sqrt(magnitudeSq);

        //FYI: the variable that holds the previous point value is initialized to -1000000
        //this block is only for the first point bc the if statement should only be true for that first point,
        //bc we initialized previousMagnitude to -1000000
        if(previousMagnitude == -1000000)
        {
            previousMagnitude = currentMagnitude;
            //not sure whether to initialize this to false or true
            previousPositive = false;
        }

        boolean currentPositive = currentMagnitude > previousMagnitude;

        //ensures that slope is significant enough to be detected as a step, disregards small fluctuations that shouldn't be steps
        if(Math.abs(currentMagnitude - previousMagnitude)>0.5)
        {
            //for ex: if previousMagnitude was positive and currentMagnitude is negative, the direction has changed, which
            //means a step was taken, so increment step counter
            if(!currentPositive && previousPositive)
            {
                //at the end, store current point as previous point so you're ready to compare the next point
                previousPositive = false;
                previousMagnitude = currentMagnitude;
                previousStepTimestamp = timestamp_in_milliseconds;
                onJumpDetected(timestamp_in_milliseconds, values);
            } else {
                //at the end, store current point as previous point so you're ready to compare the next point
                previousPositive = currentPositive;
                previousMagnitude = currentMagnitude;
            }
        } else {
            //at the end, store current point as previous point so you're ready to compare the next point
            previousPositive = currentPositive;
            previousMagnitude = currentMagnitude;
        }
    }
    /**
     * This method is called when a step is detected. It updates the current step count,
     * notifies all listeners that a step has occurred and also notifies all listeners
     * of the current step count.
     */
    private void onJumpDetected(long timestamp, float[] values){
        jumpCount++;
        for (OnActionListener stepListener : mStepListeners){
            stepListener.onActionDetected(timestamp, values);
            stepListener.onActionCountUpdated(jumpCount);
        }
    }
}
