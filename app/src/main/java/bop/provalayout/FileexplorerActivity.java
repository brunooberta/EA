package bop.provalayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FileexplorerActivity extends AppCompatActivity {
 
	private static final int REQUEST_PATH = 1;
    private double trackDistance=0, deltaH_pos=0, deltaH_neg=0, h_min=99999, h_max=0, during=0;
    private Global gbl = new Global();
    private String[] arr_trackId = new String[]{};
    private String selected_date = "";

	String curFileName="";
	String title = "Import GPX Files", btn_import_title = "IMPORT GPX FILE";
    ArrayList<String> ext = new ArrayList<>();
    String data="";
    EA_EditText edittext;
    Button btn_import_file;
    boolean isOfflineMapImport = false;
    private Context ctx;
    private String path = Environment.getExternalStorageDirectory().getPath();
    private int screen_width=0;
    private boolean isGpx = true;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_fileexplorer);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_file_explorer);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            ctx = getApplicationContext();

            btn_import_file = (Button) findViewById(R.id.btn_import_gpx);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                arr_trackId = extras.getStringArray("arr_trackId");
                selected_date = extras.getString("selected_date");
                curFileName = extras.getString("GetFileName");
                path = extras.getString("GetPath");
                isGpx  = extras.getBoolean("isGpx");
            }

            if (!isGpx) {
                title = "Select offline map";
                ext.add("zip");
                ext.add("sqlite");
                ext.add("gemf");
                btn_import_title = "SELECT MAP";
                //path += "/Download";

            } else {
                title = "Import GPX Files";
                ext.add("gpx");
                btn_import_title = "IMPORT GPX FILE";
                //path += "/Download";
            }

            TextInputLayout til_fe_editText = (TextInputLayout) findViewById(R.id.til_fe_editText);
            edittext = (EA_EditText) findViewById(R.id.fe_editText);
            MyTextWatcher tw_fe_editText = new MyTextWatcher(edittext,til_fe_editText, getApplicationContext());
            edittext.addTextChangedListener(tw_fe_editText);
            edittext.setText(curFileName);

            btn_import_file.setText(btn_import_title);

            btn_import_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // se non ho selezionato nulla con il BROWSE ma ho scritto qualcosa sull'edit --> lo uso come nome del file
                        if (curFileName.length() == 0)
                            curFileName = edittext.getText().toString();

                        if (edittext.getText().length() > 0 || isOfflineMapImport) {
                            if (!isOfflineMapImport) {
                                File gpxFile = new File(path + "/" + curFileName);

                                if (gpxFile.exists()) {
                                    GPX gpx_data = new GPX();
                                    String retValue = "";
                                    gpx_data = decodeGPX(gpxFile);

                                    if (gpx_data != null) {
                                        retValue = SaveTrackOnDB(edittext.getText().toString(), gpx_data);
                                        if(gpx_data.isDataOk()) {
                                            Dlg_Confirm("IMPORT OK", retValue);
                                        }else{
                                            Dlg_Confirm("IMPORT OK WITH ERROR", retValue);
                                        }
                                    } else
                                        Dlg_Confirm("IMPORT NOK", retValue);
                                } else {
                                    Dlg_Confirm("FILE_NOT_EXIST", "");
                                }
                            } else {
                                File zipFile = new File(path + "/" + curFileName);
                                if ((zipFile.getName().toString().length() > 0 && zipFile.exists()) || zipFile.getName().toString().length() == 0)
                                    Dlg_Confirm("SET_OFFLINE_MODE", "");
                                else if (!zipFile.exists()) {
                                    Dlg_Confirm("FILE_NOT_EXIST", "");
                                }
                            }
                        }
                    } catch (Exception e) {
                        gbl.myLog("ERRORE in onClick [" + e.toString() + "]");
                    }
                }
            });
        }
        catch(Exception e){
            gbl.myLog("ERRORE in FileexplorerActivity-->onCreate [" + e.toString() + "]");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        try {
            if(!isOfflineMapImport) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.menu_file_expl, menu);

                FontManager.manageMenuItem(menu, screen_width, getBaseContext(), this);
            }
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


            // Handle item selection
            switch (item.getItemId()) {
                case R.id.menu_fe_item_go_to_map:
                    goToMap();
                    return true;

                case R.id.menu_fe_item_go_to_tracklist:
                    goToTrackList();
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
    // Consente di spostarsi nella vista con la lista delle tracce salvate
    private void goToTrackList() {
        try {
            Intent intent = new Intent(this, TracksListActivity.class);
            intent.putExtra("arr_trackId",arr_trackId);
            intent.putExtra("selected_date",selected_date);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in goToTrackList [" + e.toString() + "]");
        }
    }

    // Consente di spostarsi nella vista con la mappa
    public void goToMap() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            gbl.myLog("startMainActivity ERRORE["+ e.toString() +"]" );
        }
    }

    private void Dlg_Confirm(String operation, String note) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(FileexplorerActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();
            dlg.setTitle("Import Track");
            switch(operation){
                case "SET_OFFLINE_MODE":
                    dlg.setMessage("Setting Successfully.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(getString(R.string.pref_offlinemap_key), curFileName);
                                editor.commit();

                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            }catch(Exception e){ gbl.myLog("ERRORE in onClick(SET_OFFLINE_MODE)["+e.toString()+"]");}
                        }
                    });
                    break;
                case "FILE_NOT_EXIST":
                    dlg.setMessage("Attention Please...");
                    dlg.setMessage("Teh Selected file doesn't exist.");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

                case "IMPORT OK":

                    if(note.length()==0)
                        dlg.setMessage("Import Successfully.");
                    else
                        dlg.setMessage("Import Failed: ["+note+"]");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                            intent.putExtra("arr_trackId",arr_trackId);
                            intent.putExtra("selected_date",selected_date);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }catch(Exception e){ gbl.myLog("ERRORE in onClick(IMPORT OK)["+e.toString()+"]");}
                        }
                    });
                    break;

                case "IMPORT OK WITH ERROR":

                    if(note.length()==0)
                        dlg.setMessage("Import Successfully but with errors in parsing phase.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                                intent.putExtra("arr_trackId",arr_trackId);
                                intent.putExtra("selected_date",selected_date);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            }catch(Exception e){ gbl.myLog("ERRORE in onClick(IMPORT OK WITH ERROR)["+e.toString()+"]");}
                        }
                    });
                    break;

                case "IMPORT NOK":

                    dlg.setMessage("Import Failed.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;
            }

            dlg.show();


        }
        catch (Exception e){
            gbl.myLog("ERRORE in Dlg_Confirm["+ e.toString() +"]");
        }

    }

    public void getfile(View view){
    	Intent intent1 = new Intent(this, EAFileChooserActivity.class);
        intent1.putStringArrayListExtra("extension", ext);
        intent1.putExtra("arr_trackId",arr_trackId);
        intent1.putExtra("selected_date",selected_date);
        intent1.putExtra("isGpx", isGpx);

        startActivityForResult(intent1,REQUEST_PATH);
    }


 // Listen for results --> Viene
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

    	if (requestCode == REQUEST_PATH){
    		if (resultCode == RESULT_OK) { 
    			curFileName = data.getStringExtra("GetFileName");
                arr_trackId = data.getStringArrayExtra("arr_trackId");
                selected_date = data.getStringExtra("selected_date");
            	edittext.setText(curFileName);
    		}
    	 }
    }

    private String SaveTrackOnDB(String trackName, GPX gpx_data){
        try {
            GPSDatabase myDatabase = new GPSDatabase(getApplicationContext());
            myDatabase.open();

            int trackId=-1;
            if (gpx_data.m_location_list.size() > 0) {
                get_track_data( gpx_data.m_location_list);

                trackId = myDatabase.ins_GPX_data(trackName, gpx_data.m_location_list, trackDistance, h_max, h_min, deltaH_pos, deltaH_neg, during );

                if (gpx_data.m_wayPoint_list.size() > 0 && trackId>0)
                    myDatabase.ins_GPX_WayPoint_lst(""+trackId, gpx_data.m_wayPoint_list);

                gbl.getTrackOsmCollection().addTrackToCollection(""+trackId);
            }

            myDatabase.manage_TrackInfo_Rows("start tracking", "0");
            myDatabase.close();

            return "";
        }
        catch (Exception e){
            gbl.myLog("ERRORE in Dlg_Confirm["+ e.toString() +"]");
            return e.toString();
        }
    }

    private void get_track_data(List<Location> locList) {
        try {
            int loc_index = 0;
            LatLng p_prec = null;
            double h=0,h_prec=0;

            trackDistance=0;
            deltaH_pos=0;
            deltaH_neg=0;
            h_min=99999;
            h_max=0;

            for (Location loc : locList) {

                LatLng p = new LatLng(loc.getLatitude(),loc.getLongitude());
                h = loc.getAltitude();

                if (loc_index > 0){
                    if ( h >= h_prec ){
                        deltaH_pos += h - h_prec;
                    }
                    else{
                        deltaH_neg +=h_prec - h;
                    }
                }

                if (h>h_max) h_max = h;
                if (h<h_min) h_min = h;

                if (loc_index > 0)
                    trackDistance += SphericalUtil.computeDistanceBetween(p_prec, p);

                p_prec = new LatLng(loc.getLatitude(),loc.getLongitude());
                h_prec = h;
                loc_index++;

                during = loc.getTime();
            }



        } catch (Exception e) {
            gbl.myLog("ERRORE in get_track_data ["+e.toString()+"]");

        }
    }

    private GPX decodeGPX(File file) throws ParseException {

        GPX gpw_data = null;
        List<Location> loc_list = new ArrayList<Location>();
        List<WayPointType> wp_list = new ArrayList<WayPointType>();
        boolean isDataOk = true;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();

            wp_list = getWayPoint(elementRoot);

            NodeList nodelist_trkpt = elementRoot.getElementsByTagName("trkpt");

            long otherDate=0;

            for(int i = 0; i < nodelist_trkpt.getLength(); i++){

                Node node = nodelist_trkpt.item(i);
                NamedNodeMap attributes = node.getAttributes();

                String newLatitude = attributes.getNamedItem("lat").getTextContent();
                Double newLatitude_double = Double.parseDouble(newLatitude);

                String newLongitude = attributes.getNamedItem("lon").getTextContent();
                Double newLongitude_double = Double.parseDouble(newLongitude);
                Double newElevation =-1.0;
                String newTime_string ="";
                NodeList nList = node.getChildNodes();

                for(int j=0; j<nList.getLength(); j++) {
                    Node el = nList.item(j);

                    if(el.getNodeName().equals("ele")) {
                        newElevation = Double.parseDouble(el.getTextContent());
                    }else if(el.getNodeName().equals("time")){
                        newTime_string = el.getTextContent();
                    }
                }

                if (newElevation<0)
                    newElevation = -1.0; //getAltitude(newLatitude_double,newLongitude_double);

                if(newTime_string.length()>0)
                    otherDate = getMillisecond(newTime_string)/1000;
                else {
                    otherDate = 0;
                    isDataOk = false;
                }

                String newLocationName = newLatitude + ":" + newLongitude;
                Location newLocation = new Location(newLocationName);
                newLocation.setLatitude(newLatitude_double);
                newLocation.setLongitude(newLongitude_double);
                newLocation.setAltitude(newElevation);
                newLocation.setTime(otherDate);
                loc_list.add(newLocation);

            }

            fileInputStream.close();
            gpw_data = new GPX(loc_list, wp_list);
            gpw_data.setDataOk(isDataOk);

        } catch (ParserConfigurationException e) {
            gbl.myLog("ERRORE in decodeGPX ["+e.toString()+"]");
            e.printStackTrace();
            gpw_data=null;
        } catch (FileNotFoundException e) {
            gbl.myLog("ERRORE in decodeGPX ["+e.toString()+"]");
            e.printStackTrace();
            gpw_data=null;
        } catch (SAXException e) {
            gbl.myLog("ERRORE in decodeGPX ["+e.toString()+"]");
            e.printStackTrace();
            gpw_data=null;
        } catch (IOException e) {
            gbl.myLog("ERRORE in decodeGPX ["+e.toString()+"]");
            e.printStackTrace();
            gpw_data=null;
        } catch(Exception e){
            gbl.myLog("ERRORE in decodeGPX ["+e.toString()+"]");
            gpw_data=null;
        }


        return gpw_data;
    }

    private long getMillisecond(String d) throws ParseException {

        d = d.replace("T", " ");
        d = d.replace("Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(d);

        return date.getTime();
    }

    private List<WayPointType> getWayPoint (Element elementRoot){
        try{
        List<WayPointType> wp_list = new ArrayList<WayPointType>();

        NodeList nodelist_wpt = elementRoot.getElementsByTagName("wpt");

        for(int i = 0; i < nodelist_wpt.getLength(); i++){

            Node node = nodelist_wpt.item(i);

            NamedNodeMap attributes = node.getAttributes();

            String lat = attributes.getNamedItem("lat").getTextContent();
            String lon = attributes.getNamedItem("lon").getTextContent();
            String ele="", name = "", cmt = "", desc = "", sym = "";

            NodeList nList = node.getChildNodes();

            for(int j=0; j<nList.getLength(); j++) {
                Node el = nList.item(j);

                if(el.getNodeName().equals("ele")) {
                    ele = el.getTextContent();
                }else if(el.getNodeName().equals("name")){
                    name = el.getTextContent();
                }else if(el.getNodeName().equals("cmt")){
                    cmt = el.getTextContent();
                }else if(el.getNodeName().equals("desc")){
                    desc = el.getTextContent();
                }else if(el.getNodeName().equals("sym")){
                    sym = el.getTextContent();
                }
            }

            WayPointType wp = new WayPointType(lat,lon,ele,name,cmt,desc,sym);

            wp_list.add(wp);

        }

        return wp_list;

        } catch(Exception e){
            gbl.myLog("ERRORE in getWayPoint ["+e.toString()+"]");
            return null;
        }
    }
}
