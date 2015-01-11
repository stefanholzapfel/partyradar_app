package at.fhtw.partyradar;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import at.fhtw.partyradar.authentication.AccountAuthenticator;
import at.fhtw.partyradar.authentication.AuthenticationHelper;

public class LoginActivity extends AccountAuthenticatorActivity {

    protected static final String LOG_TAG = LoginActivity.class.getSimpleName();

    public final static String PARAM_USER_PASS = "USER_PASS";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(getBaseContext());
        mAuthTokenType = getIntent().getStringExtra(AccountAuthenticator.AUTHORIZATION_TYPE);

        if (mAuthTokenType == null) mAuthTokenType = AuthenticationHelper.TOKEN_TYPE_FULL_ACCESS;

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public void submit() {
        final String userName = ((TextView) findViewById(R.id.login_username)).getText().toString();
        final String userPass = ((TextView) findViewById(R.id.login_password)).getText().toString();
        final String accountType = getString(R.string.auth_account_type);

        new AsyncTask<String, Void, Intent>() {
            @Override
            protected Intent doInBackground(String... params) {
                Log.d(LOG_TAG, "started authenticating");

                String authToken;
                Bundle data = new Bundle();

                try {
                    authToken = AuthenticationHelper.requestTokenFromService(userName, userPass);

                    if (authToken == null)
                        // something went wrong, possibly because of wrong user / pass
                        data.putString(KEY_ERROR_MESSAGE, getString(R.string.text_wrong_user_password));

                    // preparing data for later processing
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                    data.putString(PARAM_USER_PASS, userPass);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }

                Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    // something went wrong, show error
                    Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                } else {
                    // everything fine, let's log-in (create account)
                    finishLogin(intent);
                }
            }
        }.execute();
    }

    private void finishLogin(Intent intent) {
        Log.d(LOG_TAG, "finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);

        Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(AccountAuthenticator.IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(LOG_TAG, "finishLogin: addAccountExplicitly");

            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authTokenType = mAuthTokenType;

            // create new account, and then store the token
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authTokenType, authToken);

        } else {
            Log.d(LOG_TAG, "finishLogin: setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        // settling and finishing
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }
}
