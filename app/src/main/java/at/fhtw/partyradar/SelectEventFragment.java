package at.fhtw.partyradar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import at.fhtw.partyradar.authentication.AuthenticationHelper;
import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.helper.Utility;

public class SelectEventFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String LOG_TAG = SelectEventFragment.class.getSimpleName();

    private LatLng mLastPosition;
    private static final int EVENT_LOADER = 1;

    private SelectEventAdapter mSelectEventAdapter;
    private Cursor mCursor;

    // Definition of the database's columns used by the loader
    private static final String[] EVENT_COLUMNS = {
            EventContract.EventEntry._ID,
            EventContract.EventEntry.COLUMN_EVENT_ID,
            EventContract.EventEntry.COLUMN_TITLE,
            EventContract.EventEntry.COLUMN_LOCATION_NAME,
    };

    // Indices tied to the EVENT_COLUMNS. If EVENT_COLUMNS changes, these have to be adapted.
    public static final int COL_ID = 0;
    public static final int COL_EVENT_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_LOCATION_NAME = 3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Log.d(LOG_TAG, "onCreateDialog");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_selectevent, null);

        // get the latest known position
        mLastPosition = Utility.getPositionFromStorage(getActivity());
        mSelectEventAdapter = new SelectEventAdapter(getActivity(), null, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_select_event)
                .setView(view)
                .setAdapter(mSelectEventAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        attendEvent(which);
                    }
                });

        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        getLoaderManager().initLoader(EVENT_LOADER, null, this);

        return builder.create();
    }

    private void attendEvent(int position) {
        mCursor.moveToPosition(position);
        final String eventId = mCursor.getString(COL_EVENT_ID);
        final String authToken = AuthenticationHelper.getToken(getActivity(), false);
        //Log.d(LOG_TAG, "Selected title: " + mCursor.getString(COL_TITLE));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                AuthenticationHelper.logInEvent(eventId, authToken);
                return null;
            }
        }.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: restrict to events in very near distance
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
        mCursor = data;
        mSelectEventAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Log.d(LOG_TAG, "onLoaderReset");
        mSelectEventAdapter.swapCursor(null);
    }
}
