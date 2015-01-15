package at.fhtw.partyradar.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import at.fhtw.partyradar.helper.Utility;

public class EventProvider extends ContentProvider {

    // the URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EventDbHelper mOpenHelper;

    private static final int EVENT = 100;
    private static final int EVENT_ID = 101;
    private static final int EVENT_AREA = 102;

    private static final int KEYWORD = 200;
    private static final int KEYWORD_ID = 201;

    @Override
    public boolean onCreate() {
        mOpenHelper = new EventDbHelper(getContext());
        return true;
    }

     private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EventContract.CONTENT_AUTHORITY;

        // all possible URIs
        matcher.addURI(authority, EventContract.PATH_EVENT, EVENT);
        matcher.addURI(authority, EventContract.PATH_EVENT + "/*", EVENT_ID);
        matcher.addURI(authority, EventContract.PATH_EVENTAREA, EVENT_AREA);

        matcher.addURI(authority, EventContract.PATH_KEYWORD, KEYWORD);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENT:
                return EventContract.EventEntry.CONTENT_TYPE;
            case EVENT_AREA:
                return EventContract.EventEntry.CONTENT_TYPE;
            case EVENT_ID:
                return EventContract.EventEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "keyword"
            case KEYWORD: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.KeywordEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case KEYWORD_ID: {
                String queryId = EventContract.KeywordEntry.getIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.KeywordEntry.TABLE_NAME,
                        projection,
                        EventContract.KeywordEntry.COLUMN_KEYWORD_ID + " = '" + queryId + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "event"
            case EVENT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "event_area"
            case EVENT_AREA: {
                double radius = Double.parseDouble(EventContract.EventEntry.getRadiusFromUri(uri)) / 1000;      // for radius in km
                double lat = Double.parseDouble(EventContract.EventEntry.getLatFromUri(uri));
                double lng = Double.parseDouble(EventContract.EventEntry.getLngFromUri(uri));

                String fromTime = EventContract.EventEntry.getFromTimeFromUri(uri);
                String toTime = EventContract.EventEntry.getToTimeFromUri(uri);

                // required for the distance calculation (pre-calculating certain values, since SQLite cannot do that in the query)
                double coslat = Math.cos(Utility.deg2rad(lat));
                double sinlat = Math.sin(Utility.deg2rad(lat));
                double coslng = Math.cos(Utility.deg2rad(lng));
                double sinlng = Math.sin(Utility.deg2rad(lng));

                // extending the projection of the query by a new column for the distance
                String[] extendedProjection = new String[projection.length + 1];
                System.arraycopy(projection, 0, extendedProjection, 0, projection.length);
                extendedProjection[extendedProjection.length - 1] = "( " + coslat + " * coslat * (coslng * " + coslng + " + sinlng * " + sinlng + ") + " + sinlat + " * sinlat) AS " + EventContract.EventEntry.COLUMN_DISTANCE;

                // extending the selection of the query by filtering for the distance
                String extendedSelection = "";
                if (selection != null) extendedSelection = selection + " AND ";
                extendedSelection += "distance > " + Double.toString(Math.cos(radius / 6371.0));

                // extending the selection of the query by filtering for the time
                if (fromTime != null && !fromTime.isEmpty()) {
                    extendedSelection += " AND " + EventContract.EventEntry.COLUMN_START + " > '" + fromTime + "'";
                }

                if (toTime != null && !toTime.isEmpty()) {
                    extendedSelection += " AND " + EventContract.EventEntry.COLUMN_START + " < '" + toTime + "'";
                }

                retCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.EventEntry.TABLE_NAME,
                        extendedProjection,
                        extendedSelection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "event/*"
            case EVENT_ID: {
                String queryId = EventContract.EventEntry.getIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EventContract.EventEntry.TABLE_NAME,
                        projection,
                        EventContract.EventEntry.COLUMN_EVENT_ID + " = '" + queryId + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // the bulk insert is notifying these URIs, so we need to set the notification for it as well
        retCursor.setNotificationUri(getContext().getContentResolver(), EventContract.EventEntry.CONTENT_URI);
        retCursor.setNotificationUri(getContext().getContentResolver(), EventContract.KeywordEntry.CONTENT_URI);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case EVENT:
                rowsUpdated = db.update(EventContract.EventEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case KEYWORD: {
                rowsUpdated = db.update(EventContract.KeywordEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case EVENT:
                rowsDeleted = db.delete(EventContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case KEYWORD:
                rowsDeleted = db.delete(EventContract.KeywordEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case EVENT: {
                long _id = db.insert(EventContract.EventEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = EventContract.EventEntry.buildEventUri(values.getAsString(EventContract.EventEntry.COLUMN_EVENT_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case KEYWORD: {
                long _id = db.insert(EventContract.KeywordEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = EventContract.KeywordEntry.buildKeywordUri(values.getAsString(EventContract.KeywordEntry.COLUMN_KEYWORD_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case EVENT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(EventContract.EventEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case KEYWORD:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(EventContract.KeywordEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
