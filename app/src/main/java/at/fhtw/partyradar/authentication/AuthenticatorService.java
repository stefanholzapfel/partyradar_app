package at.fhtw.partyradar.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuthenticatorService extends Service {

    private static final String LOG_TAG = AuthenticatorService.class.getSimpleName();

    private AccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "Binding the service");
        return mAuthenticator.getIBinder();
    }

}
