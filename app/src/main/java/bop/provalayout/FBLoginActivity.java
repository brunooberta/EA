package bop.provalayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FBLoginActivity extends FragmentActivity {
    CallbackManager callbackManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_fblogin);

            AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    if (currentAccessToken == null){
                        Intent intent = new Intent(FBLoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            };

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

            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in FBLoginActivity.onCreate [" + e.toString() + "]");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in FBLoginActivity.onActivityResult [" + e.toString() + "]");
        }
    }

}
