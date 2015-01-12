package at.fhtw.partyradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.LinkedList;
import java.util.List;

import at.fhtw.partyradar.authentication.AuthenticationHelper;
import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.helper.Utility;
import at.fhtw.partyradar.service.BackgroundLocationService;

import static at.fhtw.partyradar.helper.Utility.*;

public class EventMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String LOG_TAG = EventMapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private Switch mSwitch_showHeatMap;
    private LatLng mLastPosition;

    private BroadcastReceiver mReceiver;
    private Cursor mEventData;

    private static final int EVENT_LOADER = 1;

    // Definition of the database's columns used by the loader
    private static final String[] EVENT_COLUMNS = {
            EventContract.EventEntry._ID,
            EventContract.EventEntry.COLUMN_EVENT_ID,
            EventContract.EventEntry.COLUMN_TITLE,
            EventContract.EventEntry.COLUMN_LOCATION_NAME,
            EventContract.EventEntry.COLUMN_ADDRESS,
            EventContract.EventEntry.COLUMN_ADDRESS_ADDITIONS,
            EventContract.EventEntry.COLUMN_CITY,
            EventContract.EventEntry.COLUMN_LATITUDE,
            EventContract.EventEntry.COLUMN_LONGITUDE,
            EventContract.EventEntry.COLUMN_MAX_ATTENDS,
            EventContract.EventEntry.COLUMN_ATTENDEECOUNT
    };

    // Indices tied to the EVENT_COLUMNS. If EVENT_COLUMNS changes, these have to be adapted.
    public static final int COL_ID = 0;
    public static final int COL_EVENT_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_LOCATION_NAME = 3;
    public static final int COL_ADDRESS = 4;
    public static final int COL_ADDRESS_ADDITIONS = 5;
    public static final int COL_CITY = 6;
    public static final int COL_LATITUDE = 7;
    public static final int COL_LONGITUDE = 8;
    public static final int COL_MAX_ATTENDS = 9;
    public static final int COL_ATTENDEECOUNT = 10;


    public EventMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: check if this is actually required in "v1"
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

        // get the latest known position
        mLastPosition = Utility.getPositionFromStorage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eventmap, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment)(getChildFragmentManager().findFragmentById(R.id.mapFragment));

        mSwitch_showHeatMap = (Switch) rootView.findViewById(R.id.switch_showHeatMap);
        mSwitch_showHeatMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawMapContent();
            }
        });

        mapFragment.getMapAsync(this);

        // show the option to switch to the heat map only if logged in
        new AsyncTask<Context, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Context... params) {
                if (AuthenticationHelper.getToken(getActivity(), false) != null) return true;
                else return false;
            }

            @Override
            protected void onPostExecute(Boolean isLoggedIn) {
                if (isLoggedIn) mSwitch_showHeatMap.setVisibility(View.VISIBLE);
            }
        }.execute(getActivity());

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // centering the map to the latest known position
        if (mLastPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastPosition, 12));
        }

        // initiate retrieving the events data
        getLoaderManager().initLoader(EVENT_LOADER, null, this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    /**
     * clearing the map and drawing its content (events / heat map)
     */
    private void drawMapContent() {
        // no data, no drawing
        if (mEventData == null || mEventData.getCount() == 0) return;

        mMap.clear();
        List<WeightedLatLng> eventsForHeatMap = new LinkedList<>();

        // required, because the cursor is re-run several times and we need to get to the first position
        mEventData.moveToFirst();
        do {
            double event_latitude = mEventData.getDouble(COL_LATITUDE);
            double event_longitude = mEventData.getDouble(COL_LONGITUDE);
            String event_title = mEventData.getString(COL_TITLE);
            String event_locationName = mEventData.getString(COL_LOCATION_NAME);
            int event_maxAttends = mEventData.getInt(COL_MAX_ATTENDS);
            int event_attendeeCount = mEventData.getInt(COL_ATTENDEECOUNT);

            // TODO: add intents to open event details

            // building data for the heat map
            if (mSwitch_showHeatMap.isChecked()) {
                // TODO: calculate correct ratio
                double attendeeRatio = event_maxAttends / 100; //event_attendeeCount / event_maxAttends;
                eventsForHeatMap.add(new WeightedLatLng(new LatLng(event_latitude, event_longitude), attendeeRatio));
            }
            // drawing the markers
            else {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(event_latitude, event_longitude))
                        .title(event_title)
                        .snippet(event_locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
        } while (mEventData.moveToNext());

        // drawing the heat map
        if (mSwitch_showHeatMap.isChecked() && eventsForHeatMap.size() > 0) {
            HeatmapTileProvider heatMapTileProvider = new HeatmapTileProvider.Builder()
                    .weightedData(eventsForHeatMap)
                    .radius(50)
                    .build();
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapTileProvider));
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Log.d(LOG_TAG, "onCreateLoader");
        Uri weatherForLocationUri = EventContract.EventEntry.buildEventWithinArea(mLastPosition.latitude, mLastPosition.longitude, Double.parseDouble(getActivity().getString(R.string.events_max_range)));

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                EVENT_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.d(LOG_TAG, "onLoadFinished");
        mEventData = data;
        drawMapContent();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Log.d(LOG_TAG, "onLoaderReset");
    }
}
