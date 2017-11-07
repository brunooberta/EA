package bop.provalayout;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class track_ListAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<Track_OSM> mTracksArray = new ArrayList<>();  // Contiene le tracks che devo visualizzare nella lista
    private MapView mMap_osm;
    private String lastTrackIdSelected = ""; // Contiene l'ultimo TrackId Selezionato
    private Track_OSM lastTrackSelected = null; // Contiene l'ultima Track_OSM Selezionata
    private ArrayList<String> mSelectedTrackIdArray = new ArrayList<>(); // Contiene tutti i trackId selezionati
    private ArrayList<Integer> mItemsIdCheckedArray = new ArrayList<>(); // contiene la lista degli indici degli elementi selezionati
    private Global gbl = new Global();

    public track_ListAdapter(ArrayList<Track_OSM> tracksArray, Context context, MapView map_osm){
        mContext = context;
        mTracksArray = tracksArray;
        mMap_osm = map_osm;
    }

    public int getCountSelected() {
        return mItemsIdCheckedArray.size();
    }

    public String getLastTrackIdSelected() { return lastTrackIdSelected; }

    public ArrayList<Integer> getPositionsSelected() { return mItemsIdCheckedArray; }

    public ArrayList<String> getSelectedTrackIdArray() { return mSelectedTrackIdArray; }

    @Override
    public int getCount() {
        return mTracksArray.size();
    }

    @Override
    public Track_OSM getItem(int position) {
        return mTracksArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.track_list_row_layout, parent, false);

            final Track_OSM lstItem = mTracksArray.get(position);

            TextView tv_trackName  = (TextView) convertView.findViewById(R.id.tv_trackName);
            TextView tv_trackLength = (TextView) convertView.findViewById(R.id.tv_trackLength);
            TextView tv_track_DPlus = (TextView) convertView.findViewById(R.id.tv_track_DPlus);
            TextView tv_track_DMinus = (TextView) convertView.findViewById(R.id.tv_track_DMinus);

            tv_trackName.setFocusable(false);
            tv_trackName.setFocusableInTouchMode(false);
            tv_trackLength.setFocusable(false);
            tv_trackLength.setFocusableInTouchMode(false);
            tv_track_DPlus.setFocusable(false);
            tv_track_DPlus.setFocusableInTouchMode(false);
            tv_track_DMinus.setFocusable(false);
            tv_track_DMinus.setFocusableInTouchMode(false);

            tv_trackName.setText(lstItem.getTrackName());
            double L = Double.parseDouble(lstItem.getLength())/1000;

            String track_L = String.format("L = %.2fkm", L);

            tv_trackLength.setText(track_L);
            tv_track_DPlus.setText("D+ = " + lstItem.getDeltaHPos() + "m");
            tv_track_DMinus.setText("D- = " + lstItem.getDeltaHNeg() + "m");

            setBackground(convertView,position);

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Dlg_Confirm("EXPORT_TRACK",mContext,lstItem.getTrackId());

                    return false;
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean isSelected = manageSelection(position);

                    LinearLayout mainLayout = (LinearLayout)v.findViewById(R.id.trackList_mainLayout);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if(isSelected) {
                            mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_selected));
                            if (mMap_osm != null){
                                zoomTrack(position);
                            }
                            gbl.setSelectTrack_osm(lastTrackSelected);
                        }
                        else {
                            mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_normal));
                            gbl.setSelectTrack_osm(null);
                        }

                    }
                }

            });

            return convertView;
        }catch (Exception e){
            Log.w("MY_CHECK", "Errore in getView ["+e.toString()+"]");
            return convertView;
        }
    }

    private void setBackground(View v, int position){
        LinearLayout mainLayout = (LinearLayout)v.findViewById(R.id.trackList_mainLayout);
        for(int i=0;i<mItemsIdCheckedArray.size();i++){

            if(mItemsIdCheckedArray.get(i) == position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_selected));
                }
            }
        }
    }

    // Se l'item index è selezionato --> lo deseleziono e ritorno FALSE, altrimenti lo selezione torno TRUE
    private boolean manageSelection(int index){
        try{
            for(int i=0;i<mItemsIdCheckedArray.size();i++){

                if(mItemsIdCheckedArray.get(i) == index){
                    mItemsIdCheckedArray.remove(i);
                    String trackId_deselect = mTracksArray.get(index).getTrackId();

                    for(int j=0;j<mSelectedTrackIdArray.size();j++){

                        if(mSelectedTrackIdArray.get(j).equals(trackId_deselect)) {
                            mSelectedTrackIdArray.remove(j);
                            break;
                        }
                    }

                    return false;
                }
            }

            mItemsIdCheckedArray.add(index);
            lastTrackIdSelected = mTracksArray.get(index).getTrackId();
            lastTrackSelected = mTracksArray.get(index);
            mSelectedTrackIdArray.add(lastTrackIdSelected);

            return true;

        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in manageSelection [" + e.toString() + "]");
            return false;
        }
    }

    private void zoomTrack(int id) {
        try {
            Track_OSM t = mTracksArray.get(id);
            mMap_osm.zoomToBoundingBox(t.boundingBox, false);
            mMap_osm.invalidate();
        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in zoomTrack [" + e.toString() + "]");

        }

    }

    private void Dlg_Confirm(String operation, final Context ctx, final String trackId) {
        try {
            AlertDialog.Builder myBuilder = new AlertDialog.Builder(ctx);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();

            switch (operation) {

                case "EXPORT_TRACK":
                    dlg.setTitle("Confimation");
                    dlg.setMessage("Do you want to export selected tracks?");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "EXPORT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            GPSDatabase myDatabase;
                            myDatabase = new GPSDatabase(ctx);
                            myDatabase.open();

                            ExportToGpxFormat exportToFile = new ExportToGpxFormat(myDatabase,trackId);

                            myDatabase.close();
                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
                    break;

            }

            dlg.show();


        } catch (Exception e) {
            gbl.myLog( "ERRORE in Dlg_Confirm[" + e.toString() + "]");
        }
    }
}
