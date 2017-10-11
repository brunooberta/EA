package bop.provalayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class SplashActivity extends FragmentActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private boolean pref_facebook_key_default_connection = false;
    private Global gbl = new Global();
    CallbackManager callbackManager;

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
}
