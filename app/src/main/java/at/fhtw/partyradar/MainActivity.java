package at.fhtw.partyradar;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import at.fhtw.partyradar.authentication.AuthenticationHelper;
import at.fhtw.partyradar.data.EventContract;
import at.fhtw.partyradar.service.BackgroundLocationService;
import at.fhtw.partyradar.service.FetchDataService;

public class MainActivity extends ActionBarActivity implements SelectEventFragment.NoticeDialogListener {

    protected static final String LOG_TAG = "MainActivity";

    private Intent mLocationServiceIntent;
    private PendingIntent mDataPendingIntent;
    private Menu mMenu;

    private String mLoggedEventId;
    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showListFragment(null);
        }

        // define the intent for the background location service (will be started later)
        mLocationServiceIntent = new Intent(this, BackgroundLocationService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Remove BackgroundService if not necessary for other issues
        // start background location service
        startService(mLocationServiceIntent);

        // set repeating data update task
        Intent alarmIntent = new Intent(this, FetchDataService.AlarmReceiver.class);
        mDataPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 600000, mDataPendingIntent);     // 10 minutes * 60 seconds * 1000 = 600000 milliseconds
    }

    @Override
    protected void onStop() {
        super.onStop();

        // stop the background location service
        stopService(mLocationServiceIntent);

        // stop the repeating data update
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(mDataPendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;

        // needs to be run here, since it is manipulating the actionbar menu
        runAutoLogin();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_select_event) {
            DialogFragment newFragment = new SelectEventFragment();
            newFragment.show(getSupportFragmentManager(), "select_event");
            return true;
        }

        if (id == R.id.action_refresh) {
            Intent sendIntent = new Intent(this, FetchDataService.class);
            startService(sendIntent);
            return true;
        }

        if (id == R.id.action_logOutEvent) {
            logOutFromEvent();
            return true;
        }

        if (id == R.id.action_logout) {
            AuthenticationHelper.logoutUser(this);

            // reload activity, so it is in clean logged-out state
            finish();
            startActivity(getIntent());

            return true;
        }

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttendEventClick(DialogFragment dialog) {
        showLoggedInEvent(this);
    }

    public void showListFragment(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.events_container, new EventListFragment())
                .commit();
    }

    public void showMapFragment(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.events_container, new EventMapFragment())
                .commit();
    }

    public void showLoginForm(View view) {
        try {
            Log.d(LOG_TAG, "showLoginForm");

            // using getAuthTokenByFeatures for logging in:
            // if there is already an account it is returning the token, or it is running through the login / account-creation process
            AccountManager accountManager = AccountManager.get(this);
            accountManager.getAuthTokenByFeatures(getString(R.string.auth_account_type), AuthenticationHelper.TOKEN_TYPE_FULL_ACCESS, null, this, null, null, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    Bundle bundle;

                    try {
                        Log.d(LOG_TAG, "getting Token from getAuthTokenByFeatures");

                        bundle = future.getResult();
                        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                        if (authToken != null) {
                            mAuthToken = authToken;
                            switchToLoggedInMode();
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }, null);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void runAutoLogin() {
        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... params) {
                if (params == null) return null;

                Context context = params[0];

                // get new token (by invalidating the old one)
                final String authToken = AuthenticationHelper.getToken(context, true);
                if (authToken == null) return null;

                mAuthToken = authToken;

                // needs to be run as runOnUiThread (since a lot of UI elements need to be updated)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchToLoggedInMode();
                    }
                });

                return null;
            }
        }.execute(this);
    }

    /**
     * switching various UI elements to logged-in mode
     */
    private void switchToLoggedInMode() {
        if (mMenu != null) {
            MenuItem item_attend = mMenu.findItem(R.id.action_select_event);
            item_attend.setVisible(true);

            MenuItem item_logout = mMenu.findItem(R.id.action_logout);
            item_logout.setVisible(true);
        }

        View area_loggedOut = findViewById(R.id.area_loggedOut);
        area_loggedOut.setVisibility(View.GONE);

        View area_loggedIn = findViewById(R.id.area_loggedIn);
        area_loggedIn.setVisibility(View.VISIBLE);

        TextView text_username = (TextView) findViewById(R.id.main_username);
        text_username.setText(AuthenticationHelper.getUsername(this));

        TextView text_loggedEventTitle = (TextView) findViewById(R.id.main_loggedEventTitle);
        text_loggedEventTitle.setText(getString(R.string.text_none));

        showLoggedInEvent(this);
    }

    /**
     * showing the title of the logged-in event
     * @param context context of the activity
     */
    private void showLoggedInEvent(final Context context) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                mLoggedEventId = AuthenticationHelper.getLoggedInEvent(mAuthToken);
                return mLoggedEventId;
            }

            @Override
            protected void onPostExecute(String eventId) {
                TextView text_loggedEventTitle = (TextView) findViewById(R.id.main_loggedEventTitle);

                String[] EVENT_COLUMNS = {
                        EventContract.EventEntry._ID,
                        EventContract.EventEntry.COLUMN_EVENT_ID,
                        EventContract.EventEntry.COLUMN_TITLE
                };

                Uri eventDetailUri = EventContract.EventEntry.buildEventUri(eventId);
                Cursor cursor = context.getContentResolver().query(
                        eventDetailUri,
                        EVENT_COLUMNS,
                        null,
                        null,
                        null
                );

                if (cursor.moveToFirst()) {
                    String eventTitle = cursor.getString(2);
                    text_loggedEventTitle.setText(eventTitle);

                    if (mMenu != null) {
                        MenuItem item_attend = mMenu.findItem(R.id.action_logOutEvent);
                        item_attend.setVisible(true);
                    }
                }
            }

        }.execute();
    }

    /**
     * logs the user out of an event
     */
    private void logOutFromEvent() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return AuthenticationHelper.logOutEvent(mLoggedEventId, mAuthToken);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    mLoggedEventId = null;

                    TextView text_loggedEventTitle = (TextView) findViewById(R.id.main_loggedEventTitle);
                    text_loggedEventTitle.setText(getString(R.string.text_none));
                }
            }
        }.execute();
    }
}
