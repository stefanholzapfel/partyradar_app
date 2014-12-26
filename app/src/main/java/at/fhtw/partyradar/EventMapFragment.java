package at.fhtw.partyradar;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class EventMapFragment extends Fragment {

    private GoogleMap mMap;

    public EventMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eventmap, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity mainActivity = (MainActivity) getActivity();
        LatLng lastLocation = mainActivity.getLastLocation();

        mMap = ((SupportMapFragment)(getChildFragmentManager().findFragmentById(R.id.mapFragment))).getMap();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 12);
        mMap.animateCamera(cameraUpdate);
    }
}
