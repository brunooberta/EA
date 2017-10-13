package bop.ea_free;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class WPListActivity extends AppCompatActivity {

    GPSDatabase myDatabase;
    private ArrayList<String> listContents;
    private ListView list;
    private String trackId = "", trackName  = "";
    private ArrayList<WayPointType> wp_array = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_wplist);

            this.setTitle("WayPoints List");

            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                trackId = extras.getString("trackId");
                trackName = extras.getString("trackName");
            }

            myDatabase = new GPSDatabase(this);
            myDatabase.open();


            list = (ListView) findViewById(R.id.wpList);
            list.setClickable(true);

            //list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
    }

    public void fillListView() {
        try {

            int TRACK_ID=0, WP_ID=1, TRACK_DESCR=2, WP_NAME = 3, WP_LAT=4,WP_LON=5,WP_ALT=6,WP_CMT=7,WP_DESC=8,WP_SYM=9;

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
}
