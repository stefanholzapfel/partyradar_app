package at.fhtw.partyradar.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.helper.Utility;

public class FetchDataService extends IntentService {

    public final String LOG_TAG = FetchDataService.class.getSimpleName();

    public FetchDataService() {
        super("FetchDataService");
    }

    static public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, FetchDataService.class);
            context.startService(sendIntent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "Starting update");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String eventsJsonStr = null;

        // range filter
        LatLng lastPosition = Utility.getPositionFromStorage(this);
        Double radius = 30000.0;        // in meters

        Calendar c = Calendar.getInstance();
        Date dt = new Date();
        c.setTime(dt);

        // time frame filter
        c.add(Calendar.DATE, -20);
        Date startDate = c.getTime();
        c.add(Calendar.DATE, 40);
        Date endDate = c.getTime();

        String startDateStr = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
        String endDateStr = new SimpleDateFormat("yyyy-MM-dd").format(endDate);

        try {
            final String EVENTS_BASE_URL = "http://wi-gate.technikum-wien.at:60349/api/App/GetEvents?";
            final String QUERY_PARAM_LAT = "latitude";
            final String QUERY_PARAM_LNG = "longitude";
            final String QUERY_PARAM_RADIUS = "radius";
            final String QUERY_PARAM_START = "start";
            final String QUERY_PARAM_END = "end";

            Uri builtUri = Uri.parse(EVENTS_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM_LAT, Double.toString(lastPosition.latitude))
                    .appendQueryParameter(QUERY_PARAM_LNG, Double.toString(lastPosition.longitude))
                    .appendQueryParameter(QUERY_PARAM_RADIUS, Double.toString(radius))
                    .appendQueryParameter(QUERY_PARAM_START, startDateStr)
                    .appendQueryParameter(QUERY_PARAM_END, endDateStr)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                return;
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
                return;
            }

            eventsJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;

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

        //Log.d(LOG_TAG, eventsJsonStr);
        // TODO: needs to be fixed in the service
        eventsJsonStr  = "{\"ArrayOfEvent\": " + eventsJsonStr + "}";

        final String EVENTS_ARRAY = "ArrayOfEvent";
        final String EVENTS_EVENTID = "EventId";
        final String EVENTS_TITLE = "Title";
        final String EVENTS_START = "Start";
        final String EVENTS_END = "End";
        final String EVENTS_DESCRIPTION = "Description";
        final String EVENTS_WEBSITE = "Website";
        final String EVENTS_KEYWORDS = "Keywords";
        final String EVENTS_KEYWORDLABEL = "Label";
        final String EVENTS_ATTENDEECOUNT = "AttendeeCount";
        final String EVENTS_LATITUDE = "Latitude";
        final String EVENTS_LONGITUDE = "Longitude";
        //final String EVENTS_LOCATIONID = "LocationId";
        final String EVENTS_LOCATIONNAME = "LocationName";
        final String EVENTS_MAXATTENDS = "MaxAttends";
        final String EVENTS_ADDRESS = "Adress";
        final String EVENTS_ADDRESSADDITIONS = "AdressAdditions";
        final String EVENTS_CITY = "City";
        final String EVENTS_ZIPCODE = "ZipCode";
        final String EVENTS_COUNTRY = "Country";

        try {
            JSONObject eventsJson = new JSONObject(eventsJsonStr);
            JSONArray eventsArray = eventsJson.getJSONArray(EVENTS_ARRAY);

            Vector<ContentValues> cVVector = new Vector<>(eventsArray.length());

            for(int i = 0; i < eventsArray.length(); i++) {

                String eventId;
                String title;
                String start;
                String end;
                String description;
                String website;
                String keywords = "";
                int attendeeCount;
                double latitude;
                double longitude;
                //String locationId;
                String locationName;
                int maxAttends;
                String address;
                String addressAdditions;
                String city;
                String zipCode;
                String country;

                JSONObject event = eventsArray.getJSONObject(i);

                eventId = event.getString(EVENTS_EVENTID);
                title = event.getString(EVENTS_TITLE);
                start = Utility.dbDate2Text(event.getString(EVENTS_START));
                end = Utility.dbDate2Text(event.getString(EVENTS_END));
                description = event.getString(EVENTS_DESCRIPTION);
                website = event.getString(EVENTS_WEBSITE);
                attendeeCount = event.getInt(EVENTS_ATTENDEECOUNT);
                latitude = event.getDouble(EVENTS_LATITUDE);
                longitude = event.getDouble(EVENTS_LONGITUDE);
                //locationId = event.getString(EVENTS_LOCATIONID);
                locationName = event.getString(EVENTS_LOCATIONNAME);
                maxAttends = event.getInt(EVENTS_MAXATTENDS);
                address = event.getString(EVENTS_ADDRESS);
                addressAdditions = event.getString(EVENTS_ADDRESSADDITIONS);
                city = event.getString(EVENTS_CITY);
                zipCode = event.getString(EVENTS_ZIPCODE);
                country = event.getString(EVENTS_COUNTRY);

                JSONArray keywordArray = event.getJSONArray(EVENTS_KEYWORDS);

                for(int j = 0; j < keywordArray.length(); j++) {
                    JSONObject keywordsObject = keywordArray.getJSONObject(j);
                    keywords += "#" + keywordsObject.getString(EVENTS_KEYWORDLABEL);
                }

                ContentValues eventValues = new ContentValues();

                eventValues.put(EventContract.EventEntry.COLUMN_EVENT_ID, eventId);
                eventValues.put(EventContract.EventEntry.COLUMN_TITLE, title);
                eventValues.put(EventContract.EventEntry.COLUMN_START, start);
                eventValues.put(EventContract.EventEntry.COLUMN_END, end);
                eventValues.put(EventContract.EventEntry.COLUMN_KEYWORDS, keywords);
                eventValues.put(EventContract.EventEntry.COLUMN_LONGITUDE, longitude);
                eventValues.put(EventContract.EventEntry.COLUMN_LATITUDE, latitude);
                eventValues.put(EventContract.EventEntry.COLUMN_COSLNG, Math.cos(Utility.deg2rad(longitude)));
                eventValues.put(EventContract.EventEntry.COLUMN_SINLNG, Math.sin(Utility.deg2rad(longitude)));
                eventValues.put(EventContract.EventEntry.COLUMN_COSLAT, Math.cos(Utility.deg2rad(latitude)));
                eventValues.put(EventContract.EventEntry.COLUMN_SINLAT, Math.sin(Utility.deg2rad(latitude)));
                eventValues.put(EventContract.EventEntry.COLUMN_LOCATION_NAME, locationName);
                eventValues.put(EventContract.EventEntry.COLUMN_ZIPCODE, zipCode);
                eventValues.put(EventContract.EventEntry.COLUMN_CITY, city);
                eventValues.put(EventContract.EventEntry.COLUMN_ATTENDEECOUNT, attendeeCount);
                eventValues.put(EventContract.EventEntry.COLUMN_DESCRIPTION , description);
                eventValues.put(EventContract.EventEntry.COLUMN_WEBSITE , website);
                eventValues.put(EventContract.EventEntry.COLUMN_MAX_ATTENDS , maxAttends);
                eventValues.put(EventContract.EventEntry.COLUMN_ADDRESS , address);
                eventValues.put(EventContract.EventEntry.COLUMN_ADDRESS_ADDITIONS , addressAdditions);
                eventValues.put(EventContract.EventEntry.COLUMN_COUNTRY, country);

                cVVector.add(eventValues);
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                // delete all events in database
                getApplication().getContentResolver().delete(EventContract.EventEntry.CONTENT_URI, null, null);

                // import
                getApplication().getContentResolver().bulkInsert(EventContract.EventEntry.CONTENT_URI, cvArray);

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "Finished update");
    }
}
