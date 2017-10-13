package bop.provalayout;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static Location CURRENT_BETTER_LOCATION=null;
    private Global gbl  = new Global();
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public LocationService() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //gbl.myLog("LocationService --> onStartCommand");

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                        .addConnectionCallbacks(LocationService.this)
                        .addOnConnectionFailedListener(LocationService.this)
                        .addApi(LocationServices.API)
                        .build();

                mGoogleApiClient.connect();
            }

            int pollingFrequency = 1000;
            float smallestDisplacement = 0;

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(pollingFrequency);
            mLocationRequest.setFastestInterval(pollingFrequency);
            mLocationRequest.setSmallestDisplacement(smallestDisplacement);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (mGoogleApiClient.isConnected()) {

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return Service.START_NOT_STICKY;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

            return Service.START_NOT_STICKY;
        } catch (Exception e) {
            gbl.myLog( "ERRORE in onStartCommand [" + e.toString() + "]");
            return Service.START_NOT_STICKY;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if( isBetterLocation(location, CURRENT_BETTER_LOCATION) ) {
                CURRENT_BETTER_LOCATION = location;
                Intent new_intent = new Intent("intLocationChanged");
                new_intent.putExtra("LOCATION", location);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new_intent);

                if(location.getAltitude() == 0.0)
                    location.setAltitude(gbl.H_PRECEDENTE);

                //gbl.myLog("LocationService --> onLocationChanged H[" + location.getAltitude() + "] LAT["+location.getLatitude()+"] LON["+ location.getLongitude() +"]" );
            }
        }
        catch (Exception e) {
            gbl.myLog("ERRORE in onLocationChanged ["+e.toString()+"]");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        gbl.myLog("LocationService --> onConnectionFailed");
    }


    @Override
    public void onConnected(Bundle arg0) {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (Exception e) {
            gbl.myLog("ERRORE in onConnected ["+e.toString()+"]");
        }

    }


    @Override
    public void onConnectionSuspended(int arg0) {
         gbl.myLog("LocationService --> onConnectionSuspended");
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int DELTA_T = 1000 * 10;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > DELTA_T;
        boolean isSignificantlyOlder = timeDelta < -DELTA_T;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        //TODO: testare questa condizione:se location.getAccuracy() > 30 m --> il segnale nn va bene
        boolean isAccurate = location.getAccuracy() < 30;
        //gbl.myLog("isAccurate["+isAccurate+"]");
        if(!isAccurate)
            return false;

        // Check whether the new location fix is more or less accurate (migliore se piccola)
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
       // gbl.myLog("accuracyDelta["+accuracyDelta+"] location.getAccuracy()["+location.getAccuracy()+"]");

        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        //gbl.myLog( "accuracyDelta["+accuracyDelta+"] isNewer["+isNewer+"] isLessAccurate["+isLessAccurate+"] isSignificantlyLessAccurate["+isSignificantlyLessAccurate+"] isMoreAccurate["+isMoreAccurate+"]");

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
