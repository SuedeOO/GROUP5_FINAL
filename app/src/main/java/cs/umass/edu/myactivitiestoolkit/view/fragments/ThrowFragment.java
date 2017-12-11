package cs.umass.edu.myactivitiestoolkit.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Fragment;

import cs.umass.edu.myactivitiestoolkit.R;

/**
 * Created by Jon on 12/10/2017.
 */

public class ThrowFragment extends Fragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_throw, container, false);
        TextView txtVersion = (TextView) view.findViewById(R.id.txtVersion);

        return view;
    }
}
