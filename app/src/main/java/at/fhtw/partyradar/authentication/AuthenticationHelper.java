package at.fhtw.partyradar.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.fhtw.partyradar.R;

public class AuthenticationHelper {

    private static final String LOG_TAG = AuthenticationHelper.class.getSimpleName();

    public static final String TOKEN_TYPE_FULL_ACCESS = "Full access";

    /**
     * requests a new token from the service API for the given username and password
     * (this must not run in the main / UI thread, or an Exception will occur)
     * @param userName username of the account
     * @param password password of the account
     * @return token
     */
    public static String requestTokenFromService(String userName, String password) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://wi-gate.technikum-wien.at:60349/Token");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
            nameValuePairs.add(new BasicNameValuePair("username", userName));
            nameValuePairs.add(new BasicNameValuePair("password", password));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) return null;

            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject tokenJson = new JSONObject(responseStr);

            return tokenJson.getString("access_token");

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    /**
     * returns a token for authorizing API request
     * @param context context of the activity
     * @param invalidate if a new token should be requested from the service
     */
    public static String getToken(Context context, boolean invalidate) {
        if (context == null) return null;

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.auth_account_type));

        AccountManagerFuture<Bundle> accountManagerFuture;
        Bundle authTokenBundle;

        if (accounts.length == 1) {
            Account acct = accounts[0];

            try {
                if (invalidate) {
                    // getting token from account manager
                    accountManagerFuture = accountManager.getAuthToken(acct, AuthenticationHelper.TOKEN_TYPE_FULL_ACCESS, null, null, null, null);
                    authTokenBundle = accountManagerFuture.getResult();
                    String authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);

                    // invalidate it
                    accountManager.invalidateAuthToken(context.getString(R.string.auth_account_type), authToken);
                }

                // getting it again (this time it will be a new one requested from the service)
                accountManagerFuture = accountManager.getAuthToken(acct, AuthenticationHelper.TOKEN_TYPE_FULL_ACCESS, null, null, null, null);
                authTokenBundle = accountManagerFuture.getResult();
                return authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * logs the user into an event
     * (this must not run in the main / UI thread, or an Exception will occur)
     * @param eventId id of the event
     * @param authToken authentication token of the user
     */
    public static boolean logInEvent(String eventId, String authToken) {
        if (eventId == null || authToken == null) return false;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://wi-gate.technikum-wien.at:60349/api/App/LoginEvent?eventId=" + eventId);

        try {
            httpPost.setHeader("Authorization", "Bearer " + authToken);

            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 200) return true;

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return false;
    }

    /**
     * returns the (user)name of the account
     * @param context context of the app
     */
    public static String getUsername(Context context) {
        if (context == null) return null;

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.auth_account_type));

        if (accounts.length == 1) {
            Account acct = accounts[0];
            return acct.name;
        }

        return null;
    }
}
