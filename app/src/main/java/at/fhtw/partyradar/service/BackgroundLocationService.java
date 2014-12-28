package at.fhtw.partyradar.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import at.fhtw.partyradar.helper.Utility;

/**
 * Created by Stefan on 27.12.2014.
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    protected static final String TAG = "BackgroundLocationService";
    public static final String BROADCAST_ACTION = "at.fhtw.partyradar.LOCATIONUPDATE";
    public static final String BROADCAST_DATA = "at.fhtw.partyradar.LOCATIONDATA";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // build the API client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // set up the location updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Utility.storePositionInStorage(this, mLastLocation);
            broadcastLocation(mLastLocation);
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void onDisconnected() {
        //Log.i(TAG, "Disconnected");
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, "Location update = Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());

            Utility.storePositionInStorage(this, location);
            broadcastLocation(location);
            mLastLocation = location;
        }
    }

    /**
     * broadcasts the location to all registered broadcast receivers
     * @param location
     */
    private void broadcastLocation(Location location) {
        Intent locationIntent = new Intent(BROADCAST_ACTION).putExtra(BROADCAST_DATA, location.getLatitude() + "|" + location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "BackgroundLocationService started");
        mGoogleApiClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "BackgroundLocationService destroyed");

        stopLocationUpdates();
        mGoogleApiClient.disconnect();

        super.onDestroy();
    }
}
