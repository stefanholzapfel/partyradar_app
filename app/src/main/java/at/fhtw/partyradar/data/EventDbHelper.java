package at.fhtw.partyradar.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.Key;

import at.fhtw.partyradar.data.EventContract.EventEntry;
import at.fhtw.partyradar.data.EventContract.KeywordEntry;

public class EventDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "event.db";

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EventEntry.COLUMN_EVENT_ID + " TEXT NOT NULL," +
                EventEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                EventEntry.COLUMN_START + " TEXT NOT NULL, " +
                EventEntry.COLUMN_END + " TEXT NOT NULL, " +
                EventEntry.COLUMN_KEYWORDS + " TEXT NOT NULL, " +
                EventEntry.COLUMN_LONGITUDE + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_LATITUDE + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_COSLNG + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_SINLNG + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_COSLAT + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_SINLAT + " DOUBLE NOT NULL, " +
                EventEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                EventEntry.COLUMN_ZIPCODE + " TEXT NOT NULL, " +
                EventEntry.COLUMN_CITY + " TEXT NOT NULL, " +
                EventEntry.COLUMN_ATTENDEECOUNT + " INTEGER NOT NULL," +
                EventEntry.COLUMN_DESCRIPTION + " TEXT NULL, " +
                EventEntry.COLUMN_WEBSITE + " TEXT NULL, " +
                EventEntry.COLUMN_MAX_ATTENDS + " INTEGER NULL, " +
                EventEntry.COLUMN_ADDRESS + " TEXT NULL, " +
                EventEntry.COLUMN_ADDRESS_ADDITIONS + " TEXT NULL, " +
                EventEntry.COLUMN_COUNTRY + " TEXT NULL" +
                " );";
        final String SQL_CREATE_KEYWORD_TABLE = "CREATE TABLE " + KeywordEntry.TABLE_NAME + " (" +
                KeywordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KeywordEntry.COLUMN_KEYWORD_ID + " TEXT UNIQUE NOT NULL, " +
                KeywordEntry.COLUMN_LABEL + " TEXT UNIQUE NOT NULL, " +
                "UNIQUE (" + KeywordEntry.COLUMN_KEYWORD_ID + ") ON CONFLICT IGNORE, " +
                "UNIQUE (" + KeywordEntry.COLUMN_LABEL + ") ON CONFLICT IGNORE );";


        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_KEYWORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + KeywordEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
