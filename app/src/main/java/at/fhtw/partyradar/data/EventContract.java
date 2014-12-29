package at.fhtw.partyradar.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class EventContract {

    public static final String CONTENT_AUTHORITY = "at.fhtw.partyradar.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_EVENT = "event";
    public static final String PATH_EVENTAREA = "event_area";

    public static final class EventEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        // Columns for overview
        public static final String TABLE_NAME = "event";
        public static final String COLUMN_EVENT_ID = "eventID";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_START = "start";
        public static final String COLUMN_END = "end";
        public static final String COLUMN_KEYWORDS = "keywords";
        public static final String COLUMN_LONGITUDE = "lng";
        public static final String COLUMN_LATITUDE = "lat";
        public static final String COLUMN_COSLNG = "coslng";
        public static final String COLUMN_SINLNG = "sinlng";
        public static final String COLUMN_COSLAT = "coslat";
        public static final String COLUMN_SINLAT = "sinlat";
        public static final String COLUMN_LOCATION_NAME = "locationName";
        public static final String COLUMN_ZIPCODE = "zipCode";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_ATTENDEECOUNT = "attendeeCount";

        // Columns only for details
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_WEBSITE = "website";
        public static final String COLUMN_MAX_ATTENDS = "maxAttends";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_ADDRESS_ADDITIONS = "addressAdditions";
        public static final String COLUMN_COUNTRY = "country";

        // Columns returned by internal queries
        public static final String COLUMN_DISTANCE = "distance";

        private static final String QUERYPARAM_RADIUS = "radius";

        public static Uri buildEventUri(String id) {
            return Uri.parse(BASE_CONTENT_URI + "/" + PATH_EVENT + "/" + id);
        }

        public static Uri buildEventWithinArea(String lat, String lng, String radius) {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTAREA).appendQueryParameter(QUERYPARAM_RADIUS, radius).appendQueryParameter(COLUMN_LATITUDE, lat).appendQueryParameter(COLUMN_LONGITUDE, lng).build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getRadiusFromUri(Uri uri) {
            return uri.getQueryParameter(QUERYPARAM_RADIUS);
        }

        public static String getLngFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_LONGITUDE);
        }

        public static String getLatFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_LATITUDE);
        }
    }

}
