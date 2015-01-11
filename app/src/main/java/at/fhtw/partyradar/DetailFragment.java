package at.fhtw.partyradar;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import at.fhtw.partyradar.data.EventContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int DETAIL_LOADER = 0;

    //UI Elements
    public TextView mTitle;
    public TextView mLocation;
    public TextView mDescription;
    public TextView mWebsite;
    public TextView mStart;
    public TextView mEnd;
    public ImageView mPicture;

    public DetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("eventId")) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("eventId")) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.textView_title);
        mLocation = (TextView) rootView.findViewById(R.id.textView_location);
        mDescription = (TextView) rootView.findViewById(R.id.textView_description);
        mWebsite = (TextView) rootView.findViewById(R.id.textView_website);
        mStart = (TextView) rootView.findViewById(R.id.textView_start);
        mEnd = (TextView) rootView.findViewById(R.id.textView_end);
        mPicture = (ImageView) rootView.findViewById(R.id.imageView_partypic);

        new GetImage().execute(getArguments().getString("eventId"));
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loeader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id.

        // Specify needed columns.

        String[] colummns = {
                EventContract.EventEntry.COLUMN_EVENT_ID,
                EventContract.EventEntry.COLUMN_TITLE,
                EventContract.EventEntry.COLUMN_ADDRESS,
                EventContract.EventEntry.COLUMN_ADDRESS_ADDITIONS,
                EventContract.EventEntry.COLUMN_LOCATION_NAME,
                EventContract.EventEntry.COLUMN_CITY,
                EventContract.EventEntry.COLUMN_ZIPCODE,
                EventContract.EventEntry.COLUMN_DESCRIPTION,
                EventContract.EventEntry.COLUMN_WEBSITE,
                EventContract.EventEntry.COLUMN_START,
                EventContract.EventEntry.COLUMN_END
        };

        String eventStr = getArguments().getString("eventId");

        Uri EventUri = EventContract.EventEntry.buildEventUri(eventStr);
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                EventUri,
                colummns,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            mTitle.setText(data.getString(1));
            String location = data.getString(4) + " - " + data.getString(2);
            if (data.getString(3) != null  && !data.getString(3).equals("null"))
                        location += " " + data.getString(3);
            location += " " + data.getString(6) + " " + data.getString(5);
            mLocation.setText(location);
            mDescription.setText(data.getString(7));
            if (data.getString(8) != null && !data.getString(8).equals("null"))
            {
                mWebsite.setVisibility(View.VISIBLE);
                mWebsite.setText(data.getString(8));
            }
            else
                mWebsite.setVisibility(View.GONE);
            mStart.setText(data.getString(9));
            mEnd.setText(data.getString(10));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private class GetImage extends AsyncTask<String,Void,Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... params) {
            Log.v(LOG_TAG, "IN DO IN BACKGROUND");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String eventsJsonStr = null;
            String imagestring = "";

            try {
                final String EVENTS_BASE_URL = "http://wi-gate.technikum-wien.at:60349/api/App/GetEventPicture?";
                final String QUERY_PARAM_ID = "eventId";

                Uri builtUri = Uri.parse(EVENTS_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM_ID, params[0])
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                eventsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                JSONObject eventsJson = new JSONObject(eventsJsonStr);
                JSONArray eventsArray = eventsJson.getJSONArray("Result");
                JSONObject event = eventsArray.getJSONObject(0);
                imagestring = event.getString("Image");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
            Log.v(LOG_TAG, "STRING: " + imagestring);
            byte[] decodedString = Base64.decode(imagestring, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedByte;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mPicture.setImageBitmap(result);
        }

    }
}