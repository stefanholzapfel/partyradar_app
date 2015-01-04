package at.fhtw.partyradar.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

import at.fhtw.partyradar.LoginActivity;
import at.fhtw.partyradar.R;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private static final String LOG_TAG = AuthenticatorService.class.getSimpleName();
    private final Context mContext;

    public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String AUTH_TYPE = "AUTH_TYPE";
    public final static String ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG, "Adding account");

        Intent intent = new Intent(mContext, LoginActivity.class);

        intent.putExtra(ACCOUNT_TYPE, accountType);
        intent.putExtra(AUTH_TYPE, authTokenType);
        intent.putExtra(IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG, "getAuthToken");

        AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String password = accountManager.getPassword(account);
            if (password != null) {
                try {
                    Log.d(LOG_TAG, "re-authenticating with the existing password");
                    authToken = TokenHelper.getToken(account.name, password);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ACCOUNT_TYPE, account.type);
        intent.putExtra(AUTH_TYPE, authTokenType);
        intent.putExtra(ACCOUNT_NAME, account.name);

        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (TokenHelper.TOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return mContext.getString(R.string.token_full_access_label);
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

}
