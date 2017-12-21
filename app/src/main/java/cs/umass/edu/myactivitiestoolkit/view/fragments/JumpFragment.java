package cs.umass.edu.myactivitiestoolkit.view.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.services.AccelerometerService;
import cs.umass.edu.myactivitiestoolkit.services.ServiceManager;
import cs.umass.edu.myactivitiestoolkit.services.msband.BandService;

/**
 * Created by Jon on 12/10/2017.
 */

public class JumpFragment extends Fragment {
    /**
     * Used during debugging to identify logs by class.
     */
    @SuppressWarnings("unused")
    private static final String TAG = JumpFragment.class.getName();

    /**
     * The switch which toggles the {@link AccelerometerService}.
     **/
    public Switch jumpAccelerometer;
    private TextView txtHighestJump;
    private TextView txtLastJump;
    private ServiceManager serviceManager;

    /**
     * The receiver listens for messages from the {@link AccelerometerService}, e.g. was the
     * service started/stopped, and updates the status views accordingly. It also
     * listens for sensor data and displays the sensor readings to the user.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           // System.out.println("looooook at me");
            if (intent.getAction() != null) {
                //System.out.println("looook not null");
                if (intent.getAction().equals(Constants.ACTION.BROADCAST_MESSAGE)) {
                    //System.out.println("??");
                    int message = intent.getIntExtra(Constants.KEY.MESSAGE, -1);
                    if (message == Constants.MESSAGE.ACCELEROMETER_SERVICE_STOPPED) {
                        jumpAccelerometer.setChecked(false);
                    } else if (message == Constants.MESSAGE.BAND_SERVICE_STOPPED) {
                        jumpAccelerometer.setChecked(false);
                    }
                } else if (intent.getAction().equals(Constants.ACTION.BROADCAST_LAST_JUMP)) {
                    //System.out.println("last");
                    int distance = intent.getIntExtra(Constants.KEY.LAST_JUMP, 0);
                    displayLastJump(distance);
                } else if (intent.getAction().equals(Constants.ACTION.BROADCAST_HIGHEST_JUMP)) {
                    //System.out.println("highest");
                    int distance = intent.getIntExtra(Constants.KEY.HIGHEST_JUMP, 0);
                    displayHighestJump(distance);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.serviceManager = ServiceManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_jump, container, false);
        txtHighestJump = (TextView) view.findViewById(R.id.txtHighestJump);
        txtLastJump = (TextView) view.findViewById(R.id.txtLastJump);

        jumpAccelerometer = (Switch) view.findViewById(R.id.jumpAccelerometer);
        jumpAccelerometer.setChecked(serviceManager.isServiceRunning(AccelerometerService.class));
        jumpAccelerometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                if (enabled) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.PAGE.PAGE_ZERO,0);
                    //System.out.println("Enabled!!!!");
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    boolean runOverMSBand = preferences.getBoolean(getString(R.string.pref_msband_key),
                            getResources().getBoolean(R.bool.pref_msband_default));
                    if (runOverMSBand) {
                        serviceManager.startSensorService(BandService.class);
                    } else {
                        serviceManager.startSensorService(AccelerometerService.class);
                    }
                } else {
                    if (serviceManager.isServiceRunning(AccelerometerService.class))
                        serviceManager.stopSensorService(AccelerometerService.class);
                    if (serviceManager.isServiceRunning(BandService.class))
                        serviceManager.stopSensorService(BandService.class);
                }
            }
        });

        return view;
    }

    private void displayLastJump(final int jump) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtLastJump.setText(String.format(Locale.getDefault(), getString(R.string.last_jump), jump));
            }
        });
    }


    private void displayHighestJump(final int jump) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtHighestJump.setText(String.format(Locale.getDefault(), getString(R.string.highest_jump), jump));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.BROADCAST_MESSAGE);
        filter.addAction(Constants.ACTION.BROADCAST_AVERAGE_ACCELERATION);
        filter.addAction(Constants.ACTION.BROADCAST_HIGHEST_JUMP);
        filter.addAction(Constants.ACTION.BROADCAST_LAST_JUMP);
        broadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        try {
            broadcastManager.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

}