package at.fhtw.partyradar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Adapter;
import android.widget.ListAdapter;

import com.google.android.gms.maps.model.LatLng;

import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.helper.Utility;

public class SelectEventFragment extends DialogFragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EVENT_LOADER = 1;
    private LatLng mLastPosition;
    private SelectEventAdapter mSelectEventAdapter;

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
        // get the latest known position
        mLastPosition = Utility.getPositionFromStorage(getActivity());
        mSelectEventAdapter = new SelectEventAdapter(getActivity(), null, 0);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.title_select_event)
                .setAdapter(mSelectEventAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });

        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        getLoaderManager().initLoader(EVENT_LOADER, null, this);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Uri weatherForLocationUri = EventContract.EventEntry.buildEventWithinArea(mMapCenter.latitude, mMapCenter.longitude, mVisibleRadius);
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
        mSelectEventAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Log.d(LOG_TAG, "onLoaderReset");
        mSelectEventAdapter.swapCursor(null);
    }
}
