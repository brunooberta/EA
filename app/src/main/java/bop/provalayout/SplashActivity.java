package bop.provalayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends FragmentActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private boolean pref_facebook_key_default_connection = false;
    private Global gbl = new Global();
    CallbackManager callbackManager;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2;

    @Override
    protected void onStart() {
        super.onStart();
        pref_facebook_key_default_connection = getPreferenceValue_bool(R.string.pref_facebook_key_default_connection);
        if (pref_facebook_key_default_connection) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(checkAndRequestPermissions()) {
            LoginManager.getInstance().logOut();

            Intent serviceIntent = new Intent(this,LocationService.class);
            startService(serviceIntent);

            try {

                gbl.setPreferences(this);

                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(getApplicationContext(), getString(R.string.fb_conn_success), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), getString(R.string.fb_conn_cancel), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.fb_conn_fail), Toast.LENGTH_SHORT).show();
                    }
                });

            }
            catch (Exception e) {
                Log.w("MY_CHECK", "ERRORE in SplashActivity.onCreate [" + e.toString() + "]");
            }
                /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    try {
                /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);

                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
                        SplashActivity.this.startActivity(mainIntent);
                        overridePendingTransition(R.anim.fade_id, R.anim.fade_out);
                    }
                    catch (Exception e) {
                        Log.w("MY_CHECK", "ERRORE in postDelayed [" + e.toString() + "]");
                    }
                }
            }, SPLASH_DISPLAY_LENGTH);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in SplashActivity.onActivityResult [" + e.toString() + "]");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Attenzione: Permesso di Accesso alla posizione GPS NEGATO!", Toast.LENGTH_LONG);
                }
                return;
            }

            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);


                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        if( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ){

                            showDialogOK("Service Permissions are required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                        finishAndRemoveTask ();
                                                    }
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
                            //                            //proceed with logic by disabling the related features or quit the app.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                finishAndRemoveTask ();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean getPreferenceValue_bool(int key){
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            return pref.getBoolean(getResources().getString(key), false);
        }
        catch (Exception e){
            Log.w("MY_CHECK", "ERRORE in getPreferenceValue key["+getResources().getString(key)+"] [" + e.toString() + "]");
            return false;
        }
    }

    private  boolean checkAndRequestPermissions() {
        int camerapermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            int writepermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionFineLocation = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

            List<String> listPermissionsNeeded = new ArrayList<>();

            if (writepermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void explain(String msg){
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:bop.provalayout")));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }
}
