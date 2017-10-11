package bop.provalayout;

import android.content.Context;
import android.database.Cursor;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adm on 18/09/2017.
 */

public class Track_OSM_Collection {



    private ArrayList<Track_OSM> all_track_osm = new ArrayList<Track_OSM>();
    private Context mCtx = null;
    private GPSDatabase mDb = null;
    private MapView mMap;

    public Track_OSM_Collection(GPSDatabase db, MapView map, Context ctx) {

        mCtx = ctx;
        mDb = db;
        mMap = map;

        List<Integer> lst_trackId = getTrackIdList();

        for(int trackId:lst_trackId) {
            Track_OSM track = new Track_OSM(mMap, ""+trackId, mDb, mCtx);
            all_track_osm.add(track);
        }

    }

    public ArrayList<Track_OSM> get_All_track_osm() {
        return all_track_osm;
    }

    public void addTrackToCollection(String trackId){
        Track_OSM track = new Track_OSM(mMap, trackId, mDb, mCtx);
        all_track_osm.add(track);
    }

    public void removeTrackFromCollection(String trackId){
        int size = all_track_osm.size();

        for(int i=0;i<size;i++) {
            if(all_track_osm.get(i).getTrackId().equals(trackId)) {
                all_track_osm.remove(i);
                break;
            }
        }

    }

    public Track_OSM getTrackFromCollectionByTrackId(String trackId){
        int size = all_track_osm.size();

        for(int i=0;i<size;i++) {
            if(all_track_osm.get(i).getTrackId().equals(trackId)) {
                return all_track_osm.get(i);
            }
        }

        return null;
    }

    private List<Integer> getTrackIdList(){
        int TRACKID = 0;

        Cursor cursor = mDb.choiceData("track_saved","",new String[]{});
        cursor.moveToFirst();

        List<Integer> trackId_lst = new ArrayList<Integer>();

        for (int i = 0; i < cursor.getCount(); i++) {
            trackId_lst.add(cursor.getInt(TRACKID));
            cursor.moveToNext();
        }
        cursor.close();

        return trackId_lst;
    }
}
