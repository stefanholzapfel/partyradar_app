package at.fhtw.partyradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import at.fhtw.partyradar.helper.Utility;
import at.fhtw.partyradar.service.BackgroundLocationService;

public class EventListFragment extends Fragment {

    protected static final String TAG = "EventListFragment";

    private LatLng mLastPosition;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prepare and register for broadcasts of location updates
        mIntentFilter = new IntentFilter(BackgroundLocationService.BROADCAST_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String latLngString = intent.getStringExtra(BackgroundLocationService.BROADCAST_DATA);
                String[] latLngSplit = latLngString.split("\\|");

                mLastPosition = new LatLng(Double.parseDouble(latLngSplit[0]), Double.parseDouble(latLngSplit[1]));
                showLastLocation();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, mIntentFilter);

        mLastPosition = Utility.getPositionFromStorage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eventlist, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        showLastLocation();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void showLastLocation() {
        TextView textView = (TextView) getActivity().findViewById(R.id.text_position);
        textView.setText("Lat: " + mLastPosition.latitude + " Lng: " + mLastPosition.longitude);
    }
}
