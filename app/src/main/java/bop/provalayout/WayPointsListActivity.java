package bop.provalayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WayPointsListActivity extends AppCompatActivity {

    private GPSDatabase myDatabase;
    private ListView list;
    private String trackId = "", trackName  = "";
    private ArrayList<WayPointType> wp_array = new ArrayList<>();
    private wp_ListAdapter myLstAdaper;
    private boolean isBtnPressed=false; // Utilizzato nell'onStop se chiudo l'app --> onStop --> vado su MainActivity se c'è in corso una REC
    private Global gbl = new Global();
    private String[] arr_trackId = new String[]{};
    private String selected_date="";
    private int screen_width =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }

            setContentView(R.layout.activity_way_points_list);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activy_wplist);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayUseLogoEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                trackId = extras.getString("trackId");
                trackName = extras.getString("trackName");
                arr_trackId = extras.getStringArray("arr_trackId");
                selected_date =  extras.getString("selected_date");
            }

            myDatabase = new GPSDatabase(this);
            myDatabase.open();

            list = (ListView) findViewById(R.id.lst_Points);
            list.setClickable(true);

            TextView tv_trackName_wp = (TextView)findViewById(R.id.tv_trackName_wp);
            tv_trackName_wp.setText(trackName);

            fillListView();

        }
        catch(Exception e){
            Log.w("MY_CHECK", " ERRORE in onCreate [" + e.toString() + "]");
        }

    }

    @Override
    protected void onStop() {

        super.onStop();
        myDatabase.close();

        if (!isBtnPressed && gbl.getRecordingTime()!=0) {
            Intent intent = new Intent(WayPointsListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_wp_list, menu);

            FontManager.manageMenuItem(menu, screen_width,getBaseContext(),this);

            return true;
        }catch(Exception e){
            gbl.myLog(e.toString());
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        myLstAdaper = (wp_ListAdapter)list.getAdapter();
        int num_item_selected = myLstAdaper.getItemsCheckedArray().size();

        switch (item.getItemId()) {
            case R.id.menu_wpl_item_go_to_tracklist:
               // if (gbl.getRecordingTime() != 0)
                    goToTrackList();
                return true;
            case R.id.menu_wpl_item_go_to_map:
                go_to_map();
                return true;
            case R.id.menu_wpl_item_go_to_wp_detail:
                if (num_item_selected == 0)
                    Dlg_Confirm("NO_WP_SELECTED");
                else if (num_item_selected > 1)
                    Dlg_Confirm("TOOMUCH_WP_SELECTED");
                else {
                    goToWayPointDetails();
                }
                return true;
            case R.id.menu_wpl_item_del_track:
                if (num_item_selected > 0) {
                    Dlg_Confirm("DELETE");
                } else
                    Dlg_Confirm("NO_WP_SELECTED");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void remove_wp(String id){
        myDatabase.del_WayPoint(id);
    }

    // Consente di raggiungere la attività con i dettagli del wp selezionato
    private void goToWayPointDetails() {
        try {

            isBtnPressed = true;

            myLstAdaper = (wp_ListAdapter)list.getAdapter();

            Intent intent = new Intent(this, WayPointDetailActivity.class);
            intent.putExtra("wpId",wp_array.get(myLstAdaper.getItemsCheckedArray().get(0)).mId);
            intent.putExtra("trackId",trackId);
            startActivity(intent);
            //finish();

        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goToTrackList [" + e.toString() + "]");
        }
    }

    // Consente di raggiungere la lista delle tracce
    private void goToTrackList() {
        try {
            Intent intent = new Intent(this, TracksListActivity.class);
            intent.putExtra("arr_trackId",arr_trackId);
            intent.putExtra("selected_date",selected_date);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goToTrackList [" + e.toString() + "]");
        }
    }

    // Consente di spostarsi nella vista con la mappa
    public void go_to_map() {
        try {
            isBtnPressed = true;
            myLstAdaper = (wp_ListAdapter)list.getAdapter();
            WayPointType myWp = null;

            if(myLstAdaper.getItemsCheckedArray().size()>0)
                myWp = wp_array.get(myLstAdaper.getItemsCheckedArray().get(0));

            Intent intent = new Intent(this, MainActivity.class);
            if(myWp != null ) {
                intent.putExtra("lat", myWp.mLat);
                intent.putExtra("lon", myWp.mLon);
            }
            startActivity(intent);
        }
        catch(Exception e){

            Log.w("MY_CHECK","go_to_map ERRORE["+ e.toString() +"]" );
        }
    }

    // Popola la lista delle tracce salvate
    public void fillListView() {
        try {

            int WP_ID=1, WP_NAME = 3, WP_LAT=4,WP_LON=5,WP_ALT=6,WP_CMT=7,WP_DESC=8,WP_SYM=9;

            Cursor cur = myDatabase.get_waypoints(trackId);
            cur.moveToFirst();

            ArrayList<WayPointType> listContents  = new ArrayList();

            wp_array =  new ArrayList<>();
            for(int i=0;i<cur.getCount();i++){
                WayPointType wp = new WayPointType();
                wp.mCmt = cur.getString(WP_CMT);
                wp.mDesc = cur.getString(WP_DESC);
                wp.mEle = cur.getString(WP_ALT);
                wp.mLat = cur.getString(WP_LAT);
                wp.mLon = cur.getString(WP_LON);
                wp.mSym = cur.getString(WP_SYM);
                wp.mId = cur.getString(WP_ID);
                wp.mName = cur.getString(WP_NAME) ;
                wp_array.add(wp);

                listContents.add(wp);

                cur.moveToNext();
            }
            cur.close();

            ListAdapter adapter = new wp_ListAdapter(listContents, this );
            list.setAdapter(adapter);

        }
        catch (Exception e){

            Log.w("MY_CHECK","ERRORE in fillListView["+ e.toString() +"]");
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void Dlg_Confirm(String operation) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(WayPointsListActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();

            switch(operation) {
                case "NO_WP_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Select at least a waypoint to list.");
                    //dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override  public void onClick(DialogInterface dialog, int id){  }});
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
                    break;

                case "TOOMUCH_WP_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Select only one waypoint to list.");
                    //dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override  public void onClick(DialogInterface dialog, int id){  }});
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
                    break;

                case "DELETE":
                    dlg.setTitle("Delete");
                    dlg.setMessage("Do you want to delete selected waypoint(s)?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                myLstAdaper = (wp_ListAdapter) list.getAdapter();

                                if (myLstAdaper.getItemsCheckedArray().size() > 0) {
                                    for (int j : myLstAdaper.getItemsCheckedArray()) {

                                        remove_wp(wp_array.get(j).mId);
                                    }

                                    fillListView();
                                    list.invalidate();
                                }
                            }
                            catch(Exception e){Log.w("MY_CHECK", " DialogInterface.DELETE ["+e.toString()+"] ");}
                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

            }


            dlg.show();

            if(operation == "STOP")
                dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in Dlg_Confirm["+ e.toString() +"]");
        }

    }

}
