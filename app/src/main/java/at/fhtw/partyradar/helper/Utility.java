package at.fhtw.partyradar.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;

import at.fhtw.partyradar.R;

public class Utility {

    /**
     * returns the position from the storage (or returns the default position)
     * @param context
     * @return
     */
    public static LatLng getPositionFromStorage(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lat = preferences.getString(context.getString(R.string.pref_last_location_lat), context.getString(R.string.default_location_lat));
        String lng = preferences.getString(context.getString(R.string.pref_last_location_lng), context.getString(R.string.default_location_lng));

        //Log.i("Utility", "Location retrieved from Preferences");

        return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
    }

    /**
     * stores a position in the storage
     * @param context
     * @param location
     */
    public static void storePositionInStorage(Context context, Location location) {
        if (location != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(context.getString(R.string.pref_last_location_lat), Double.toString(location.getLatitude()));
            editor.putString(context.getString(R.string.pref_last_location_lng), Double.toString(location.getLongitude()));
            editor.apply();

            //Log.i("Utility", "Location stored in Preferences");
        }
    }

    /**
     * calculates the distance between to LatLng points
     * @param lat1 latitude from point 1
     * @param lon1 longitude from point 1
     * @param lat2 latitude from point 2
     * @param lon2 longitude from point 1
     * @return distance in km
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    /**
     * converts degree to rad
     * @param degree
     * @return
     */
    public static double deg2rad(double degree) {
        return degree * (Math.PI/180);
    }

    /**
     * concerts rad value to kilometers
     * @param rad
     * @return
     */
    public static double rad2km(double rad) {
        return 6371 * Math.acos(rad);
    }

    /**
     * rounds a double
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
