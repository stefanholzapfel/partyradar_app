package at.fhtw.partyradar.authentication;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenHelper {

    private static final String LOG_TAG = TokenHelper.class.getSimpleName();

    public static final String TOKEN_TYPE_FULL_ACCESS = "Full access";

    public static String getToken(String userName, String password) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://wi-gate.technikum-wien.at:60349/Token");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
            nameValuePairs.add(new BasicNameValuePair("username", userName));
            nameValuePairs.add(new BasicNameValuePair("password", password));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) return null;

            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject tokenJson = new JSONObject(responseStr);

            if (tokenJson != null) return tokenJson.getString("access_token");

        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, e.getMessage(), e);

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }
}