package at.fhtw.partyradar;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import at.fhtw.partyradar.authentication.TokenHelper;
import at.fhtw.partyradar.service.BackgroundLocationService;
import at.fhtw.partyradar.service.FetchDataService;

public class MainActivity extends ActionBarActivity {

    protected static final String LOG_TAG = "MainActivity";

    private Intent mLocationServiceIntent;
    private PendingIntent mDataPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.events_container, new EventListFragment())
                    .commit();
        }

        mLocationServiceIntent = new Intent(this, BackgroundLocationService.class);

        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... params) {
                if (params == null) return null;

                Context context = params[0];
                final String authToken = TokenHelper.getToken(context, true);
                if (authToken == null) return null;

                // required, since the token is showed on the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView_token = (TextView) findViewById(R.id.main_token);
                        textView_token.setText(authToken);
                    }
                });

                return null;
            }
        }.execute(this);

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

        stopService(mLocationServiceIntent);

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(mDataPendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Intent sendIntent = new Intent(this, FetchDataService.class);
            startService(sendIntent);

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: Remove temporary method
    // TODO: Find a a better way to switch, so fragment is not created anew
    public void showListFragment(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.events_container, new EventListFragment())
                .commit();
    }

    // TODO: Remove temporary method
    // TODO: Find a a better way to switch, so fragment is not created anew
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
            accountManager.getAuthTokenByFeatures(getString(R.string.auth_account_type), TokenHelper.TOKEN_TYPE_FULL_ACCESS, null, this, null, null, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    Bundle bundle;

                    try {
                        Log.d(LOG_TAG, "getting Token from getAuthTokenByFeatures");

                        bundle = future.getResult();
                        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                        TextView textView_token = (TextView) findViewById(R.id.main_token);
                        textView_token.setText(authToken);

                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }
                    , null);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
