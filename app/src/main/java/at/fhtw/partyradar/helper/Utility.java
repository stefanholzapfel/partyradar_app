package at.fhtw.partyradar.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.fhtw.partyradar.R;

public class Utility {

    /**
     * returns the position from the storage (or returns the default position)
     * @param context context of the app
     * @return Latitude and Longitude of the position
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
     * @param context context of the app
     * @param location location to be stored
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
     * @return distance in meter
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;                        // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);       // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c * 1000; // Distance in meter
    }

    /**
     * converts a "database date" like returned by the service API to a text date
     * @param dbDate date as "yyyy-MM-dd'T'HH:mm:ss"
     * @return date as "yyyyMMddHHmm"
     */
    public static String dbDate2Text(String dbDate) {
        // 2014-12-31T23:00:00
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dt = df.parse(dbDate);
            return new SimpleDateFormat("yyyyMMddHHmm").format(dt);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * converts degree to rad
     */
    public static double deg2rad(double degree) {
        return degree * (Math.PI/180);
    }

    /**
     * concerts rad value to meters
     */
    public static double rad2meter(double rad) {
        return 6371 * Math.acos(rad) * 1000;
    }

    /**
     * returns a user friendly string of the distance (like '123 m' or '3.45 km')
     */
    public static String getFriendlyDistance(double meter) {
        if (meter < 1000) {
            return Integer.toString((int)meter) + " m";
        }
        else {
            return Double.toString(round(meter / 1000, 2)) + " km";
        }
    }

    /**
     * rounds a double
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * returns a user friendly date
     * @param date Date string in format as stored in dataprovider
     * @return string friendly date
     */
    public static String getFriendlyDate(String date) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
            Date dt = df.parse(date);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dt);
        } catch (Exception ex) {
            return "";
        }
    }

}
