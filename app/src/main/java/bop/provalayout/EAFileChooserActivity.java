package bop.provalayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Adm on 13/10/2017.
 */

public class EAFileChooserActivity extends AppCompatActivity {

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

        adapter = new FileArrayAdapter(EAFileChooserActivity.this,R.layout.file_view,dir);
        list.setAdapter(adapter);
        list.setPadding(3,3,3,3);
    }


    private void onFileClick(Item o)    {
        try {

            if(o.isFile()) {

                Intent intent = new Intent(getApplicationContext(), FileexplorerActivity.class);
                intent.putExtra("arr_trackId", arr_trackId);
                intent.putExtra("selected_date", selected_date);
                intent.putExtra("GetPath", currentDir.toString());
                intent.putExtra("GetFileName", o.getName());
                intent.putExtra("isGpx", isGpx);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
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
}
