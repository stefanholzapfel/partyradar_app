package at.fhtw.partyradar.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import at.fhtw.partyradar.data.EventContract.EventEntry;

/**
 * Created by Stefan on 27.12.2014.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                EventEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                EventEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {

        ContentValues testValues = TestDb.createEventValues("123a");

        Uri locationUri = mContext.getContentResolver().insert(EventEntry.CONTENT_URI, testValues);
        String locationRowId = EventEntry.getIdFromUri(locationUri);

        // Verify we got a row back.
        assertTrue(!locationRowId.equals("")  && locationRowId != null );

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                EventEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        //TestDb.validateCursor(cursor, testValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                EventEntry.buildEventUri("123a"),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        //TestDb.validateCursor(cursor, testValues);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                EventEntry.buildEventWithinArea("48.192835", "16.438592", "5"),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        assertTrue(cursor.getCount() > 0);

        /*
        cursor.moveToFirst();

        Double lat = cursor.getDouble(cursor.getColumnIndex( EventEntry.COLUMN_LATITUDE));
        Double lng = cursor.getDouble(cursor.getColumnIndex( EventEntry.COLUMN_LONGITUDE));

        Double distance = cursor.getDouble(cursor.getColumnIndex( EventEntry.COLUMN_DISTANCE));
        Double distance_km = Utility.rad2km(distance);
        Double distance_real = Utility.getDistance(48.192835, 16.438592, 48.216618, 16.393059);
        */
    }

    public void testGetType() {
        // content://at.fhtw.partyradar.app/event/
        String type = mContext.getContentResolver().getType(EventEntry.CONTENT_URI);

        assertEquals(EventEntry.CONTENT_TYPE, type);

        String testLocation = "123a";
        // content://at.fhtw.partyradar.app/event/123a
        type = mContext.getContentResolver().getType(EventEntry.buildEventUri(testLocation));
        assertEquals(EventEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createDetailedEventValues("1234b");

        Uri locationUri = mContext.getContentResolver().insert(EventEntry.CONTENT_URI, values);
        String locationRowId = EventEntry.getIdFromUri(locationUri);

        // Verify we got a row back.
        assertTrue(!locationRowId.equals("")  && locationRowId != null );
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(EventEntry.COLUMN_EVENT_ID, locationRowId);
        updatedValues.put(EventEntry.COLUMN_KEYWORDS, "#TESTTTTTTT!");

        int count = mContext.getContentResolver().update(EventEntry.CONTENT_URI, updatedValues, EventEntry.COLUMN_EVENT_ID + "= ?", new String[] { locationRowId});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                EventEntry.buildEventUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        //TestDb.validateCursor(cursor, updatedValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }
}
