/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tracker.motion.androidmotiontracker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class RadarView extends View implements SensorListener, LocationListener {

    private static final long RETAIN_GPS_MILLIS = 10000L;

    private Paint mGridPaint;

    private Paint mErasePaint;

    private float mOrientation;


    private ArrayList<String> mTargetLat=new ArrayList<String>();
    private ArrayList<String> mTargetLon=new ArrayList<String>();
   // private ArrayList<String[]> mDistanceScaleList=new ArrayList<String[]>();
   String[] mDistanceScale = new String[4];
    /**
     * Distance to target, in KM
     */
    private ArrayList<Double> mDistance=new ArrayList<Double>();

    /**
     * Bearing to target, in degrees
     */
    private ArrayList<Double> mBearing=new ArrayList<Double>();

    /**
     * Ratio of the distance to the target to the radius of the outermost ring on the radar screen
     */
    private ArrayList<Double> mDistanceRatio=new ArrayList<Double>();

    private double mMyLocationLat;
    private double mMyLocationLon;
    private Location myLastLocation;
    boolean useLocation = false;
    /**
     * These are the list of choices for the radius of the outer circle on the
     * screen when using metric units. All items are in kilometers. This array is
     * used to choose the scale of the radar display.
     */
    private double radarScaleValue;


    /**
     * Once the scale is chosen, this array is used to convert the number of
     * kilometers on the screen to an integer. (Note that for short distances we
     * use meters, so we multiply the distance by . (This
     * array is for metric measurements.)
     */
    private float mMetricDisplayUnitsPerKm = 1.0f;


    /**
     * This array holds the formatting string used to display the distance to
     * the target. (This array is for metric measurements.)
     */
    private String mMetricDisplayFormats=new String("%.0fkm");


    /**
     * This array holds the formatting string used to display the distance on
     * each ring of the radar screen. (This array is for metric measurements.)
     */
    private String mMetricScaleFormats = new String("%.0fkm");



    /**
     * True when we have know our own location
     */
    private boolean mHaveLocation = false;


    /**
     * Utility rect for calculating the ring labels
     */
    private Rect mTextBounds = new Rect();

    /**
     * The bitmap used to draw the target
     */
    private Bitmap mBlip;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint0;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint1;

    /**
     * Used to draw the animated ring that sweeps out from the center
     */
    private Paint mSweepPaint2;

    /**
     * Time in millis when the most recent sweep began
     */
    private long mSweepTime;

    /**
     * True if the sweep has not yet intersected the blip
     */
    private boolean mSweepBefore;

    /**
     * Time in millis when the sweep last crossed the blip
     */
    private long mBlipTime;


    /**
     * Time in millis for the last time GPS reported a location
     */
    private long mLastGpsFixTime = 0L;

    /**
     * The last location reported by the network provider. Use this if we can't get a location from
     * GPS
     */
    private Location mNetworkLocation;

    /**
     * True if GPS is reporting a location
     */
    private boolean mGpsAvailable;

    /**
     * True if the network provider is reporting a location
     */
    private boolean mNetworkAvailable;


    UpdaterTaskClass updaterTask=null;
    protected Activity activityContext;
    protected Context context;
    private Cursor cursor;




    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Paint used for the rings and ring text
        mGridPaint = new Paint();
        mGridPaint.setColor(0xFF00FF00);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Style.STROKE);
        mGridPaint.setStrokeWidth(1.0f);
        mGridPaint.setTextSize(10.0f);
        mGridPaint.setTextAlign(Align.CENTER);

        // Paint used to erase the rectangle behing the ring text
        mErasePaint = new Paint();
        mErasePaint.setColor(0xFF191919);
        mErasePaint.setStyle(Style.FILL);

        // Outer ring of the sweep
        mSweepPaint0 = new Paint();
        mSweepPaint0.setColor(0xFF33FF33);
        mSweepPaint0.setAntiAlias(true);
        mSweepPaint0.setStyle(Style.STROKE);
        mSweepPaint0.setStrokeWidth(2f);

        // Middle ring of the sweep
        mSweepPaint1 = new Paint();
        mSweepPaint1.setColor(0x7733FF33);
        mSweepPaint1.setAntiAlias(true);
        mSweepPaint1.setStyle(Style.STROKE);
        mSweepPaint1.setStrokeWidth(2f);

        // Inner ring of the sweep
        mSweepPaint2 = new Paint();
        mSweepPaint2.setColor(0x3333FF33);
        mSweepPaint2.setAntiAlias(true);
        mSweepPaint2.setStyle(Style.STROKE);
        mSweepPaint2.setStrokeWidth(2f);

        mBlip = ((BitmapDrawable)getResources().getDrawable(R.drawable.blip)).getBitmap();
    }

    /**
     * Sets the target to track on the radar
     *
     */
    public void setTarget()
    {
        cursor=GlobalHelpClass.retrievePoints();
        mTargetLat.clear();
        mTargetLon.clear();
        if(cursor!=null)
        {
            cursor.moveToFirst();

            while(!cursor.isAfterLast())
            {
                mTargetLat.add(cursor.getString(cursor.getColumnIndex("lat")));
                mTargetLon.add(cursor.getString(cursor.getColumnIndex("lng")));
                cursor.moveToNext();
            }
            cursor.close();
        }
        updateRadar();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center = getWidth() / 2;
        int radius = center - 8;

        // Draw the rings
        final Paint gridPaint = mGridPaint;
        canvas.drawCircle(center, center, radius, gridPaint);
        canvas.drawCircle(center, center, radius * 3 / 4, gridPaint);
        canvas.drawCircle(center, center, radius >> 1, gridPaint);
        canvas.drawCircle(center, center, radius >> 2, gridPaint);



        final long now = SystemClock.uptimeMillis();
        if (mSweepTime > 0 && mHaveLocation && mDistanceRatio.size()>0)
        {
            int blipRad = (int) (mDistanceRatio.get(0) * radius);
            // Draw the sweep. Radius is determined by how long ago it started
            long sweepDifference = now - mSweepTime;
            if (sweepDifference < 512L) {
                int sweepRadius = (int) (((radius + 6) * sweepDifference) >> 9);
                canvas.drawCircle(center, center, sweepRadius, mSweepPaint0);
                canvas.drawCircle(center, center, sweepRadius - 2, mSweepPaint1);
                canvas.drawCircle(center, center, sweepRadius - 4, mSweepPaint2);

                // Note when the sweep has passed the blip
                boolean before = sweepRadius < blipRad;
                if (!before && mSweepBefore) {
                    mSweepBefore = false;
                    mBlipTime = now;
                }
            } else {
                mSweepTime = now + 1000;
                mSweepBefore = true;
            }

        }

        // Draw horizontal and vertical lines
        canvas.drawLine(center, center - (radius >> 2) + 6, center, center - radius - 6, gridPaint);
        canvas.drawLine(center, center + (radius >> 2) - 6 , center, center + radius + 6, gridPaint);
        canvas.drawLine(center - (radius >> 2) + 6, center, center - radius - 6, center, gridPaint);
        canvas.drawLine(center + (radius >> 2) - 6, center, center + radius + 6, center, gridPaint);

        // Draw X in the center of the screen
        canvas.drawLine(center - 4, center - 4, center + 4, center + 4, gridPaint);
        canvas.drawLine(center - 4, center + 4, center + 4, center - 4, gridPaint);



        if (mHaveLocation) {

            // ArrayList
            for(int i=0;i<mBearing.size();i++)
            {

                int blipRadius = (int) (mDistanceRatio.get(i) * radius);


                double bearingToTarget = mBearing.get(i) - mOrientation;
                double drawingAngle = Math.toRadians(bearingToTarget) - (Math.PI / 2);

                float cos = (float) Math.cos(drawingAngle);
                float sin = (float) Math.sin(drawingAngle);

                // Draw the text for the rings
                final String[] distanceScale = mDistanceScale;



                addText(canvas, distanceScale[0], center, center + (radius >> 2));
                addText(canvas, distanceScale[1], center, center + (radius >> 1));
                addText(canvas, distanceScale[2], center, center + radius * 3 / 4);
                addText(canvas, distanceScale[3], center, center + radius);




                // Draw the blip. Alpha is based on how long ago the sweep crossed the blip
                long blipDifference = now - mBlipTime;
                gridPaint.setAlpha(255 - (int)((128 * blipDifference) >> 10));
                canvas.drawBitmap(mBlip, center + (cos * blipRadius) - 8 ,
                        center + (sin * blipRadius) - 8, gridPaint);
                gridPaint.setAlpha(255);

            }

            postInvalidate();

        }
    }

    private void addText(Canvas canvas, String str, int x, int y) {

        mGridPaint.getTextBounds(str, 0, str.length(), mTextBounds);
        mTextBounds.offset(x - (mTextBounds.width() >> 1), y);
        mTextBounds.inset(-2, -2);
        canvas.drawRect(mTextBounds, mErasePaint);
        canvas.drawText(str, x, y, mGridPaint);
    }

    public void onAccuracyChanged(int sensor, int accuracy) {
    }

    /**
     * Called when we get a new value from the compass
     *
     * @see android.hardware.SensorListener#onSensorChanged(int, float[])
     */
    public void onSensorChanged(int sensor, float[] values) {
        mOrientation = values[0];
        postInvalidate();
    }

    /**
     * Called when a location provider has a new location to report
     *
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    public void onLocationChanged(Location location) {

        if (!mHaveLocation) {
            mHaveLocation = true;
        }

        if(updaterTask==null)
        {
            //Call the task which receives the devices repeatedly
            updaterTask = new UpdaterTaskClass(new Runnable()
            {
                @Override
                public void run() {
                    repeatReceiveTask();
                }
            });
            updaterTask.startUpdates();
        }


        /***   We send  our new location at the server by calling the suitable service     ****/
        SendDeviceLocationTask sendTask = new SendDeviceLocationTask(activityContext,location);
        //sendTask.delegate=(RadarActivity)activityContext;
        sendTask.execute(new String[] { "" });


        final long now = SystemClock.uptimeMillis();

        final String provider = location.getProvider();
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            // Use GPS if available
            mLastGpsFixTime = SystemClock.uptimeMillis();
            useLocation = true;
        } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            // Use network provider if GPS is getting stale
            useLocation = now - mLastGpsFixTime > RETAIN_GPS_MILLIS;
            if (mNetworkLocation == null) {
                mNetworkLocation = new Location(location);
            } else {
                mNetworkLocation.set(location);
            }

            mLastGpsFixTime = 0L;
        }

        myLastLocation=location;

        updateRadar();
    }

    public void resetRadar()
    {
        mBearing.clear();
        mDistanceRatio.clear();
    }
    public void updateRadar()
    {
        resetRadar();
        final float displayUnitsPerKm;

        displayUnitsPerKm = mMetricDisplayUnitsPerKm;
        if (useLocation)
        {
            mMyLocationLat = myLastLocation.getLatitude();
            mMyLocationLon = myLastLocation.getLongitude();


            for(int i=0;i<mTargetLat.size();i++)
            {

                int latE6 = (int)( Double.parseDouble( mTargetLat.get(i)) * GeoUtils.MILLION);
                int lonE6 =(int)( Double.parseDouble(mTargetLon.get(i)) * GeoUtils.MILLION);

                double targetLat = latE6 / (double) GeoUtils.MILLION;
                double targetLon = lonE6 / (double) GeoUtils.MILLION;

                mDistance.add(GeoUtils.distanceKm(mMyLocationLat, mMyLocationLon, targetLat,
                        targetLon));

                radarScaleValue=GlobalHelpClass.getRadius();

                if (mDistance.get(i) < radarScaleValue)
                {

                    mBearing.add(GeoUtils.bearing(mMyLocationLat, mMyLocationLon, targetLat,
                            targetLon));


                    float scaleDistance = (float) (radarScaleValue * displayUnitsPerKm);

                    mDistanceScale[0] = String.format(mMetricScaleFormats, (scaleDistance / 4));
                    mDistanceScale[1] = String.format(mMetricScaleFormats, (scaleDistance / 2));
                    mDistanceScale[2] = String.format(mMetricScaleFormats, (scaleDistance * 3 / 4));
                    mDistanceScale[3] = String.format(mMetricScaleFormats, scaleDistance);

                    mDistanceRatio.add((double) (mDistance.get(i) / radarScaleValue));

                }

            }
            invalidate();
        }

    }


    /**
     * Function called every x minutes
     *
     */
    public void repeatReceiveTask()
    {
        ReceiveDevicesLocationsTask receiveTask = new ReceiveDevicesLocationsTask(activityContext,mNetworkLocation);
        receiveTask.delegate =(RadarActivity)activityContext;
        receiveTask.execute(new String[] { "" });
    }




    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    /**
     * Called when a location provider has changed its availability.
     *
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {

        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            switch (status) {
            case LocationProvider.AVAILABLE:
                mGpsAvailable = true;
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mGpsAvailable = false;

                if (mNetworkLocation != null && mNetworkAvailable) {
                    // Fallback to network location
                    mLastGpsFixTime = 0L;
                    onLocationChanged(mNetworkLocation);
                } else {
                    handleUnknownLocation();
                }

                break;
            }

        } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            switch (status) {
            case LocationProvider.AVAILABLE:
                mNetworkAvailable = true;
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mNetworkAvailable = false;

                if (!mGpsAvailable) {
                    handleUnknownLocation();
                }
                break;
            }
        }
    }

    /**
     * Called when we no longer have a valid location.
     */
    private void handleUnknownLocation() {
        mHaveLocation = false;
    }

    /**
     * Update state to reflect whether we are using metric or standard units.
     *
     */
    public void setRadarActivityContext(Activity act,Context ctx )
    {
        activityContext=act;
        context=ctx;

    }



    /**
     * Turn on the sweep animation starting with the next draw
     */
    public void startSweep() {
        mSweepTime = SystemClock.uptimeMillis();
        mSweepBefore = true;
    }

    /**
     * Turn off the sweep animation
     */
    public void stopSweep() {
        mSweepTime = 0L;
    }
}