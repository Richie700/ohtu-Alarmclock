package ohtu.beddit.web;

import android.content.Context;
import android.os.Build;
import android.text.AndroidCharacter;
import android.util.Log;
import ohtu.beddit.io.PreferenceService;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class BedditConnectorImpl implements BedditConnector {

    private static final String TAG = "BedditConnectorImpl";
    private static final int GINGERBREAD = 9;

    public BedditConnectorImpl() {
        disableConnectionReuseIfNecessary();
    }

    // HTTP connection reuse which was buggy pre-gingerbread
    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < GINGERBREAD) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public String getUserJson(Context context) throws BedditConnectionException {
        return queryForJson(context, "", false);
    }

    public String getWakeUpJson(Context context, String date) throws BedditConnectionException {
        String username = PreferenceService.getUsername(context);
        String query = username + "/" + date + "/sleep";
        return queryForJson(context, query, false); //add
    }

    public String getQueueStateJson(Context context, String date) throws BedditConnectionException {
        String username = PreferenceService.getUsername(context);
        String query = username + "/" + date + "/sleep/queue_for_analysis";
        return queryForJson(context, query, false); //add
    }

    //need to check if POSTing actually works as it is:
    public String requestDataAnalysis(Context context, String date) throws BedditConnectionException {
        String username = PreferenceService.getUsername(context);
        String query = username + "/" + date + "/sleep/queue_for_analysis";
        return queryForJson(context, query, true); //add
    }

    private String queryForJson(Context context, String query, boolean doPost) throws BedditConnectionException {
        String url = getQueryURL(context, query);
        return getJsonFromServer(url, doPost);
    }

    //forms query url using user name from preferences
    private String getQueryURL(Context context, String query) {
        String token = PreferenceService.getToken(context);
        return "https://api.beddit.com/api2/user/" + query + "?access_token=" + token;
    }

    private URL stringToURL(String url) throws BedditConnectionException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new BedditConnectionException("url was malformed: "+url);
        }
    }

    @Override
    public String getJsonFromServer(String url, boolean doPost) throws BedditConnectionException {
        String response = "";
        HttpsURLConnection connection = null;
        InputStream inputStream = null;

        try {
            connection = connect(stringToURL(url), connection, doPost);
            if (connection.getResponseCode() < 400) { //no error
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            response = readFromStream(inputStream);
        } catch (Throwable e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new BedditConnectionException(e.getMessage());
        } finally {
            closeConnections(connection, inputStream);
        }

        if (response.equals("")) {
            throw new BedditConnectionException("Empty response from Beddit");
        }

        return response;
    }

    private HttpsURLConnection connect(URL url, HttpsURLConnection connection, boolean do_post) throws IOException {
        connection = (HttpsURLConnection) url.openConnection();
        System.setProperty("http.keepAlive", "false");
        if (do_post) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } else {
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
        }
        connection.connect();
        return connection;
    }

    private String readFromStream(InputStream inputStream) {
        String response = "";
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine() ) {
            response += scanner.nextLine();
        }
        return response;
    }

    private void closeConnections(HttpsURLConnection connection, InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        if (connection != null) {
            connection.disconnect();
        }
    }

}
