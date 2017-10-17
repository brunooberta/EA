package bop.provalayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

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
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Adm on 13/10/2017.
 */

public class EA_FileChooserActivity extends AppCompatActivity {

    private GPSDatabase myDatabase;
    private ListView list;
    private String trackId = "", trackName  = "";
    private ArrayList<WayPointType> wp_array = new ArrayList<>();
    private wp_ListAdapter myLstAdaper;
    private boolean isBtnPressed=false; // Utilizzato nell'onStop se chiudo l'app --> onStop --> vado su MainActivity se c'è in corso una REC
    private int screen_width =0;

    private File currentDir;
    private FileArrayAdapter adapter;
    private ArrayList<String> ext = new ArrayList<>();
    private String path = Environment.getExternalStorageDirectory().getPath();
    private String[] arr_trackId = new String[]{};
    private String selected_date = "";
    private boolean isGpx = true, ISFILE = true, ISDIR = false;
    private Global gbl = new Global();
    private String path_back = "";
    private ArrayList<String> path_History = new ArrayList();
    private double trackDistance = 0, deltaH_pos = 0, deltaH_neg = 0, h_min = 99999, h_max = 0, during = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }

            setContentView(R.layout.activity_ea_filechooser);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_fc);
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
                ext = extras.getStringArrayList("extension");
                arr_trackId = extras.getStringArray("arr_trackId");
                selected_date = extras.getString("selected_date");
                isGpx = extras.getBoolean("isGpx");
            }

            // Configurato nell'XML per la gestione dei file di mappe offline
            String data = getIntent().getDataString();
            if (data == null) data = "";
            if(data.equals("offlinemap")) {
                ext.add("zip");
                ext.add("sqlite");
                ext.add("gemf");
                isGpx = false;
            }else
                isGpx = true;

            path += "/Download";

            currentDir = new File(path);

            list = (ListView) findViewById(R.id.lst_fc_file);
            list.setClickable(true);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick( AdapterView<?> adapterView, View view, int i, long l) {

                    Item o = adapter.getItem(i);
                    onFileClick(o);
                }
            });


            fill(currentDir);

        }
        catch(Exception e){
            Log.w("MY_CHECK", " ERRORE in onCreate [" + e.toString() + "]");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_file_chooser, menu);

            FontManager.manageMenuItem(menu, screen_width,getBaseContext(),this);

            return true;
        }catch(Exception e){
            gbl.myLog(e.toString());
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_fc_item_go_to_tracklist:
                // if (gbl.getRecordingTime() != 0)
                goToTrackList();
                return true;
            case R.id.menu_fc_item_go_to_map:
                go_to_map();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Consente di raggiungere la lista delle tracce
    private void goToTrackList() {
        try {
            Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
            intent.putExtra("arr_trackId", arr_trackId);
            intent.putExtra("selected_date", selected_date);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goToTrackList [" + e.toString() + "]");
        }
    }

    // Consente di spostarsi nella vista con la mappa
    public void go_to_map() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            Log.w("MY_CHECK","go_to_map ERRORE["+ e.toString() +"]" );
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void fill(File f)    {
        File[]dirs = f.listFiles();
        this.setTitle("Current Dir: "+f.getName());
        List<Item> dir = new ArrayList<Item>();
        List<Item>fls = new ArrayList<Item>();
        try{
            int size = path_History.size();
            if(size>0)
                path_back = path_History.get(size - 1);
            else
                path_back = "";

            path_History.add(f.getPath());
            File up_file = new File(f.getParent()), back_file = new File(path_back);
            if(path_back.length()>0)
                dir.add(new Item(" Back to " + back_file.getName(),"","",path_back,ISDIR,getString(R.string.fa_arrow_left),"BACK"));

            if(f.getParent().length()>1) {
                dir.add(new Item(" Up to " + up_file.getName(), "", "", f.getParent(), ISDIR, getString(R.string.fa_arrow_up), "UP"));
            }
            if(dirs != null) {
                for (File ff : dirs) {
                    Date lastModDate = new Date(ff.lastModified());
                    DateFormat formater = DateFormat.getDateTimeInstance();
                    String date_modify = formater.format(lastModDate);
                    if (ff.isDirectory()) {
                        File[] fbuf = ff.listFiles();
                        int buf = 0;
                        if (fbuf != null) {

                            buf = fbuf.length;
                        } else buf = 0;
                        String num_item = String.valueOf(buf);
                        if (buf == 0) num_item = num_item + " item";
                        else num_item = num_item + " items";
                        dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), ISDIR, getString(R.string.fa_folder_o), ""));

                    } else {
                        for (String e : ext) {
                            if (ff.getName().contains("." + e))
                                fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), ISFILE, getString(R.string.fa_file_text_o), ""));
                        }
                    }
                }
            }

        }catch(Exception e){ gbl.myLog("ERRORE in FileChooser -> fill ["+e.toString()+"]");}

        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);

        if(!f.getName().equalsIgnoreCase("sdcard")) {
            //dir.add(0, new Item("..", "Parent Directory", "", f.getParent(), "directory_up"));
        }

        adapter = new FileArrayAdapter(EA_FileChooserActivity.this,R.layout.file_view,dir);
        list.setAdapter(adapter);
        list.setPadding(3,3,3,3);
    }

    private void setOfflineMapPreference(String mapName){
        try {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.pref_offlinemap_key), mapName);
            editor.commit();

            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            gbl.myLog("ERRORE in onClick(SET_OFFLINE_MODE)[" + e.toString() + "]");
        }
    }

    private void onFileClick(Item o)    {
        try {

            if(o.isFile()) {
                if(isGpx)
                    Dlg_Confirm(o.getName());
                else
                    Dlg_Confirm("SET_MAP_OFFLINE", o.getName());

            }
            else{
                int size = path_History.size();
                currentDir =  new File(o.getPath());

                if(!o.isBack()) {
                    fill(currentDir);
                }
                else{
                    if(size>1 ) {
                        path_History.remove(path_History.size() - 1);
                        path_History.remove(path_History.size() - 1);
                        fill(currentDir);
                    }
                }
            }
        }
        catch(Exception e){ gbl.myLog("ERRORE in FileChooser -> onFileClick ["+e.toString()+"]");}
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

            long otherDate = 0;

            for (int i = 0; i < nodelist_trkpt.getLength(); i++) {

                Node node = nodelist_trkpt.item(i);
                NamedNodeMap attributes = node.getAttributes();

                String newLatitude = attributes.getNamedItem("lat").getTextContent();
                Double newLatitude_double = Double.parseDouble(newLatitude);

                String newLongitude = attributes.getNamedItem("lon").getTextContent();
                Double newLongitude_double = Double.parseDouble(newLongitude);
                Double newElevation = -1.0;
                String newTime_string = "";
                NodeList nList = node.getChildNodes();

                for (int j = 0; j < nList.getLength(); j++) {
                    Node el = nList.item(j);

                    if (el.getNodeName().equals("ele")) {
                        newElevation = Double.parseDouble(el.getTextContent());
                    } else if (el.getNodeName().equals("time")) {
                        newTime_string = el.getTextContent();
                    }
                }

                if (newElevation < 0)
                    newElevation = -1.0; //getAltitude(newLatitude_double,newLongitude_double);

                if (newTime_string.length() > 0)
                    otherDate = getMillisecond(newTime_string) / 1000;
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
            gbl.myLog("ERRORE in decodeGPX [" + e.toString() + "]");
            e.printStackTrace();
            gpw_data = null;
        } catch (FileNotFoundException e) {
            gbl.myLog("ERRORE in decodeGPX [" + e.toString() + "]");
            e.printStackTrace();
            gpw_data = null;
        } catch (SAXException e) {
            gbl.myLog("ERRORE in decodeGPX [" + e.toString() + "]");
            e.printStackTrace();
            gpw_data = null;
        } catch (IOException e) {
            gbl.myLog("ERRORE in decodeGPX [" + e.toString() + "]");
            e.printStackTrace();
            gpw_data = null;
        } catch (Exception e) {
            gbl.myLog("ERRORE in decodeGPX [" + e.toString() + "]");
            gpw_data = null;
        }


        return gpw_data;
    }

    private String SaveTrackOnDB(String trackName, GPX gpx_data) {
        try {
            GPSDatabase myDatabase = new GPSDatabase(getApplicationContext());
            myDatabase.open();

            int trackId = -1;
            if (gpx_data.m_location_list.size() > 0) {
                get_track_data(gpx_data.m_location_list);

                trackId = myDatabase.ins_GPX_data(trackName, gpx_data.m_location_list, trackDistance, h_max, h_min, deltaH_pos, deltaH_neg, during);

                if (gpx_data.m_wayPoint_list.size() > 0 && trackId > 0)
                    myDatabase.ins_GPX_WayPoint_lst("" + trackId, gpx_data.m_wayPoint_list);

                gbl.getTrackOsmCollection().addTrackToCollection("" + trackId);
            }

            myDatabase.manage_TrackInfo_Rows("start tracking", "0");
            myDatabase.close();

            return "";
        } catch (Exception e) {
            gbl.myLog("ERRORE in Dlg_Confirm[" + e.toString() + "]");
            return e.toString();
        }
    }

    private void Dlg_Confirm(final String fileName) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(EA_FileChooserActivity.this);

            myBuilder.setCancelable(true);

            final AlertDialog dlg = myBuilder.create();

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final EA_EditText et_trackName = new EA_EditText(this);
            TextInputLayout til_trackName = new TextInputLayout(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                et_trackName.setId(View.generateViewId());
                til_trackName.setId(View.generateViewId());
            }
            et_trackName.setLayoutParams(lp);
            til_trackName.setLayoutParams(lp);
            MyTextWatcher tw_fe_editText = new MyTextWatcher(et_trackName, til_trackName, getApplicationContext());
            et_trackName.addTextChangedListener(tw_fe_editText);

            InputFilter filter = new InputFilter() {
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isSpaceChar(source.charAt(i))) {
                            if (!Character.isLetterOrDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                    }
                    return null;
                }
            };

            et_trackName.setFilters(new InputFilter[]{filter});
            et_trackName.setHint("File name");

            String s = fileName.substring(0,fileName.lastIndexOf("."));
            et_trackName.setText(s);

            til_trackName.addView(et_trackName);

            dlg.setView(et_trackName);
            dlg.setView(til_trackName);

            dlg.setTitle("Import File");

            dlg.setMessage("Modify File Name and press Save Button:");

            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {}
            });

            dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                    "SAVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                File f = new File(currentDir + "/" + fileName);

                                if (et_trackName.getText().length() > 0 ) {
                                    if (!f.exists())
                                        Dlg_Confirm("FILE_NOT_EXIST", "");
                                    else {
                                        GPX gpx_data = new GPX();
                                        String retValue = "";
                                        gpx_data = decodeGPX(f);

                                        if (gpx_data != null) {
                                            retValue = SaveTrackOnDB(et_trackName.getText().toString(), gpx_data);
                                            if (gpx_data.isDataOk()) {
                                                Dlg_Confirm("IMPORT OK", retValue);
                                            } else {
                                                Dlg_Confirm("IMPORT OK WITH ERROR", retValue);
                                            }
                                        } else
                                            Dlg_Confirm("IMPORT NOK", retValue);
                                    }

                                }
                            } catch (Exception e) {
                                gbl.myLog("ERRORE in onClick [" + e.toString() + "]");
                            }
                        }
                    });

            dlg.show();

        } catch (Exception e) {
            gbl.myLog("ERRORE in Dlg_Confirm[" + e.toString() + "]");
        }
    }

    private void Dlg_Confirm(String operation, final String note) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(EA_FileChooserActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();
            dlg.setTitle("Import Track");
            switch (operation) {
                case "SET_MAP_OFFLINE":
                    dlg.setMessage("Setting Offline Map");
                    dlg.setMessage("Do you want to select this map?");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {setOfflineMapPreference(note);}
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

                case "SET_OFFLINE_MODE":
                    dlg.setMessage("Setting Successfully.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(getString(R.string.pref_offlinemap_key), note);
                                editor.commit();

                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            } catch (Exception e) {
                                gbl.myLog("ERRORE in onClick(SET_OFFLINE_MODE)[" + e.toString() + "]");
                            }
                        }
                    });
                    break;
                case "FILE_NOT_EXIST":
                    dlg.setMessage("Attention Please...");
                    dlg.setMessage("The Selected file doesn't exist.");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

                case "IMPORT OK":

                    if (note.length() == 0)
                        dlg.setMessage("Import Successfully.");
                    else
                        dlg.setMessage("Import Failed: [" + note + "]");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                goToTrackList();
                            } catch (Exception e) {
                                gbl.myLog("ERRORE in onClick(IMPORT OK)[" + e.toString() + "]");
                            }
                        }
                    });
                    break;

                case "IMPORT OK WITH ERROR":

                    if (note.length() == 0)
                        dlg.setMessage("Import Successfully but with errors in parsing phase.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                goToTrackList();
                            } catch (Exception e) {
                                gbl.myLog("ERRORE in onClick(IMPORT OK WITH ERROR)[" + e.toString() + "]");
                            }
                        }
                    });
                    break;

                case "IMPORT NOK":

                    dlg.setMessage("Import Failed.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    break;
            }

            dlg.show();


        } catch (Exception e) {
            gbl.myLog("ERRORE in Dlg_Confirm[" + e.toString() + "]");
        }

    }

    private long getMillisecond(String d) throws ParseException {

        d = d.replace("T", " ");
        d = d.replace("Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = sdf.parse(d);

        return date.getTime();
    }

    private List<WayPointType> getWayPoint(Element elementRoot) {
        try {
            List<WayPointType> wp_list = new ArrayList<WayPointType>();

            NodeList nodelist_wpt = elementRoot.getElementsByTagName("wpt");

            for (int i = 0; i < nodelist_wpt.getLength(); i++) {

                Node node = nodelist_wpt.item(i);

                NamedNodeMap attributes = node.getAttributes();

                String lat = attributes.getNamedItem("lat").getTextContent();
                String lon = attributes.getNamedItem("lon").getTextContent();
                String ele = "", name = "", cmt = "", desc = "", sym = "";

                NodeList nList = node.getChildNodes();

                for (int j = 0; j < nList.getLength(); j++) {
                    Node el = nList.item(j);

                    if (el.getNodeName().equals("ele")) {
                        ele = el.getTextContent();
                    } else if (el.getNodeName().equals("name")) {
                        name = el.getTextContent();
                    } else if (el.getNodeName().equals("cmt")) {
                        cmt = el.getTextContent();
                    } else if (el.getNodeName().equals("desc")) {
                        desc = el.getTextContent();
                    } else if (el.getNodeName().equals("sym")) {
                        sym = el.getTextContent();
                    }
                }

                WayPointType wp = new WayPointType(lat, lon, ele, name, cmt, desc, sym);

                wp_list.add(wp);

            }

            return wp_list;

        } catch (Exception e) {
            gbl.myLog("ERRORE in getWayPoint [" + e.toString() + "]");
            return null;
        }
    }

    private void get_track_data(List<Location> locList) {
        try {
            int loc_index = 0;
            LatLng p_prec = null;
            double h = 0, h_prec = 0;

            trackDistance = 0;
            deltaH_pos = 0;
            deltaH_neg = 0;
            h_min = 99999;
            h_max = 0;

            for (Location loc : locList) {

                LatLng p = new LatLng(loc.getLatitude(), loc.getLongitude());
                h = loc.getAltitude();

                if (loc_index > 0) {
                    if (h >= h_prec) {
                        deltaH_pos += h - h_prec;
                    } else {
                        deltaH_neg += h_prec - h;
                    }
                }

                if (h > h_max) h_max = h;
                if (h < h_min) h_min = h;

                if (loc_index > 0)
                    trackDistance += SphericalUtil.computeDistanceBetween(p_prec, p);

                p_prec = new LatLng(loc.getLatitude(), loc.getLongitude());
                h_prec = h;
                loc_index++;

                during = loc.getTime();
            }


        } catch (Exception e) {
            gbl.myLog("ERRORE in get_track_data [" + e.toString() + "]");

        }
    }
}
