package bop.provalayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareActivity extends AppCompatActivity {

    private ImageView snapshotHolder;
    private String mPath_source="", mPath_to_share="", d="",t="",dp="",dm="";
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    private String trackId = "",trackName="";
    private Animation toolBarAnimation;
    private Global gbl = new Global();
    private String[] arr_trackId = new String[]{};
    private String selected_date="";
    private int screen_width =0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            mPath_source = gbl.getAppFolderPath() + File.separator + "screenshot.jpg";
            mPath_to_share = gbl.getAppFolderPath() + File.separator + "sharing_pic.jpg";

            setContentView(R.layout.activity_share);

            toolbar = (Toolbar) findViewById(R.id.tb_share_activity);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.getWidth();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                d = extras.getString("Distance");
                t = extras.getString("Time");
                dp = extras.getString("D_plus");
                dm = extras.getString("D_minus");
                trackId = extras.getString("trackId");
                trackName = extras.getString("trackName");
                arr_trackId = extras.getStringArray("arr_trackId");
                selected_date = extras.getString("selected_date");

            }
            snapshotHolder = (ImageView) findViewById(R.id.imageView_holder);

            TextView tv_L = (TextView) findViewById(R.id.tv_L);
            TextView tv_T = (TextView) findViewById(R.id.tv_T);
            TextView tv_Dp = (TextView) findViewById(R.id.tv_Dp);
            TextView tv_Dm = (TextView) findViewById(R.id.tv_Dm);
            TextView tv_Name = (TextView) findViewById(R.id.tv_trackName);

            d = tv_L.getText().toString() + d;
            t = tv_T.getText().toString() + t;
            dp = tv_Dp.getText().toString() + dp;
            dm = tv_Dm.getText().toString() + dm;

            tv_L.setText(d);
            tv_T.setText(t);
            tv_Dp.setText(dp);
            tv_Dm.setText(dm);
            tv_Name.setText(trackName);

            callbackManager = CallbackManager.Factory.create();
            shareDialog = new ShareDialog(this);
            setImageFromFile();
        }
        catch(Exception e){gbl.myLog("ERRORE ShareActivity --> onCreate["+e.toString()+"]");}
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_share, menu);

            FontManager.manageMenuItem(menu, screen_width,getBaseContext(),this);

            return true;
        }catch(Exception e){
            gbl.myLog(e.toString());
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        try {
            int cnt = 0;

            // Handle item selection
            switch (item.getItemId()) {

                case R.id.menu_sh_item_go_to_chart:
                    goToCharts();
                    return true;

                case R.id.menu_sh_item_go_to_map:
                    startMainActivity();
                    return true;

                case R.id.menu_sh_item_fb:
                    makeScreenshot();
                    share_fb();
                    return true;

                case R.id.menu_sh_item_wa:
                    makeScreenshot();
                    share_wa();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }

        }
        catch(Exception e){
            gbl.myLog("ERRORE in onOptionsItemSelected ["+e.toString()+"]");
            return false;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void makeScreenshot() throws IOException {

        LinearLayout lr = (LinearLayout)findViewById(R.id.rl_share_main);
        lr.setDrawingCacheEnabled(true);
        lr.buildDrawingCache();
        Bitmap bitmap = lr.getDrawingCache();
        File imageFile = new File(mPath_to_share);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    private void setImageFromFile(){

        Bitmap myBitmap = null;

        myBitmap = getBitmapFromFile(mPath_source);

        if(myBitmap != null){

            snapshotHolder.setImageBitmap(myBitmap);
            snapshotHolder.invalidate();
        }

    }

    private Bitmap getBitmapFromFile(String path){
        Bitmap retBmp = null;
        File imgFile = new  File(path);

        if(imgFile.exists()){

            retBmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        else
            Log.w("MY_CHECK", " IL FILE NON ESISTE ");

        return retBmp;
    }

    private void goToCharts(){
        try {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("arr_itemsChecked", new String[]{trackId});
            intent.putExtra("arr_trackId",arr_trackId);
            intent.putExtra("selected_date",selected_date);
            startActivity(intent);
        }
        catch(Exception e){Log.w("MY_CHECK", " goToCharts["+e.toString()+"] ");}
    }

    // Consente di spostarsi nella vista con la mappa
    public void startMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            Log.w("MY_CHECK","startMainActivity ERRORE["+ e.toString() +"]" );
        }
    }

    private void share_fb(){
        try {
            Bitmap screenShot = getBitmapFromFile(mPath_to_share);
            SharePhoto photo = new SharePhoto.Builder().setBitmap(screenShot).build();
            SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException e) {
                }
            });
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        }
        catch(Exception e){
            gbl.myLog("Errore in share_fb["+e.toString()+"]");
        }
    }

    private void share_wa(){

        File pictureFile = new File(mPath_to_share);
        Uri imageUri = Uri.parse(pictureFile.getAbsolutePath());
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage("com.whatsapp");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trackName);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Log.w("MY_CHECK", "share_wa[Whatsapp have not been installed.]");
        }

    }
}
