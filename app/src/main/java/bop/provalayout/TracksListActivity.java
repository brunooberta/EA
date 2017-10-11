package bop.provalayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TracksListActivity extends AppCompatActivity {

    private MapView map_osm_tl;
    private ListView list;
    private String trackId_to_modify = "";
    private String trackName_to_modify = "";
    private ArrayList<Track_OSM> track_ArrayList;
    private List<org.osmdroid.views.overlay.Polyline> lst_polyline;
    private track_ListAdapter myLstAdaper;
    private GPSDatabase myDatabase;
    private IMapController mapController;
    private Global gbl = new Global();
    private String[] arr_trackId = new String[]{};
    private String selected_date="";
    private int screen_width =0;
    boolean showAllTracks=false;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onStop() {
        super.onStop();
        myDatabase.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }

            setContentView(R.layout.activity_tracks_list);

            myDatabase = new GPSDatabase(this);
            myDatabase.open();

            map_osm_tl = (MapView) findViewById(R.id.mapView_TrackList);
            map_osm_tl.setTileProvider(gbl.getTileProviderArray());
            map_osm_tl.setTilesScaledToDpi(true);
            map_osm_tl.invalidate();

            GeoPoint startPoint = new GeoPoint(45.208456, 7.137358);
            mapController = map_osm_tl.getController();
            mapController.setZoom(10);
            mapController.setCenter(startPoint);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activy_tracklist);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayUseLogoEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            list = (ListView) findViewById(R.id.lst_Tracks);
            list.setClickable(true);

            TextView tv_selection_mode = (TextView) findViewById(R.id.tv_selection_mode);

            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                arr_trackId = extras.getStringArray("arr_trackId");
                selected_date = extras.getString("selected_date");
            }

            if(arr_trackId==null)
                showAllTracks = true;
            else{
                if(arr_trackId.length == 0) showAllTracks = true;
            }

            if (!showAllTracks)
                tv_selection_mode.setText(getString(R.string.tla_selezione_per_data) + " " + selected_date);
            else
                tv_selection_mode.setText(getString(R.string.tla_selezione_completa));

            fillListView();

            drawAllTracks_OSM(arr_trackId);

            myLstAdaper = (track_ListAdapter) list.getAdapter();

        }
        catch(Exception e){
            gbl.myLog("ERRORE in onCreate [" + e.toString() + "]");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_tracks_list, menu);

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

            myLstAdaper = (track_ListAdapter) list.getAdapter();
            int cnt = 0;
            cnt = myLstAdaper.getCountSelected();

            // Handle item selection
            switch (item.getItemId()) {
                case R.id.menu_item_del_track:

                    if (cnt == 0) {
                        Dlg_Confirm("NO_ITEM_SELECTED");
                        return true;
                    }
                    if (cnt > 1) {
                        Dlg_Confirm("SELECT_ONLY_ONE_ITEM");
                        return true;
                    }

                    trackId_to_modify = myLstAdaper.getLastTrackIdSelected();

                    trackName_to_modify = track_ArrayList.get(myLstAdaper.getPositionsSelected().get(0)).getTrackName();

                    Dlg_Confirm("DELETE_TRACKS");

                    return true;

                case R.id.menu_item_upd_track_name:

                    if (cnt == 0){
                        Dlg_Confirm("NO_ITEM_SELECTED");
                        return true;
                    }
                    if (cnt > 1){
                        Dlg_Confirm("SELECT_ONLY_ONE_ITEM");
                        return true;
                    }

                    trackId_to_modify = myLstAdaper.getLastTrackIdSelected();

                    trackName_to_modify = track_ArrayList.get(myLstAdaper.getPositionsSelected().get(0)).getTrackName();

                    Dlg_Confirm("MODIFY_TRACK_NAME");

                    return true;
                case R.id.menu_item_go_to_chart:

                    if (cnt == 0){
                        Dlg_Confirm("NO_ITEM_SELECTED");
                        return true;
                    }
                    if (cnt > 2){
                        Dlg_Confirm("TOO_TRACK_SELECTED");
                        return true;
                    }

                    ArrayList<String> trackIdArrayList = myLstAdaper.getSelectedTrackIdArray();
                    String[] arr_itemsChecked = new String[trackIdArrayList.size()];

                    for(int i=0;i<trackIdArrayList.size();i++){
                        arr_itemsChecked[i] = trackIdArrayList.get(i);
                    }

                    try {
                        getScreenShot();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    intent.putExtra("arr_itemsChecked", arr_itemsChecked);
                    intent.putExtra("arr_trackId",arr_trackId);
                    intent.putExtra("selected_date",selected_date);
                    startActivity(intent);

                    return true;
                case R.id.menu_item_go_to_map:
                    startMainActivity();
                    return true;

                case R.id.menu_tl_item_import_track:
                    importTrack();
                    return true;
                case R.id.menu_tl_item_go_to_wp_list:
                    goWPList(cnt);
                    return true;

                case R.id.menu_tl_export_track:
                    if (cnt == 0){
                        Dlg_Confirm("NO_ITEM_SELECTED");
                        return true;
                    }
                    if (cnt > 2){
                        Dlg_Confirm("TOO_TRACK_SELECTED");
                        return true;
                    }
                    Dlg_Confirm("EXPORT_TRACK");
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

    private void goWPList(int num_item_selected) {
        try {

            if (num_item_selected == 0) {
                Dlg_Confirm("NO_ITEM_SELECTED");
                return;
            }
            if (num_item_selected > 2){
                Dlg_Confirm("TOO_TRACK_SELECTED");
                return;
            }
            Intent intent = new Intent(this, WayPointsListActivity.class);

            if ( gbl.getSelectTrack_osm() != null ) {

                Cursor cur = myDatabase.get_waypoints(gbl.getSelectTrack_osm().getTrackId());
                cur.moveToFirst();
                if (cur.getCount() == 0) {
                    Dlg_Confirm("NO_ITEM_TO_SHOW");
                } else {
                    intent.putExtra("trackId", gbl.getSelectTrack_osm().getTrackId());
                    intent.putExtra("trackName", gbl.getSelectTrack_osm().getTrackName());
                    intent.putExtra("arr_trackId", arr_trackId);
                    intent.putExtra("selected_date", selected_date);

                    startActivity(intent);
                    //finish();
                }
                cur.close();
            }



        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goWPList [" + e.toString() + "]");
        }
    }

    private void drawAllTracks_OSM(String[] arr_trackId){
        try {
            if (map_osm_tl != null) {
                int overlaysSize = map_osm_tl.getOverlays().size();
                for(int i=0; i<overlaysSize;i++ ) {
                    map_osm_tl.getOverlays().remove(0);
                }
                map_osm_tl.invalidate();

                lst_polyline = new ArrayList<>();

                for (Track_OSM t : track_ArrayList) {
                    drawTrackOnMap_OSM(t);
                }

            }

        }catch(Exception e){
            gbl.myLog( "Errore in drawAllTracks_OSM ["+e.toString()+"]");
        }
    }

    //Consente di disegnare la traccia sulla mappa OSM
    private void drawTrackOnMap_OSM(Track_OSM track_osm) {

       try {
            org.osmdroid.views.overlay.Polyline polyLine = track_osm.polyline;
            lst_polyline.add(polyLine);
            map_osm_tl.getOverlays().add(polyLine);
            map_osm_tl.getOverlays().add(track_osm.startMarker);
            map_osm_tl.getOverlays().add(track_osm.endMarker);

        } catch (Exception e) {
            gbl.myLog( "ERRORE in drawTrackOnMap_OSM [" + e.toString() + "]");

        }
    }

    private void importTrack(){
        try {
            ArrayList<String> ext = new ArrayList<>();
            ext.add("gpx");

            Intent intent = new Intent(getApplicationContext(), FileChooser.class);
            intent.putStringArrayListExtra("extension", ext);
            intent.putExtra("isGpx", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);

        }
        catch(Exception e){
            gbl.myLog("importTrack ERRORE["+ e.toString() +"]" );
        }
    }

    // Consente di spostarsi nella vista con la mappa
    public void startMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            gbl.myLog("startMainActivity ERRORE["+ e.toString() +"]" );
        }
    }

    // Popola la lista delle tracce salvate
    public void fillListView() {
        try {

            track_ArrayList = new ArrayList<>();

            if(showAllTracks) {

                track_ArrayList = gbl.getTrackOsmCollection().get_All_track_osm();
            }
            else{

                for (int i = 0; i < arr_trackId.length; i++) {
                    track_ArrayList.add(gbl.getTrackOsmCollection().getTrackFromCollectionByTrackId(arr_trackId[i]));
                }
            }

            ListAdapter adapter = new track_ListAdapter(track_ArrayList, this, map_osm_tl);
            list.setAdapter(adapter);
         }
        catch (Exception e){

            gbl.myLog("ERRORE in fillListView["+ e.toString() +"]");
        }
    }

    private void Dlg_Confirm(String operation) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(TracksListActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();

            switch (operation) {
                case "EXPORT_TRACK":
                    dlg.setTitle("Confimation");
                    dlg.setMessage("Do you want to export selected tracks?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "EXPORT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //myLstAdaper = (track_ListAdapter) list.getAdapter();

                            ArrayList<String> trackIdSelected = myLstAdaper.getSelectedTrackIdArray();

                            if( trackIdSelected.size() == 1 ) {
                                ExportToGpxFormat exportToFile = new ExportToGpxFormat(myDatabase, trackIdSelected.get(0));
                            }

                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
                    break;

                case "NO_ITEM_TO_SHOW":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Trere are no items to show.");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

                case "TOO_TRACK_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("The maximum number of tracks selected was selected.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

               case "DELETE_TRACKS":
                    dlg.setTitle("Delete");
                    dlg.setMessage("Do you want to delete selected track(s)?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                //myLstAdaper = (track_ListAdapter) list.getAdapter();

                                ArrayList<String> trackIdSelected = myLstAdaper.getSelectedTrackIdArray();

                                String[] whereValues = new String[trackIdSelected.size()];

                                for (int k = 0; k < trackIdSelected.size(); k++) {
                                    whereValues[k] = trackIdSelected.get(k);

                                    if(gbl.getSelectTrack_osm().getTrackId().equals(trackIdSelected.get(k)) )
                                        gbl.setSelectTrack_osm(null);

                                    if (arr_trackId != null) {
                                        if (arr_trackId.length > 0) {
                                            // Elimino da arr_trackId gli elementi che sto cancellando
                                            List<String> lst_trackId = new ArrayList<String>();
                                            for (int z = 0; z < arr_trackId.length; z++) {
                                                if (!arr_trackId[z].equals(trackIdSelected.get(k)))
                                                    lst_trackId.add(arr_trackId[z]);
                                            }
                                            arr_trackId = lst_trackId.toArray(new String[lst_trackId.size()]);
                                        }
                                    }
                                }

                                myDatabase.del_TrackSaved(whereValues);

                                fillListView();

                                drawAllTracks_OSM(arr_trackId);
                            }
                            catch(Exception e){gbl.myLog( " DialogInterface.DELETE ["+e.toString()+"] ");}
                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    break;

                case "NO_ITEM_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("No Trace selected!");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    break;
                case "SELECT_ONLY_ONE_ITEM":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Select only one track");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    break;
                case "MODIFY_TRACK_NAME":
                    // Costruisco Edittext x il nome della traccia
                    final My_EditText et_trackName = new My_EditText(TracksListActivity.this,trackName_to_modify, "ALPHANUMERIC", 20, dlg);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                    et_trackName.setLayoutParams(lp);

                    dlg.setTitle("Modify Track Name");
                    dlg.setView(et_trackName); // Aggiungo editetext al Dialogo
                    dlg.setMessage("Modify Track Name and press Save Button:");
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface dialog, int id){ } }                    );
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                            "SAVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try{
                                        if ( et_trackName.getText().length() > 0 ) {
                                            upd_trackName(et_trackName.getText().toString(),trackId_to_modify);

                                            gbl.getTrackOsmCollection().getTrackFromCollectionByTrackId(trackId_to_modify).setTrackName(et_trackName.getText().toString());

                                            fillListView();
                                        }
                                    }
                                    catch (Exception e){
                                        gbl.myLog("ERRORE in DialogInterface.OnClickListener --> OnClick["+ e.toString() +"]");
                                    }
                                }
                            });

                    break;
            }

            dlg.show();


        } catch (Exception e) {
            gbl.myLog( "ERRORE in Dlg_Confirm[" + e.toString() + "]");
        }
    }

    private void upd_trackName(String trackName, String trackId){
        myDatabase.upd_TrackSaved_Rows(new String[]{"description"}, new String[]{trackName}, "trackId=?",new String[]{trackId});
    }

    public void getScreenShot() throws IOException {
        try {
            map_osm_tl.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(map_osm_tl.getDrawingCache());
            map_osm_tl.setDrawingCacheEnabled(false);

            //String mPath = Environment.getExternalStorageDirectory().getPath() + "/Download/screenshot.jpg";
            String mPath = gbl.getAppFolderPath() + File.separator + "screenshot.jpg";

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in getScreenShot [" + e.toString() + "]");
        }
    }

}
