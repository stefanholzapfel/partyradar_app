package at.fhtw.partyradar.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import at.fhtw.partyradar.data.EventDbHelper;
import at.fhtw.partyradar.data.EventContract.EventEntry;

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(EventDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new EventDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        EventDbHelper dbHelper = new EventDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertEquals(true, db.isOpen());

        ContentValues testValues = createDetailedEventValues("123");

        long locationRowId;
        locationRowId = db.insert(EventEntry.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

                // Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        // the round trip.

        Cursor cursor = db.query(
                EventEntry.TABLE_NAME, // Table to Query
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);
        dbHelper.close();
    }

    static ContentValues createEventValues(String _id) {
        ContentValues eventValues = new ContentValues();
        double lat = 48.21;
        double lng = 16.39;

        eventValues.put(EventEntry.COLUMN_EVENT_ID, _id);
        eventValues.put(EventEntry.COLUMN_TITLE, "Jungle Feever Party!");
        eventValues.put(EventEntry.COLUMN_START, "201412231111");
        eventValues.put(EventEntry.COLUMN_END, "201412231113");
        eventValues.put(EventEntry.COLUMN_KEYWORDS, "#Jungle#Feever#Party");
        eventValues.put(EventEntry.COLUMN_LONGITUDE, lng);
        eventValues.put(EventEntry.COLUMN_LATITUDE, lat);
        eventValues.put(EventEntry.COLUMN_COSLNG, 1); //Math.cos(Utility.deg2rad(lng)));
        eventValues.put(EventEntry.COLUMN_SINLNG, 1); //Math.sin(Utility.deg2rad(lng)));
        eventValues.put(EventEntry.COLUMN_COSLAT, 1); //Math.cos(Utility.deg2rad(lat)));
        eventValues.put(EventEntry.COLUMN_SINLAT, 1); //Math.sin(Utility.deg2rad(lat)));
        eventValues.put(EventEntry.COLUMN_LOCATION_NAME, "Jungle Bar");
        eventValues.put(EventEntry.COLUMN_ZIPCODE, "1010");
        eventValues.put(EventEntry.COLUMN_CITY, "Vienna");
        eventValues.put(EventEntry.COLUMN_ATTENDEECOUNT, 321);
        return eventValues;
    }

    static ContentValues createDetailedEventValues(String _id) {
        ContentValues eventValues = new ContentValues();
        double lat = 48.21;
        double lng = 16.39;

        eventValues.put(EventEntry.COLUMN_EVENT_ID, _id);
        eventValues.put(EventEntry.COLUMN_TITLE, "Jungle Feever Party!");
        eventValues.put(EventEntry.COLUMN_START, "201412231111");
        eventValues.put(EventEntry.COLUMN_END, "201412231113");
        eventValues.put(EventEntry.COLUMN_KEYWORDS, "#Jungle#Feever#Party");
        eventValues.put(EventEntry.COLUMN_LONGITUDE, lng);
        eventValues.put(EventEntry.COLUMN_LATITUDE, lat);
        eventValues.put(EventEntry.COLUMN_COSLNG, 1); //Math.cos(Utility.deg2rad(lng)));
        eventValues.put(EventEntry.COLUMN_SINLNG, 1); //Math.sin(Utility.deg2rad(lng)));
        eventValues.put(EventEntry.COLUMN_COSLAT, 1); //Math.cos(Utility.deg2rad(lat)));
        eventValues.put(EventEntry.COLUMN_SINLAT, 1); //Math.sin(Utility.deg2rad(lat)));
        eventValues.put(EventEntry.COLUMN_LOCATION_NAME, "Jungle Bar");
        eventValues.put(EventEntry.COLUMN_ZIPCODE, "1010");
        eventValues.put(EventEntry.COLUMN_CITY, "Vienna");
        eventValues.put(EventEntry.COLUMN_ATTENDEECOUNT, 321);
        eventValues.put(EventEntry.COLUMN_DESCRIPTION , "Best Jungle party in town!");
        eventValues.put(EventEntry.COLUMN_WEBSITE , "http://junglefeever.com");
        eventValues.put(EventEntry.COLUMN_MAX_ATTENDS , 400);
        eventValues.put(EventEntry.COLUMN_ADDRESS , "Salzgries");
        eventValues.put(EventEntry.COLUMN_ADDRESS_ADDITIONS , "1");
        eventValues.put(EventEntry.COLUMN_COUNTRY, "Austria");
        return eventValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
