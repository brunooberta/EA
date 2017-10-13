package bop.ea_free;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooser extends ListActivity {

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		try {

			Bundle extras = getIntent().getExtras();
			if (extras != null) {
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
			fill(currentDir);

		}catch(Exception e){ gbl.myLog("ERRORE in FileChooser -> onCreate ["+e.toString()+"]");}
    }

    private void fill(File f)    {
    	File[]dirs = f.listFiles(); 
		 this.setTitle("Current Dir: "+f.getName());
		 List<Item>dir = new ArrayList<Item>();
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

		 adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter);
		 this.getListView().setPadding(3,3,3,3);
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Item o = adapter.getItem(position);
		onFileClick(o);
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
