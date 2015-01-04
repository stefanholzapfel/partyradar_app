package at.fhtw.partyradar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

        try {
            AccountManager mgr = AccountManager.get(this);
            Account[] accounts = mgr.getAccountsByType(getString(R.string.auth_account_type));
            Account acct = accounts[0];

            AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(acct, TokenHelper.TOKEN_TYPE_FULL_ACCESS, null, this, null, null);
            Bundle authTokenBundle = accountManagerFuture.getResult();
            String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();

            TextView textView_token = (TextView) findViewById(R.id.main_token);
            textView_token.setText(authToken);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
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
            AccountManager mgr = AccountManager.get(this);
            mgr.getAuthTokenByFeatures(getString(R.string.auth_account_type), TokenHelper.TOKEN_TYPE_FULL_ACCESS, null, this, null, null, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    Bundle bundle;

                    try {
                        bundle = future.getResult();
                        final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

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
