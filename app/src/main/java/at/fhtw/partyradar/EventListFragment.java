package at.fhtw.partyradar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class EventListFragment extends Fragment {

    private LatLng mLastLocation;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_eventlist, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateLastLocation();

        TextView textView = (TextView) getActivity().findViewById(R.id.text_position);
        textView.setText("Lat: " + mLastLocation.latitude + " Lng: " + mLastLocation.longitude);
    }

    private void updateLastLocation() {
        // get last known location from parent / main activity
        MainActivity mainActivity = (MainActivity) getActivity();
        mLastLocation = mainActivity.getLastLocation();
    }
}
