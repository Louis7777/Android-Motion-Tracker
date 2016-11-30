package tracker.motion.androidmotiontracker;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *  This class is responsible for communication with the server. It receives the nearby devices and store them to local DB
 *
 *
 */
public class ReceiveDevicesLocationsTask extends AsyncTask<String, Void, Integer>
{
    private Activity activityContext;
    protected StringBuilder apiResponse;
    public AsyncResponse delegate=null;
    private Location deviceLocation;

    /**
     *  Constructor.
     *
     */
    public ReceiveDevicesLocationsTask(Activity context,Location loc)
    {
        activityContext = context;
        apiResponse = new StringBuilder();
        // A location can consist of a latitude, longitude, timestamp,
        // and other information such as bearing, altitude and velocity.
        deviceLocation=loc;
    }



    // before running in the background
    @Override
    protected void onPreExecute()
    {

    }

    // thread running in the background
    @Override
    protected Integer doInBackground(String... urls)
    {
        if(GlobalHelpClass.isOnline(activityContext)==true)
        {

            if(deviceLocation==null)
                return 6;
            if(deviceLocation.getLatitude()==0.0 || deviceLocation.getLongitude()==0.0)
                return 7;

            String appointsUrl=GlobalHelpClass.getBaseUrl()+"find_device.php";


            try
            {

                URL url = new URL(appointsUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user_lat", Double.toString(deviceLocation.getLatitude())));
                params.add(new BasicNameValuePair("user_lng", Double.toString(deviceLocation.getLongitude())));
                params.add(new BasicNameValuePair("rad",String.valueOf(GlobalHelpClass.getMaxRadius())));

                conn.setRequestProperty("Accept", "application/json");
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();



                if (conn.getResponseCode() != 200) {
                    return 3;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                while ((output = br.readLine()) != null)
                {
                    apiResponse.append(output);
                }

                conn.disconnect();
                return 1;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                return 4;
            }
            catch (IOException e)
            {

                e.printStackTrace();
                return 5;
            }

        }
        else
            return 2;

    }

    @Override
    protected void onCancelled()
    {

    }

    // parse JSON array
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    // when the thread is completed and execution returns to the main thread
    @Override
    protected void onPostExecute(Integer result)
    {
        if(result==1)
        {
            try
            {
                if(apiResponse!=null)
                {
                    JSONObject jsonObject = new JSONObject(apiResponse.toString());
                    if(jsonObject!=null)
                    {
                        JSONArray jsonArr=jsonObject.getJSONArray("devices");
                        GlobalHelpClass.insertDevices(jsonArr);

                    }
                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if(result==2)
        {
            Toast.makeText(activityContext, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
        else if(result==3)
        {
            Toast.makeText(activityContext, R.string.network_error, Toast.LENGTH_LONG).show();
            Toast.makeText(activityContext, R.string.network_error_sol, Toast.LENGTH_LONG).show();

        }
        else if(result==4)
        {
            Toast.makeText(activityContext, "Server returns MalformedURLException : No valid URL", Toast.LENGTH_LONG).show();

        }
        else if(result==5)
        {
            Toast.makeText(activityContext, "Server returns IOException", Toast.LENGTH_LONG).show();

        }
        else if(result==6)
        {
            Toast.makeText(activityContext, "Device Location has not received", Toast.LENGTH_LONG).show();

        }
        else if(result==7)
        {
            Toast.makeText(activityContext, "Device Location has not updated", Toast.LENGTH_LONG).show();

        }

        delegate.receiveTaskCompleted();
    }

}
