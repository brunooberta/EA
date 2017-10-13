package bop.ea_free;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class WayPointDetailActivity extends AppCompatActivity {
    private String trackId="", wpId = "";
    WayPointType wp = new WayPointType();
    GPSDatabase myDatabase;

    EA_EditText et_wp_name, et_wp_lat, et_wp_lon, et_wp_alt,et_wp_descr;

    ArrayList<ListItem> spinner_items;
    Spinner sp_icons;
    private boolean isChanged = false;
    private boolean isBtnPressed = false; // true se esco dalla activity premendo il pulsante
    private Global gbl = new Global();
    private int screen_width = 0;
    private String trackName = "";
    private int oldValueIcon = -1;
    private MyTextWatcher tw_name, tw_lat, tw_lon, tw_alt;
    private TextInputLayout til_name,til_lat ,til_lon, til_alt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);

            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }
            setContentView(R.layout.activity_way_point_detail);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_wp_det_activity);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            myDatabase = new GPSDatabase(this);
            myDatabase.open();

            et_wp_name = (EA_EditText)findViewById(R.id.et_wp_name);
            et_wp_lat = (EA_EditText)findViewById(R.id.et_wp_lat);
            et_wp_lon = (EA_EditText)findViewById(R.id.et_wp_lon);
            et_wp_alt = (EA_EditText)findViewById(R.id.et_wp_alt);
            et_wp_descr = (EA_EditText)findViewById(R.id.et_wp_descr);

            sp_icons = (Spinner) findViewById(R.id.sp_icon);
            spinner_items = getAllList();

            sp_icons.setAdapter(new SpinnerAdapter(this, R.layout.spinner_row_layout, spinner_items,true));

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                trackId = extras.getString("trackId");
                trackName = extras.getString("trackName");
                wpId = extras.getString("wpId");
                get_wp_data(wpId);
                set_ItemValues();
            }

            gbl.myLog("trackName["+trackName+"]");

            TextView tv_track_name = (TextView) findViewById(R.id.tv_wp_track_name);
            tv_track_name.setText(trackName);

            sp_icons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    isChanged=true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }
        catch (Exception e){

            gbl.myLog("ERRORE in WayPointDetailActivity.onCreate["+ e.toString() +"]");
        }
    }

    // Serve per gestire la scrollbar all'interno del MultiLine in modo che si riesca a scrollare senza interferire con quella dell'activity
    View.OnTouchListener touchListener = new View.OnTouchListener(){
        public boolean onTouch(final View v, final MotionEvent motionEvent){
            if(v.getId() == R.id.et_wp_descr){
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_wp_details, menu);

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

                case R.id.menu_wd_item_go_to_wp_list:
                    save_and_go_to_wp_list();
                    return true;

                case R.id.menu_wd_item_go_to_map:
                    go_to_map();
                    return true;

                case R.id.menu_wd_item_save:
                    save();
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

    private int getPositionOfIconForSpinnerIcon(int resourceId){
    try {
        ListItem li;
        int retIndex = -1;

        for (int i = 0; i < spinner_items.size(); i++) {

            li = spinner_items.get(i);

            if (li.imgThumb == resourceId)
                retIndex = i;

        }

        return retIndex;
    }
    catch (Exception e){
        gbl.myLog("ERRORE in getPositionOfIconForSpinnerIcon["+ e.toString() +"]");
        return -1;
        }
    }

    // Popolo la lista delle icone relative ai WayPoints
    private ArrayList<ListItem> getAllList() {
        try {
            ArrayList<ListItem> allList = new ArrayList<ListItem>();

            ListItem item = new ListItem();

            TypedArray descriptions = getResources().obtainTypedArray(R.array.wp_spinner_descriptions);
            TypedArray icons = getResources().obtainTypedArray(R.array.wp_spinner_icons);

            for (int i = 0; i < icons.length(); i++) {
                item = new ListItem();
                item.setData(descriptions.getString(i),icons.getResourceId(i,0));
                allList.add(item);
            }

            return allList;
        }
        catch (Exception e) {
            gbl.myLog( "ERRORE in getAllList[" + e.toString() + "]");
            return null;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        myDatabase.close();

        if (!isBtnPressed) {
            Intent intent = new Intent(WayPointDetailActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void get_wp_data(String wpId){

        try {
            int WP_ID = 0, WP_TRACKID = 1, WP_NAME = 2, WP_LAT = 3, WP_LON = 4, WP_ALT = 5, WP_CMT = 6, WP_DESC = 7, WP_SYM = 8;

            Cursor cur = myDatabase.get_waypoint_data(wpId);
            cur.moveToFirst();
            wp.mId = cur.getString(WP_ID);
            wp.mEle = cur.getString(WP_ALT);
            wp.mSym = cur.getString(WP_SYM);
            wp.mLon = cur.getString(WP_LON);
            wp.mLat = cur.getString(WP_LAT);
            wp.mCmt = cur.getString(WP_CMT);
            wp.mName = cur.getString(WP_NAME);
            wp.mDesc = cur.getString(WP_DESC);

            cur.close();
        }catch (Exception e) {
            gbl.myLog( "ERRORE in get_wp_data [" + e.toString() + "]");
        }
    }


    private void set_ItemValues(){
    try {
        et_wp_name.setText(wp.mName);
        til_name = (TextInputLayout) findViewById(R.id.til_wp_name);
        tw_name = new MyTextWatcher(et_wp_name,til_name, getApplicationContext());
        et_wp_name.addTextChangedListener(tw_name);

        et_wp_lat.setText(wp.mLat);
        til_lat = (TextInputLayout) findViewById(R.id.til_lat);
        tw_lat = new MyTextWatcher(et_wp_lat,til_lat, getApplicationContext());
        et_wp_lat.addTextChangedListener(tw_lat);

        et_wp_lon.setText(wp.mLon);
        til_lon = (TextInputLayout) findViewById(R.id.til_lon);
        tw_lon = new MyTextWatcher(et_wp_lon,til_lon, getApplicationContext());
        et_wp_lon.addTextChangedListener(tw_lon);

        et_wp_alt.setText(wp.mEle);

        et_wp_descr.setText(wp.mDesc);

        sp_icons.setSelection(getPositionOfIconForSpinnerIcon(Integer.parseInt(wp.mSym)));
        oldValueIcon = Integer.parseInt(wp.mSym);
    }
    catch (Exception e) {
            gbl.myLog( "ERRORE in set_EditTexts [" + e.toString() + "]");
        }
    }

    private void save(){

        if(til_lat.getError() != null || til_lon.getError() != null  ||til_name.getError() != null  ){
            Dlg_Confirm("INPUT_ERROR");
        }else
            Dlg_Confirm("ONLY_SAVE");
    }

    private void save_and_go_to_wp_list() {
        try {
            isBtnPressed = true;
            if(tw_lat.isChanged() || tw_lon.isChanged() || tw_name.isChanged()) {
                Dlg_Confirm("SAVE_AND_GO");
            }else
                go_to_WP_list();


        } catch (Exception e) {
            gbl.myLog( "ERRORE in goWPList [" + e.toString() + "]");
        }
    }

    // Consente di spostarsi nella vista con la mappa
    public void go_to_map() {
        try {
            isBtnPressed = true;
            Intent intent = new Intent(this, MainActivity.class);

            intent.putExtra("lat", et_wp_lat.getText().toString());
            intent.putExtra("lon", et_wp_lon.getText().toString());
            startActivity(intent);
        }
        catch(Exception e){

            gbl.myLog("go_to_map ERRORE["+ e.toString() +"]" );
        }
    }

    private void go_to_WP_list() {
        try {
            isBtnPressed = true;
            Intent intent = new Intent(WayPointDetailActivity.this, WayPointsListActivity.class);
            intent.putExtra("trackId", trackId);
            intent.putExtra("trackName", trackName);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in go_to_WP_list [" + e.toString() + "]");
        }
    }

    private void Dlg_Confirm(String operation) {
        try {

            AlertDialog.Builder myBuilder = new AlertDialog.Builder(WayPointDetailActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();

            switch(operation) {

                case "INPUT_ERROR":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Correct the data and try again (press OK) or Restore original values (press Discard)");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});

                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            set_ItemValues();
                        }
                    });
                    break;

                case "SAVE_AND_GO":
                    dlg.setTitle("Save WayPoint");
                    dlg.setMessage("Do you want to save data?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (!myDatabase.isOpen())
                                myDatabase.open();

                            myDatabase.upd_waypoint_data(wp.mId, et_wp_name.getText().toString(), et_wp_lat.getText().toString(), et_wp_lon.getText().toString(), et_wp_alt.getText().toString(), et_wp_descr.getText().toString(), getIconResourceId());
                            go_to_WP_list();

                        }
                    });
                    break;

                case "ONLY_SAVE":
                    dlg.setTitle("Save WayPoint");
                    dlg.setMessage("Do you want to save data?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (!myDatabase.isOpen())
                                myDatabase.open();

                            myDatabase.upd_waypoint_data(wp.mId, et_wp_name.getText().toString(), et_wp_lat.getText().toString(), et_wp_lon.getText().toString(), et_wp_alt.getText().toString(), et_wp_descr.getText().toString(), getIconResourceId());

                        }
                    });



                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE,"DISCARD", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {
                        go_to_WP_list();
                    }});
                    break;
            }

            dlg.show();


        }
        catch (Exception e){
            gbl.myLog("ERRORE in Dlg_Confirm["+ e.toString() +"]");
        }

    }

    //restituisce l'id della risorsa relativa all'icona selezionata nello spinner
    private int getIconResourceId(){
        try{
        ListItem li;
        li = (ListItem)sp_icons.getSelectedItem();

        return li.imgThumb;
        } catch (Exception e) {
            gbl.myLog( "ERRORE in go_to_WP_list [" + e.toString() + "]");
            return -1;
        }
    }

}
