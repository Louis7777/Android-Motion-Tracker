package tracker.motion.androidmotiontracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class GlobalHelpClass {

    /**
     * Key stored at SharedPreferences which declare if user's session is active
     */
    public static final String DEVICE_STORED = "device_details";

    private static SQLiteDatabase db;
    private static HelpDatabase myDb;
    private static DatabaseActions dbActions;


    /**
     * max radius of outer circle
     */
    public static double maxRadius = 200;

    /**
     * current radius of outer circle
     */
    public static double currentRadius = 200;

    /**
     * Base url for API service
     */
    public static final String baseUrl = "http://computergate.gr/radar/";



    /**
     * Initialize of object which will be responsible for communication with SQLite DB
     */
    public static void initDatabase(Context ctx)
    {
        myDb= new HelpDatabase(ctx);
        db = myDb.getWritableDatabase();
        dbActions =new DatabaseActions(ctx,db);
        db.delete("devices", null, null);
    }

    /**
     * Global Function which requests the stored devices
     */
    public static Cursor retrievePoints()
    {
        return dbActions.retrievePoints();
    }

    /**
     * Global Function which requests the stored devices
     */
    public static String[] retrievePointsAsString()
    {
        return dbActions.retrievePointsAsStringArray();
    }

    public static void insertDevices(JSONArray jsonArr)
    {
        try
        {
            //delete all current records
            db.delete("devices", null, null);

            ContentValues cv = new ContentValues(4);
            for (int i = 0; i < jsonArr.length(); i++) {

                JSONObject device=(JSONObject) jsonArr.get(i);

                if(device.isNull("device_id") || device.isNull("lat") || device.isNull("lng") || device.isNull("D") || device.isNull("name"))
                    continue;
                else
                {
                    cv.put("device_id", device.getString("device_id"));
                    cv.put("lat", device.getString("lat"));
                    cv.put("lng", device.getString("lng"));
                    cv.put("distance", device.getString("D"));
                    cv.put("name", device.getString("name"));
                    db.insertOrThrow("devices", null,cv);
                }

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    public static String getDeviceUniqueID(Context ctx)
    {
        SharedPreferences storage= ctx.getSharedPreferences(GlobalHelpClass.DEVICE_STORED, 0);
        String deviceID = storage.getString("device_id", "null");

        //The device_id is not stored
        if(deviceID.equals("null"))
        {
            String newDeviceID =GlobalHelpClass.getPsuedoUniqueID();
            SharedPreferences.Editor editor = storage.edit();
            editor.putString("device_id", newDeviceID);
            return newDeviceID;
        }
        else
            return deviceID;
    }



    public static String getPsuedoUniqueID()
    {
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their phone or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.

        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" +
                (Build.BOARD.length() % 10)
                + (Build.BRAND.length() % 10)
                + (Build.CPU_ABI.length() % 10)
                + (Build.DEVICE.length() % 10)
                + (Build.MANUFACTURER.length() % 10)
                + (Build.MODEL.length() % 10)
                + (Build.PRODUCT.length() % 10);


        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their phone, there will be a duplicate entry
        String serial = null;
        try
        {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        }
        catch (Exception e)
        {
            // String needs to be initialized
            serial = "serial"; // some value
        }


        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * Get base url for API service
     */
    public static String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Get max radius
     */
    public static double getMaxRadius()
    {
        return maxRadius;
    }

    /**
     * Get radius
     */
    public static double getRadius()
    {
        return currentRadius;
    }


    /**
     * Get radius
     */
    public static void setRadius(double val)
    {
        currentRadius=val;
    }



    /**
     * Check Internet connection
     */
    public static boolean isOnline(Context activityContext)
    {
        boolean connected = false;

        ConnectivityManager cm = (ConnectivityManager) activityContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni!=null && ni.isAvailable() && ni.isConnected())
        {
            connected=true;
        }
        else
        {
            connected=false;
        }
        return connected;
    }
}
