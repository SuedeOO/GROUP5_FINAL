package cs.umass.edu.myactivitiestoolkit.steps;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.processing.Filter;

/**
 * Created by Jon on 12/20/2017.
 */

public class JumpDetector implements OnStepListener {
    @Override
    public void onStepCountUpdated(int stepCount) {

    }

    @Override
    public void onStepDetected(long timestamp, float[] values) {

    }
}
