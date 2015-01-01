package at.fhtw.partyradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.helper.Utility;
import at.fhtw.partyradar.service.BackgroundLocationService;

import static at.fhtw.partyradar.helper.Utility.*;

public class EventMapFragment extends Fragment implements OnMapReadyCallback {

    protected static final String LOG_TAG = "EventMapFragment";

    private GoogleMap mMap;
    private LatLng mLastPosition;
    private LatLng mMapCenter;

    private BroadcastReceiver mReceiver;

    public EventMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prepare and register for broadcasts of location updates
        IntentFilter intentFilter = new IntentFilter(BackgroundLocationService.BROADCAST_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String latLngString = intent.getStringExtra(BackgroundLocationService.BROADCAST_DATA);
                String[] latLngSplit = latLngString.split("\\|");

                mLastPosition = new LatLng(Double.parseDouble(latLngSplit[0]), Double.parseDouble(latLngSplit[1]));
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);

        mLastPosition = Utility.getPositionFromStorage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eventmap, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment)(getChildFragmentManager().findFragmentById(R.id.mapFragment));

        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                mMapCenter = mMap.getCameraPosition().target;
                showEvents(mMapCenter, calculateRadius());
            }
        });

        mMap.setMyLocationEnabled(true);

        if (mLastPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastPosition, 13));
        }

    }

    /**
     * shows the Events as marker on the map
     * @param center center of area
     * @param radius radius of area
     */
    private void showEvents(LatLng center, double radius) {
        Log.d(LOG_TAG, "Center = Lat:" + center.latitude + " Lng:" + center.longitude + " Radius: " + radius);
        if (mMap != null) {

            mMap.clear();

            Cursor cursor = getActivity().getContentResolver().query(
                    EventContract.EventEntry.buildEventWithinArea(center.latitude, center.longitude, radius),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            while (cursor.moveToNext()) {
                double event_latitude = cursor.getDouble(cursor.getColumnIndex(EventContract.EventEntry.COLUMN_LATITUDE));
                double event_longitude = cursor.getDouble(cursor.getColumnIndex(EventContract.EventEntry.COLUMN_LONGITUDE));
                String event_title = cursor.getString(cursor.getColumnIndex(EventContract.EventEntry.COLUMN_TITLE));

                // TODO: add intents to open event details

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(event_latitude, event_longitude))
                        .title(event_title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }

            cursor.close();
        }
    }

    /**
     * calculates the max. radius from the center of the map to the boundary
     * @return radius in km
     */
    private double calculateRadius() {
        if (mMap != null) {
            LatLngBounds currentMapScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
            double diagonal = getDistance(currentMapScreen.northeast.latitude, currentMapScreen.northeast.longitude, currentMapScreen.southwest.latitude, currentMapScreen.southwest.longitude);

            return diagonal / 2;
        }
        else
            return 0;
    }

}
