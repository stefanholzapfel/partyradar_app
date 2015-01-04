package at.fhtw.partyradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import at.fhtw.partyradar.data.EventContract.EventEntry;
import at.fhtw.partyradar.helper.Utility;
import at.fhtw.partyradar.service.BackgroundLocationService;
import at.fhtw.partyradar.service.FetchDataService;

public class EventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String LOG_TAG = EventListFragment.class.getSimpleName();
    private EventListAdapter mEventListAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_key";
    private LatLng mLastPosition;
    private BroadcastReceiver mReceiver;
    private static final int EVENT_LOADER = 0;

    // Definition of the database's columns used by the loader
    private static final String[] EVENT_COLUMNS = {
            EventEntry._ID,
            EventEntry.COLUMN_EVENT_ID,
            EventEntry.COLUMN_TITLE,
            EventEntry.COLUMN_LOCATION_NAME,
            EventEntry.COLUMN_ADDRESS,
            EventEntry.COLUMN_ADDRESS_ADDITIONS,
            EventEntry.COLUMN_CITY
    };

    // Indices tied to the EVENT_COLUMNS. If EVENT_COLUMNS changes, these have to be adapted.
    public static final int COL_ID = 0;
    public static final int COL_EVENT_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_LOCATION_NAME = 3;
    public static final int COL_ADDRESS = 4;
    public static final int COL_ADDRESS_ADDITIONS = 5;
    public static final int COL_CITY = 6;
    public static final int COL_DISTANCE= 7;

    public EventListFragment() {
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
                //reloadEvents();
                showEvents();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);

        mLastPosition = Utility.getPositionFromStorage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // This Adapter will use the data from the loader and use it to populate the listView
        mEventListAdapter = new EventListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_eventlist, container, false);

        mListView = (ListView) rootView.findViewById(R.id.event_list_view);
        mListView.setAdapter(mEventListAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listView probably hasn't even been populated yet. Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(EVENT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(EVENT_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Saves the actual position in the list
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    //Loader methods implementation starts here

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Building Uri for content provider
        Uri eventsWithinAreaUri = EventEntry.buildEventWithinArea(mLastPosition.latitude, mLastPosition.longitude, Double.parseDouble(getActivity().getString(R.string.events_max_range)));

        // Creating and returning cursor
        return new CursorLoader(
            getActivity(),
            eventsWithinAreaUri,
            EVENT_COLUMNS, // columns to return
            null, // cols for "where" clause
            null, // values for "where" clause
            null  // sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mEventListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEventListAdapter.swapCursor(null);
    }



    // Temporary method to generate content, not needed anymore
    private void showEvents() {
        /*TextView textView_events = (TextView) getActivity().findViewById(R.id.text_events);

        Cursor cursor = getActivity().getContentResolver().query(
                EventContract.EventEntry.buildEventWithinArea(mLastPosition.latitude, mLastPosition.longitude, Double.parseDouble(getActivity().getString(R.string.events_max_range))),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        textView_events.setText("Lat: " + mLastPosition.latitude + " Lng: " + mLastPosition.longitude + " Count: " + cursor.getCount());

        cursor.close();
        */
    }

    // Move to activity, because manual reload happens there?
    private void reloadEvents() {
        // triggers a data sync
        Intent sendIntent = new Intent(getActivity(), FetchDataService.class);
        getActivity().startService(sendIntent);
    }
}
