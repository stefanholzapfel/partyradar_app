package at.fhtw.partyradar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import static at.fhtw.partyradar.helper.Utility.*;

public class EventMapFragment extends Fragment implements OnMapReadyCallback {

    protected static final String TAG = "EventMapFragment";

    private GoogleMap mMap;
    private LatLng mLastLocation;
    private LatLng mMapCenter;

    public EventMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eventmap, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment)(getChildFragmentManager().findFragmentById(R.id.mapFragment));

        mapFragment.getMapAsync(this);

        return rootView;
    }

    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                mMapCenter = mMap.getCameraPosition().target;
                getEvents(mMapCenter, calculateRadius());
            }
        });

        mMap.setMyLocationEnabled(true);
        mLastLocation = getLastLocation();

        // center the map to the current location
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLastLocation, 13);
        mMap.animateCamera(cameraUpdate);

        showEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLastLocation = getLastLocation();
    }

    private LatLng getLastLocation() {
        // get last known location from parent / main activity
        // TODO: change to better solution using ContentProvider
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity.getLastLocation();
    }

    /**
     * retrieved the Events for a specific area
     * @param center center of area
     * @param radius radius of area
     */
    private void getEvents(LatLng center, double radius) {
        // TODO: replace by ContentProvider
        Log.i(TAG, "Center = Lat:" + center.latitude + " Lng:" + center.longitude + " Radius: " + radius);
    }

    /**
     * shows the Events as marker on the map
     */
    private void showEvents() {
        if (mMap != null) {

            // TODO: replace by actual event data
            // TODO: add intents to open event details
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(48.208174, 16.373819))
                    .title("Event 1")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(48.21, 16.38))
                    .title("Event 2")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(48.20, 16.36))
                    .title("Event 3")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
    }

    /**
     * calculates the max. radius from the center of the map to the boundary
     * @return radius in km
     */
    private double calculateRadius() {
        LatLngBounds currentMapScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
        double diagonal = getDistance(currentMapScreen.northeast.latitude, currentMapScreen.northeast.longitude, currentMapScreen.southwest.latitude, currentMapScreen.southwest.longitude);

        return diagonal / 2;
    }

}
