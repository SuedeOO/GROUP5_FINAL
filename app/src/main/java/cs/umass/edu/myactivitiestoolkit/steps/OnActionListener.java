package cs.umass.edu.myactivitiestoolkit.steps;

/**
 * Clients may register an OnActionListener to receive step events and step count
 * notifications.
 */
public interface OnActionListener {
    void onActionCountUpdated(int stepCount);
    void onActionDetected(long timestamp, float[] values);
}
