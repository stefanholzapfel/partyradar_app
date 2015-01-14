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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;

import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.data.EventContract.EventEntry;
import at.fhtw.partyradar.helper.Utility;
import at.fhtw.partyradar.service.BackgroundLocationService;

public class EventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    protected static final String LOG_TAG = EventListFragment.class.getSimpleName();

    private EventListAdapter mEventListAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_key";
    private LatLng mLastPosition;
    private BroadcastReceiver mReceiver;

    private EventListFragment mThisContext;

    private static final int EVENT_LOADER = 0;

    // Definition of the database's columns used by the loader
    private static final String[] EVENT_COLUMNS = {
            EventEntry._ID,
            EventEntry.COLUMN_EVENT_ID,
            EventEntry.COLUMN_TITLE,
            EventEntry.COLUMN_LOCATION_NAME,
            EventEntry.COLUMN_ADDRESS,
            EventEntry.COLUMN_ADDRESS_ADDITIONS,
            EventEntry.COLUMN_CITY,
            EventEntry.COLUMN_KEYWORDS,
            EventEntry.COLUMN_ATTENDEECOUNT
    };

    // Indices tied to the EVENT_COLUMNS. If EVENT_COLUMNS changes, these have to be adapted.
    public static final int COL_ID = 0;
    public static final int COL_EVENT_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_LOCATION_NAME = 3;
    public static final int COL_ADDRESS = 4;
    public static final int COL_ADDRESS_ADDITIONS = 5;
    public static final int COL_CITY = 6;
    public static final int COL_KEYWORDS = 7;
    public static final int COL_ATTENDEECOUNT = 8;
    public static final int COL_DISTANCE = 9;

    private static final String[] KEYWORD_COLUMNS = {
            EventContract.KeywordEntry._ID,
            EventContract.KeywordEntry.COLUMN_KEYWORD_ID,
            EventContract.KeywordEntry.COLUMN_LABEL,
    };

    public static final int KEYWORD_COL_ID = 0;
    public static final int KEYWORD_COL_KEYWORDID = 1;
    public static final int KEYWORD_COL_LABEL = 2;

    private Bundle cursorParams;
    private Spinner sortBySpinner;

    private ArrayList<String> mKeywordList;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThisContext = this;

        // prepare and register for broadcasts of location updates
        IntentFilter intentFilter = new IntentFilter(BackgroundLocationService.BROADCAST_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String latLngString = intent.getStringExtra(BackgroundLocationService.BROADCAST_DATA);
                String[] latLngSplit = latLngString.split("\\|");

                mLastPosition = new LatLng(Double.parseDouble(latLngSplit[0]), Double.parseDouble(latLngSplit[1]));
                getLoaderManager().restartLoader(EVENT_LOADER, cursorParams, mThisContext);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);
        mKeywordList = new ArrayList<>();

        mLastPosition = Utility.getPositionFromStorage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // This Adapter will use the data from the loader and use it to populate the listView
        mEventListAdapter = new EventListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_eventlist, container, false);

        mListView = (ListView) rootView.findViewById(R.id.event_list_view);
        mListView.setAdapter(mEventListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mEventListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra("eventId", cursor.getString(COL_EVENT_ID));
                    startActivity(intent);
                }
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listView probably hasn't even been populated yet. Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        cursorParams = new Bundle();

        populateKeywordList();

        MultiAutoCompleteTextView mTagFilterAutoComplete = (MultiAutoCompleteTextView)
                rootView.findViewById(R.id.event_tag_autocomplete);

        mTagFilterAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        ArrayAdapter<String> mKeywordAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_expandable_list_item_1, mKeywordList);

        mTagFilterAutoComplete.setThreshold(1);
        mTagFilterAutoComplete.setAdapter(mKeywordAdapter);

        mTagFilterAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedKeyword = adapterView.getItemAtPosition(i).toString();
                filterEventData(selectedKeyword);
            }
        });


        /*EditText mTagFilterText = (EditText) rootView.findViewById(R.id.event_tag_filter);

        mTagFilterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                filterEventData(s);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });*/

        sortBySpinner = (Spinner) rootView.findViewById(R.id.event_sortby);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.eventlist_sortby, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sortBySpinner.setAdapter(adapter);
        sortBySpinner.setOnItemSelectedListener(this);

        return rootView;

    }

    private void populateKeywordList() {
        mKeywordList.clear();
        Cursor keywords = getActivity().getContentResolver().query(
                EventContract.KeywordEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        while (keywords.moveToNext()) {
            String label = keywords.getString(keywords.getColumnIndex(
                    KEYWORD_COLUMNS[KEYWORD_COL_LABEL]
            ));
            mKeywordList.add(keywords.getPosition(), label);
        }
    }

    private void filterEventData(String keyword) {
        if(cursorParams.getStringArrayList("keywords") == null) {
            ArrayList<String> keywords = new ArrayList<>();
            keywords.add(keyword);
            cursorParams.putStringArrayList("keywords", keywords);
        } else {
            cursorParams.getStringArrayList("keywords").add(keyword);
        }
        getLoaderManager().restartLoader(EVENT_LOADER, cursorParams, this);
    }

    @Deprecated
    private void filterEventData(CharSequence s) {
        if(!s.toString().equals("#") || s.length() > 1) {
            String[] keywords = s.toString().replaceFirst("^#", "").split("#");
            if(keywords.length > 0) {
                cursorParams.putStringArray("keywords", keywords);
                getLoaderManager().restartLoader(EVENT_LOADER, cursorParams, this);
            }
        } else if(s.length() == 0)
            getLoaderManager().restartLoader(EVENT_LOADER, null, this);
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

        String selection = null;
        String sortBy = null;

        if(args != null && !args.isEmpty()) {
            if(args.getStringArrayList("keywords") != null && !args.getStringArrayList("keywords").isEmpty()) {
                ArrayList<String> keywords = args.getStringArrayList("keywords");
                selection = EVENT_COLUMNS[COL_KEYWORDS] + " LIKE '%#" + keywords.get(0) + "%'";
                for (int i = 1; i < keywords.size() ; i++)
                    selection += " AND " + EVENT_COLUMNS[COL_KEYWORDS] + " LIKE '%" + keywords.get(i) + "%'";
            }
            if(args.getString("sortBy").length() > 0) {
                sortBy = args.getString("sortBy");
            }
        }
        // Creating and returning cursor
        return new CursorLoader(
            getActivity(),
            eventsWithinAreaUri,
            EVENT_COLUMNS, // columns to return
            selection, // cols for "where" clause
            null, // values for "where" clause
            sortBy  // sort order
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedSort = sortBySpinner.getItemAtPosition(i).toString();
        String sortBy = "";
        switch (selectedSort) {
            case "Distance":
                sortBy = EventEntry.COLUMN_DISTANCE;
                sortBy += " DESC";
                break;
            case "Name":
                sortBy = EVENT_COLUMNS[COL_TITLE];
                sortBy += " ASC";
                break;
            case "Attendees":
                sortBy = EVENT_COLUMNS[COL_ATTENDEECOUNT];
                sortBy += " ASC";
                break;
        }
        cursorParams.putString("sortBy", sortBy);
        getLoaderManager().restartLoader(EVENT_LOADER, cursorParams, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
