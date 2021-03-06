package bop.provalayout;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;


public class Track_OSM {

    private Location mLastLoc = new Location("LAST");   // Ultima Location della traccia
    private GeoPoint mStartLoc ; // Prima Location della traccia
    private int mTrackColor;
    private String mTrackId;
    private GPSDatabase mDatabase;



    private String mName = "";
    private String mLength = "";
    private String mHmax="";
    private String mHmin="";
    private String mDeltaHPos="";
    private String mDeltaHNeg="";
    private String mStartDate="";
    private String mEndDate="";
    private String mDuring="";
    private boolean mIsSelected=false;

    private boolean mIsVisible = false;
    private Global gbl = new Global();

    public Polyline polyline;
    public ArrayList<Marker> lst_marker;
    public ArrayList<Marker> lst_waypoints_marker;
    public ArrayList<Marker> lst_direction_marker;
    public List<GeoPoint> lst_geoPoint;
    public Marker startMarker, endMarker;
    public BoundingBox boundingBox;

    private MapView mMap;
    private Context mCtx;

    public ArrayList<Marker> getLst_direction_marker() {
        updOrientationOfDirectionMarker();
        return lst_direction_marker;
    }

    public void setColor(int color){
        polyline.setColor(color);
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public String getLength() {
        return mLength;
    }

    public String getDeltaHPos() {
        return mDeltaHPos;
    }

    public String getDeltaHNeg() {
        return mDeltaHNeg;
    }

    public String getHmax() {
        return mHmax;
    }

    public String getHmin() {
        return mHmin;
    }

    public String getDuring() {
        return mDuring;
    }

    public Track_OSM(MapView map, String trackId, GPSDatabase db, Context ctx) {
        int NAME=0,VISIBLE=1,LENGTH=2,HMAX=3,HMIN=4,DELTAHPOS=5,DELTAHNEG=6,DURING=7,START=8,END=9;
        mMap = map;
        mCtx = ctx;
        mTrackColor = 0;
        mTrackId    = trackId;
        mDatabase = db;
        lst_marker = new ArrayList<>();
        lst_direction_marker = new ArrayList<>();
        lst_waypoints_marker = new ArrayList<>();
        ArrayList<String> details = new ArrayList<String>();

        details = mDatabase.getTrackDetails(mTrackId);

        mName = details.get(NAME);
        mLength= details.get(LENGTH);
        mHmax= details.get(HMAX);
        mHmin= details.get(HMIN);
        mDeltaHPos= details.get(DELTAHPOS);
        mDeltaHNeg= details.get(DELTAHNEG);
        mDuring= details.get(DURING);
        mStartDate= details.get(START);
        mEndDate= details.get(END);

        if ( details.get(VISIBLE).equals("1"))
            mIsVisible = true;
        else
            mIsVisible = false;

        polyline = getPolyline();
        lst_waypoints_marker = getWayPointList();
    }

    public String getStartDate() {
        return mStartDate;
    }

    public String getEndDate() {
        return mEndDate;
    }

    public Track_OSM(String trackId, GPSDatabase db) {
        int NAME=0,VISIBLE=1,LENGTH=2,HMAX=3,HMIN=4,DELTAHPOS=5,DELTAHNEG=6,DURING=7,START=8,END=9;
        mTrackColor = 0;
        mTrackId    = trackId;
        mDatabase = db;
        lst_marker = new ArrayList<>();
        lst_direction_marker = new ArrayList<>();
        lst_waypoints_marker = new ArrayList<>();
        ArrayList<String> details = new ArrayList<String>();

        details = mDatabase.getTrackDetails(mTrackId);

        mName = details.get(NAME);
        mLength= details.get(LENGTH);
        mHmax= details.get(HMAX);
        mHmin= details.get(HMIN);
        mDeltaHPos= details.get(DELTAHPOS);
        mDeltaHNeg= details.get(DELTAHNEG);
        mDuring= details.get(DURING);
        mStartDate= details.get(START);
        mEndDate= details.get(END);

        if ( details.get(VISIBLE).equals("1"))
            mIsVisible = true;
        else
            mIsVisible = false;

        polyline = getPolyline();
        lst_waypoints_marker = getWayPointList();
    }

    private void updOrientationOfDirectionMarker(){

        if(mMap.getMapOrientation()==gbl.getMapOrientation())
            return;

        for(Marker m:lst_direction_marker){
            float deltaAngle = mMap.getMapOrientation() - gbl.getMapOrientation();
            m.setRotation(m.getRotation() + deltaAngle);
        }

    }

    private Polyline getPolyline() {
        int LAT = 2, LON = 3, ALT = 4;
        Cursor positions_cur;
        String whereFields = "trackId=?";
        String[] whereValues = new String[]{mTrackId};
        GeoPoint lastGeoPoint = new GeoPoint(0.0, 0.0, 0.0);
        lst_geoPoint =  new ArrayList<>();

        Polyline polyline = new Polyline();

        try {
            double maxLat = -1, minLat = 99999, maxLon = -1, minLon = 99999, d = 0, h = 0;
            positions_cur = mDatabase.choiceData("track", whereFields, whereValues);
            positions_cur.moveToFirst();
            GeoPoint gp_direction = new GeoPoint(0,0,0.0);
            //Prendo la lista delle posizioni della traccia passata in input

            for (int i = 0; i < positions_cur.getCount(); i++) {

                GeoPoint gp = new GeoPoint(positions_cur.getDouble(LAT), positions_cur.getDouble(LON), positions_cur.getDouble(ALT));

                // Utile per Gestione dello zoom dell'intera traccia
                if (gp.getLatitude() > maxLat) maxLat = gp.getLatitude();
                if (gp.getLatitude() < minLat) minLat = gp.getLatitude();
                if (gp.getLongitude() > maxLon) maxLon = gp.getLongitude();
                if (gp.getLongitude() < minLon) minLon = gp.getLongitude();

                lst_geoPoint.add(gp);

                if (i==0){
                    mStartLoc = gp;
                    startMarker = new Marker(mMap);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    startMarker.setPosition(gp);
                    startMarker.setTitle(mCtx.getString(R.string.start_marker_title) + " - " + mName);
                    startMarker.setSnippet(String.format("d:%.0fm - Alt:%.0fm", d,gp.getAltitude()));
                    startMarker.setIcon(ResourcesCompat.getDrawable(mCtx.getResources(),R.mipmap.flag_map_marker_green,null));
                }

                if (i>0)
                    d += gp.distanceTo(lastGeoPoint);

                String snippet = String.format("d:%.0fm - Alt:%.0fm", d,gp.getAltitude());

                org.osmdroid.views.overlay.Marker cur_marker = new org.osmdroid.views.overlay.Marker(mMap);
                cur_marker.setPosition(gp);
                cur_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                cur_marker.setTitle(mName);
                cur_marker.setSnippet(snippet);
                cur_marker.setPanToView(true);
                cur_marker.setIcon(ResourcesCompat.getDrawable(mCtx.getResources(),R.mipmap.flag_map_marker_purple,null));
                cur_marker.closeInfoWindow();

                lst_marker.add(cur_marker);

                if (i==0)
                    gp_direction = gp;

                if( gp.distanceTo(gp_direction) > 100 ) {
                    gp_direction = gp;
                    org.osmdroid.views.overlay.Marker dir_marker = new org.osmdroid.views.overlay.Marker(mMap);

                    dir_marker.setPosition(gp);
                    dir_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    dir_marker.setIcon(ResourcesCompat.getDrawable(mCtx.getResources(), R.mipmap.dir_marker, null));
                    dir_marker.closeInfoWindow();

                    dir_marker.setRotation((long) lastGeoPoint.bearingTo(gp) + mMap.getMapOrientation());

                    lst_direction_marker.add(dir_marker);
                }


                lastGeoPoint = gp;
                positions_cur.moveToNext();
            }

            endMarker= new org.osmdroid.views.overlay.Marker(mMap);
            endMarker.setPosition(lastGeoPoint);
            endMarker.setTitle(mCtx.getString(R.string.end_marker_title) + " - " + mName);
            endMarker.setSnippet(String.format("d:%.0fm - Alt:%.0fm", d,lastGeoPoint.getAltitude()));
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            endMarker.setIcon(ResourcesCompat.getDrawable(mCtx.getResources(),R.mipmap.flag_map_marker_red,null));

            boundingBox = new BoundingBox(maxLat,maxLon,minLat,minLon);
            positions_cur.close();

            polyline.setPoints(lst_geoPoint);

            polyline.setWidth(3);

            return polyline;

        } catch (Exception e) {
            Log.w("MY_CHECK", "getPolyline ERRORE [" + e.toString() + "] ");
            return polyline;
        }
    }

    private ArrayList<Marker> getWayPointList() {
        try {
            int WP_LAT = 2, WP_LON = 3, WP_ALT = 4, WP_NAME = 5, SYM = 8;
            double wp_d;

            Cursor wp_cursor = mDatabase.sel_WayPoints(mTrackId);

            wp_cursor.moveToFirst();

            for (int i = 0; i < wp_cursor.getCount(); i++) {

                Marker m = new Marker(mMap);

                GeoPoint gp = new GeoPoint(0.0,0.0,0.0);

                gp.setLatitude(wp_cursor.getDouble(WP_LAT));
                gp.setLongitude(wp_cursor.getDouble(WP_LON));
                gp.setAltitude(wp_cursor.getDouble(WP_ALT));

                wp_d = gp.distanceTo(mStartLoc);

                m.setPosition(gp);

                m.setTitle(wp_cursor.getString(WP_NAME));
                m.setSnippet(buildSnippet(wp_d, wp_cursor.getDouble(WP_ALT)));

                // Se non trovo l'icona giusta tra quelle disponibili --> metto quella di default
                Drawable icon;
                try {
                    icon = ResourcesCompat.getDrawable(mCtx.getResources(), wp_cursor.getInt(SYM), null);
                }
                catch(Exception ex){
                   // Log.w("MY_CHECK", "Errore in icon error[" + ex.toString() + "]");
                    icon = ResourcesCompat.getDrawable(mCtx.getResources(), R.mipmap.flag_map_marker_blue, null);
                }

                m.setIcon(icon);
                m.showInfoWindow();

                lst_waypoints_marker.add(m);

                wp_cursor.moveToNext();
            }
            wp_cursor.close();

            return lst_waypoints_marker;

        } catch (Exception e) {
            Log.w("MY_CHECK", "Errore in getWayPointList[" + e.toString() + "]");
            return null;
        }
    }

    public String getTrackId() {
        return mTrackId;
    }

    public String getTrackName() {
        return mName;
    }

    public void setTrackName(String mName) {
        this.mName = mName;
    }

    // Costruisce lo snippet del MarkerOpotions
    private String buildSnippet(double d, double h){
        String sTitle="";
        if (h > 0)
            sTitle = String.format("D=%.0fm - H=%.0fm", d, h);
        else
            sTitle = String.format("D=%.0fm", d);

        return sTitle;
    }
}
