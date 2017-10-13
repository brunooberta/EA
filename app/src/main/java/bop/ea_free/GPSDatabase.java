package bop.ea_free;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Adm on 04/05/2017.
 */

public class GPSDatabase {

    private Context context;
    private String FAKE_TRACK_RECORDING_WP = "-1", FAKE_TRACK_GENERIC_WP = "-2";

    private DbHelper dbHelper;

    private final String DBNAME="gps1";
    private final int DBVERSION=3;
    public SQLiteDatabase db;

    private final String CREATE_TBL_TRACKINFO="create table trackInfo(id integer, name text, value text);";
    private final String CREATE_TBL_LOCATION="create table location(locationId integer primary key autoincrement,latitude text not null, longitude text not null, altitude text not null, timestamp text null );";
    private final String CREATE_TBL_TRACK="create table track(trackId integer not null,locationId integer not null,latitude text, longitude text, altitude text not null, timestamp text,PRIMARY KEY ( trackId, locationId) );";
    private final String CREATE_TBL_SAVEDTRACK="create table track_saved(trackId integer not null, description text, visible integer, toFollow integer, polylineId text,length text, h_max text,h_min text, delta_h_pos text, delta_h_neg text, during text, startDate text, endDate text, PRIMARY KEY ( trackId ) );";
    private final String CREATE_TBL_WPT="create table waypoints(id integer primary key autoincrement, trackId integer not null, latitude text, longitude text, altitude text, name text, cmt text, desc text, sym text );";

    /*
    * Tabella location:     contiene i punti che il dispositivo sta tracciando attualmente
    * Tabella track:        contiene i punti di tutte le tracce salvate
    * Tabella track_saved:  contiene le info (ad es Description) delle tracce salvate in track
    * */

    public void updData(){
        Cursor cur;
        String query = "", upd="";

        query = "SELECT a.trackId, b.description, a.latitude, a.longitude, a.altitude, a.timestamp, a.locationId FROM track a, track_saved b WHERE a.trackId=24 and a.trackId = b.trackId ORDER BY a.trackId";
        cur = db.rawQuery(query, null);
        cur.moveToFirst();
        int tstart=0,tdelta=0;
        for(int i=0;i<cur.getCount();i++){

            //Log.w("MY_CHECK", "a.trackId["+cur.getString(0)+"], b.description["+cur.getString(1)+"], a.latitude["+cur.getString(2)+"], a.longitude["+cur.getString(3)+"], a.altitude["+cur.getString(4)+"], a.timestamp["+cur.getString(5)+"]");

            String alt = cur.getString(4);
            if (alt.length()>8)
                alt = alt.substring(0,8);

            upd = "UPDATE track SET altitude = '" +alt +"' WHERE trackId=24 and locationId = " +cur.getString(6);
            //db.execSQL(upd);
            //Log.w("MY_CHECK",upd +  "cur.getString(4)["+cur.getString(4)+"]");
            cur.moveToNext();
        }
        cur.close();

    }

    public void printData(){
        Cursor cur;
        String query = "";

        query = "SELECT a.trackId, b.description, a.latitude, a.longitude, a.altitude, a.timestamp, a.locationId FROM track a, track_saved b WHERE a.trackId = b.trackId ORDER BY a.trackId";
        cur = db.rawQuery(query, null);
        cur.moveToFirst();
        for(int i=0;i<cur.getCount();i++){

            Log.w("MY_CHECK", "a.trackId["+cur.getString(0)+"], b.description["+cur.getString(1)+"], a.latitude["+cur.getString(2)+"], a.longitude["+cur.getString(3)+"], a.altitude["+cur.getString(4)+"], a.timestamp["+cur.getString(5)+"]");

            cur.moveToNext();
        }
        cur.close();

    }

    // I WP non associati ad alcuna traccia li associo alla traccia fittizia con trackId = -1
    public Cursor get_waypoints(String trackId){
        Cursor cur;
        String query = "";

        if (!trackId.equals("-1") && !trackId.equals("-2") && trackId.length()>0 )
            query = "SELECT a.trackId, a.id, b.description, a.name, a.latitude , a.longitude , a.altitude , a.cmt , a.desc , a.sym FROM waypoints a, track_saved b WHERE a.trackId = b.trackId and a.trackId = "+trackId+" ORDER BY a.trackId, a.id";

        else
            query = "SELECT a.trackId, a.id, '', a.name, a.latitude , a.longitude , a.altitude , a.cmt , a.desc , a.sym FROM waypoints a WHERE a.trackId = "+trackId+" ORDER BY a.trackId, a.id";

        cur = db.rawQuery(query, null);

        return cur;
    }

    public Cursor get_track_data(String trackId){
        Cursor cur;
        String query = "";

        query = "SELECT b.description, a.latitude, a.longitude, a.altitude, a.timestamp, b.startDate FROM track a, track_saved b WHERE a.trackId = b.trackId and a.trackId = "+trackId+" ORDER BY a.trackId, a.locationId";

        cur = db.rawQuery(query, null);

        return cur;
    }

    // Restituisce i dati dei  WP di una certa track
    public Cursor get_waypoint_data_for_track(String trackId){
        Cursor cur;
        String query = "";

        query = "SELECT  a.name, a.latitude , a.longitude , a.altitude , a.cmt , a.desc , a.sym FROM waypoints a WHERE a.trackId = "+trackId+" ORDER BY a.id";

        cur = db.rawQuery(query, null);

        return cur;
    }

    // Restituisce i dati di un certo WP
    public Cursor get_waypoint_data(String wpId){
        Cursor cur;
        String query = "";

        query = "SELECT  a.id, a.trackId, replace(a.name, ' ', '-'), a.latitude , a.longitude , a.altitude , a.cmt , a.desc , a.sym FROM waypoints a WHERE a.id = "+wpId+" ORDER BY a.trackId, a.id";

        cur = db.rawQuery(query, null);

        return cur;
    }

    // Restituisce i dati di un certo WP
    public void upd_waypoint_data(String wpId, String name, String latitude, String longitude, String altitude, String desc, int iconId){

        ContentValues values = new ContentValues();

        values.put("name",name);
        values.put("latitude",latitude);
        values.put("longitude",longitude);
        values.put("altitude",altitude);
        values.put("desc",desc);
        values.put("sym",""+iconId);

        db.update( "waypoints",values,"id=?",new String[]{wpId});
    }


    public GPSDatabase(Context context){

        this.context=context;
        dbHelper=new DbHelper(context);
        //dbHelper.onUpgrade(dbHelper.getWritableDatabase(),3,4);
    }

    public class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context){
            super(context,DBNAME,null,DBVERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TBL_LOCATION);
            db.execSQL(CREATE_TBL_TRACK);
            db.execSQL(CREATE_TBL_SAVEDTRACK);
            db.execSQL(CREATE_TBL_TRACKINFO);
            db.execSQL(CREATE_TBL_WPT);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Log.w("MY_CHECK","onUpgrade");
                db.execSQL("DROP TABLE IF EXISTS location");
                db.execSQL("DROP TABLE IF EXISTS track");
                db.execSQL("DROP TABLE IF EXISTS track_saved");
                db.execSQL("DROP TABLE IF EXISTS trackInfo");
                db.execSQL("DROP TABLE IF EXISTS waypoints");

                onCreate(db);
            }
            catch(Exception e){
                Log.w("MY_CHECK","ERRORE in onUpgrade ["+e.toString()+"]");
            }

        }

    }

    public void manage_TrackInfo_Rows(String nome, String valore){
        try {

            Cursor cur =null;
            String[] whereValues = new String[]{nome};

            cur = choiceData("trackInfo","name=?",whereValues);

            ContentValues values = new ContentValues();

            values.put("name", nome);
            values.put("value", valore);

            if (cur.getCount()==0) {
                db.insert("trackInfo", null, values);
            }
            else{
                db.update( "trackInfo",values,"name=?",new String[]{nome});
            }
            cur.close();
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in manage_TrackInfo_Rows ["+e.toString()+"]");
        }

    }

    public long ins_Location_Rows(String latitude, String longitude, String altitude){
        try {

            ContentValues value = new ContentValues();

            value.put("latitude", latitude);
            value.put("longitude", longitude);
            value.put("timestamp", getDate());
            value.put("altitude", altitude);

            return db.insert("location", null, value);
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in insertRows ["+e.toString()+"]");
        }

        return -1;
    }

    public int get_Max_TrackId(){
        int MAX_TRACKID = 0;
        int maxTrackId = 0;
        Cursor cursor=db.rawQuery("SELECT max(trackId) as max_trackId FROM track",null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            maxTrackId = cursor.getInt(MAX_TRACKID) + 1;
        }
        cursor.close();

        return maxTrackId;

    }

    public int ins_WayPoint(String id, String trackId, String lat, String lon, String alt, String name, String cmt, String desc, String sym){
        int wp_id=-1;
        db.beginTransaction();
        try{
            ContentValues value = new ContentValues();

            value.put("trackId", trackId);
            value.put("latitude", lat);
            value.put("longitude", lon);
            value.put("altitude", alt);
            value.put("name", name);
            value.put("cmt",cmt);
            value.put("desc",desc);
            value.put("sym",sym);

            db.insert("waypoints", null, value);

            db.setTransactionSuccessful();

            Cursor cursor = db.rawQuery("SELECT max(id) FROM waypoints WHERE trackId="+trackId,null);
            cursor.moveToFirst();
            wp_id = cursor.getInt(0);
            cursor.close();


        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_WayPoint["+ e.toString() +"]");
            db.endTransaction();
            return -2;
        } finally {
            db.endTransaction();
            return wp_id;
        }
    }

    public void ins_WayPoint(String id,String trackId, WayPointType wp){
        db.beginTransaction();
        try{
            int ID=0, TRACK_ID = 1, LAT = 1, LON = 2, ALT = 3, TIME = 4, NAME=5, CMT=6,DESC=7,SYM=8;

            ContentValues value = new ContentValues();
            value.put("ID", id);
            value.put("trackId", trackId);
            value.put("latitude", wp.mLat);
            value.put("longitude", wp.mLon);
            value.put("altitude", wp.mEle);
            value.put("name", wp.mName);
            value.put("cmt",wp.mCmt);
            value.put("desc",wp.mDesc);
            value.put("sym",wp.mSym);

            db.insert("waypoints", null, value);

            db.setTransactionSuccessful();
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_WayPoint["+ e.toString() +"]");
            db.endTransaction();
        } finally {
            db.endTransaction();
        }
    }

    public void ins_GPX_WayPoint_lst(String trackId, List<WayPointType> wp_list){
        try{
            int ID=0, TRACK_ID = 1, LAT = 1, LON = 2, ALT = 3, TIME = 4, NAME=5, CMT=6,DESC=7,SYM=8;

            for(int i=0;i<wp_list.size();i++) {
                ins_WayPoint(""+i,trackId, wp_list.get(i));
            }

        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_WayPoint_lst["+ e.toString() +"]");
        }
    }

    public Cursor sel_WayPoints(String trackId) {
        try {
            String whereFields = "trackId=?";
            String[] whereValues = {trackId};

            Cursor cursor = db.query("waypoints", new String[]{"id", "trackId", "latitude", "longitude", "altitude", "name", "cmt", "desc", "sym"}, whereFields, whereValues, null, null, null);
            return cursor;
        }
        catch(Exception e){
            Log.w("MY_CHECK","ERRORE in sel_WayPoints["+ e.toString() +"]");
            return null;
        }
    }

    public void del_WayPoint(String wp_id){
        try {
            db.execSQL("DELETE FROM waypoints WHERE id = " + wp_id);
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in del_WayPoint ["+e.toString()+"]");
        }
    }

    public void del_WayPoints(String id, String trackId) {
        try {
            String slq = "";
            if (id=="")
                slq = "DELETE FROM waypoints WHERE trackId='"+trackId+"'";
            else
                slq = "DELETE FROM waypoints WHERE id='"+id+"' AND trackId='"+trackId+"'";

            db.execSQL(slq);
        }
        catch(Exception e){
            Log.w("MY_CHECK","ERRORE in del_WayPoints["+ e.toString() +"]");
        }
    }

    public void setTrackToFollow(int trackId, boolean isFollowing){
        try {
            String toFollow = "0";
            if (isFollowing)  toFollow="1";

            upd_TrackSaved_Rows(new String[]{"toFollow"},new String[]{toFollow},"trackId=?",new String[]{""+trackId});
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in setTrackToFollow ["+e.toString()+"]");
        }

    }

    // Utilizzata per trasferire i dati della traccia che sto registrando dalla tab LOCATION alla tabella TRACK
    public int ins_Track_Rows(List<Location> lstLocation){
        db.beginTransaction();

        int maxTrackId = 0;
        int LOC_ID = 0, LAT = 1, LONG = 2, ALT = 3, TIME = 4;
        try {
            ContentValues value = new ContentValues();

            // Recupero il MAX TRACK ID
            maxTrackId = get_Max_TrackId();

            for (int i = 0; i < lstLocation.size(); i++) {
                value = new ContentValues();
                value.put("trackId", maxTrackId);
                value.put("locationId", i);
                value.put("latitude", lstLocation.get(i).getLatitude());
                value.put("longitude", lstLocation.get(i).getLongitude());
                value.put("altitude", lstLocation.get(i).getAltitude());
                value.put("timestamp",lstLocation.get(i).getTime());

                db.insert("track", null, value);
            }
            String sql_upd = "UPDATE waypoints SET trackId = " +maxTrackId+ " WHERE trackId = " + FAKE_TRACK_RECORDING_WP;
            db.execSQL( sql_upd );

            db.setTransactionSuccessful();

            return maxTrackId;
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_Track_Rows["+ e.toString() +"]");
            db.endTransaction();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public int ins_Track_Rows(){
        int maxTrackId = 0;
        int LOC_ID = 0;
        int LAT = 1;
        int LONG = 2;
        int ALT = 3;
        int TIME = 4;
        try {
            ContentValues value = new ContentValues();

            // Recupero il MAX TRACK ID
            maxTrackId = get_Max_TrackId();
            Cursor cursor = sel_Location_Rows();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                value = new ContentValues();
                value.put("trackId", maxTrackId);
                value.put("locationId", cursor.getString(LOC_ID));
                value.put("latitude", cursor.getString(LAT));
                value.put("longitude", cursor.getString(LONG));
                value.put("altitude", cursor.getString(ALT));
                value.put("timestamp", cursor.getString(TIME));

                db.insert("track", null, value);

                cursor.moveToNext();
            }
            cursor.close();

            String sql_upd = "UPDATE waypoints SET trackId = " +maxTrackId+ " WHERE trackId = " + FAKE_TRACK_RECORDING_WP;
            db.execSQL( sql_upd );

            return maxTrackId;
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_Track_Rows["+ e.toString() +"]");
            return -1;
        }
    }

    private class dates {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        public String mStartDate;
        public String mEndDate;
        public double mDuring;

        public dates( String startDate,   String endDate) throws ParseException {
            mStartDate = startDate;
            mEndDate = endDate;
            Date date_start = dateFormat.parse(startDate);
            Date date_end = dateFormat.parse(endDate);

            mDuring = (date_end.getTime() - date_start.getTime())/1000;
        }

        public dates(long starTime_sec, long endTime_sec){

            mStartDate = dateFormat.format(new Date(starTime_sec*1000));
            mEndDate = dateFormat.format(new Date(endTime_sec*1000));
            mDuring = endTime_sec - starTime_sec;
        }

    }

    public ArrayList<TrackEvent> getEvents() throws ParseException {
        int TRACKID=0,STARTDATE=1;
        ArrayList<TrackEvent> lst_trackEvent = new ArrayList<>();
        dates myDates=null;
        Cursor cursor = db.rawQuery("SELECT trackId, startDate FROM track_saved ORDER BY startDate",null);
        cursor.moveToFirst();

        for(int i=0; i< cursor.getCount();i++){
            String trackId = cursor.getString(TRACKID);
            String startDate = cursor.getString(STARTDATE);
            lst_trackEvent.add(new  TrackEvent(trackId,startDate));
            cursor.moveToNext();
        }
        cursor.close();

        return lst_trackEvent;
    }

    // TODO: analizzare l'utilizzo della tab trackInfo per le date
    private dates getDates() throws ParseException {
        int VALUE = 0,NAME=1;
        dates myDates=null;
        Cursor cursor = db.rawQuery("SELECT value,name FROM trackInfo WHERE name in ('START_DATE','END_DATE') ORDER BY name",null);
        cursor.moveToFirst();
        String startDate="",endDate="";
        if(cursor.getCount()>0) {

            for (int i = 0; i < cursor.getCount(); i++) {

                if (cursor.getString(NAME).equals("START_DATE")) {
                    startDate = cursor.getString(VALUE); // START_DATE
                }
                if (cursor.getString(NAME).equals("END_DATE")) {
                    endDate = cursor.getString(VALUE); // END_DATE
                }

                cursor.moveToNext();
            }
            cursor.close();
            myDates = new dates(startDate, endDate);
        }


        return myDates;
    }

    private dates getDates_GPX(List<Location> lstLocation) throws ParseException {
        int  lastIndex = lstLocation.size()-1;
        long startTime = lstLocation.get(0).getTime();
        long endTime = lstLocation.get(lastIndex).getTime();

        dates myDates = new dates(startTime, endTime);

        return myDates;
    }

    public int ins_GPX_data(String trackName, List<Location> lstLocation, double trackDistance, double h_max, double h_min, double deltaH_pos, double deltaH_neg, double during){
        try {
            int trackId = ins_Track_Rows(lstLocation);
            dates d = getDates_GPX(lstLocation);

            ContentValues values = new ContentValues();
            values.put("trackId", trackId);
            values.put("description", trackName);
            values.put("visible", 1);
            values.put("toFollow", 0);
            values.put("polylineId", "");
            values.put("length", String.format("%.0f",trackDistance));

            values.put("h_max", String.format("%.0f",h_max));
            values.put("h_min", String.format("%.0f",h_min));
            values.put("delta_h_pos", String.format("%.0f",deltaH_pos));
            values.put("delta_h_neg", String.format("%.0f",deltaH_neg));
            values.put("during", String.format("%.0f",d.mDuring));

            values.put("startDate",d.mStartDate);
            values.put("endDate",d.mEndDate);

            db.insert("track_saved", null, values);

            return trackId;
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_TrackSaved_Rows ["+e.toString()+"]");
            return -1;
        }

    }

    public int ins_TrackSaved_Rows(String trackName, double trackLength, double h_max, double h_min, double deltaH_pos, double deltaH_neg, double during){
        try {
            int trackId = ins_Track_Rows();
            dates d = getDates();

            ContentValues values = new ContentValues();
            values.put("trackId", trackId);
            values.put("description", trackName);
            values.put("visible", 1); // la traccia salvata è immediatamente visibile sulla mappa
            values.put("toFollow", 0);
            values.put("polylineId", "");
            values.put("length", String.format("%.0f",trackLength));
            values.put("h_max", String.format("%.0f",h_max));
            values.put("h_min", String.format("%.0f",h_min));
            values.put("delta_h_pos", String.format("%.0f",deltaH_pos));
            values.put("delta_h_neg", String.format("%.0f",deltaH_neg));

            //20170805 INIZIO MODIFICA
            // values.put("during", String.format("%.0f",d.mDuring));
            values.put("during", String.format("%.0f",during));
            //20170805 FINE MODIFICA

            values.put("startDate",d.mStartDate);
            values.put("endDate",d.mEndDate);

            db.insert("track_saved", null, values);

            return trackId;
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in ins_TrackSaved_Rows ["+e.toString()+"]");
            return -1;
        }

    }

    // Setta/Resetta la traccia da seguire
    public void set_trackToFollow(String polylineId, String valore ){
        try {
            String[] setFields = new String[]{"toFollow"};
            String[] setValues = new String[]{valore};

            ContentValues values = new ContentValues();
            values.put(setFields[0],setValues[0]);

            int retUpdValue = db.update( "track_saved",values,"polylineId=?",new String[]{polylineId});

        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in set_trackToFollow ["+e.toString()+"]");
        }

    }

    // Setta/Resetta la traccia da seguire
    public void set_trackToFollow_OSM(String trackId, String valore ){
        try {
            String[] setFields = new String[]{"toFollow"};
            String[] setValues = new String[]{valore};

            ContentValues values = new ContentValues();
            values.put(setFields[0],setValues[0]);

            int retUpdValue = db.update( "track_saved",values,"trackId=?",new String[]{trackId});

        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in set_trackToFollow_OSM ["+e.toString()+"]");
        }

    }

    // Imposta a Stringa VUOTA il recordo relativo and unceto polylineId
    public void upd_resetPolylineId(String polylineId ){
        try {
            String[] setFields = new String[]{"polylineId"};
            String[] setValues = new String[]{""};
            String whereFields = "polylineId=?";
            String[] whereValues = new String[]{polylineId};

            ContentValues values = new ContentValues();

            for (int i=0; i<setFields.length;i++) {

                values.put(setFields[i],setValues[i]);
            }

            db.update( "track_saved",values,whereFields,whereValues);
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in upd_resetPolylineId ["+e.toString()+"]");
        }

    }

    public void upd_TrackSaved_Rows(String[] setFields, String[] setValues, String whereFields, String[] whereValues){
        try {
            ContentValues values = new ContentValues();

            for (int i=0; i<setFields.length;i++) {

                values.put(setFields[i],setValues[i]);
            }

            db.update( "track_saved",values,whereFields,whereValues);
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in upd_TrackSaved_Rows ["+e.toString()+"]");
        }

    }

    private String getDate(){

        //long time= System.currentTimeMillis()/1000; // inserisco nel DB i secondi
        long time= SystemClock.elapsedRealtime()/1000; // inserisco nel DB i secondi

        return "" + time;
    }

    public String getTrackName(String trackId){
        try {
            String trackName="";
            Cursor retCursor = null;
            retCursor = db.query("track_saved", new String[]{"trackId", "description", "visible","toFollow","polylineId"}, "trackId=?", new String[]{trackId}, null, null, null);
            retCursor.moveToFirst();
            trackName = retCursor.getString(1);
            retCursor.close();
            return trackName;
        }
        catch (Exception e) {
            Log.w("MY_CHECK", "getTrackName --> ERRORE[" + e.toString() + "]");
            return "";
        }
    }

    public Cursor choiceData(String choice, String whereFields, String[] whereValues) {
        try {
            Cursor retCursor = null;

            switch (choice) {

                case "location":
                    retCursor = db.query("location", new String[]{"locationId", "latitude", "longitude", "altitude","timestamp"}, whereFields, whereValues, null, null, null);
                    break;
                case "track":
                    retCursor = db.query("track", new String[]{"trackId", "locationId","latitude", "longitude", "altitude","timestamp"}, whereFields, whereValues, null, null, "trackId,locationId");
                    break;
                case "track_saved":
                    retCursor = db.query("track_saved", new String[]{"trackId", "description","visible","toFollow","polylineId"}, whereFields, whereValues, null, null, null);
                    break;
                case "trackInfo":
                    retCursor = db.query("trackInfo", new String[]{"id", "name", "value"}, whereFields, whereValues, null, null, null);
                    break;
            }

            return retCursor;
        }
        catch(Exception e){
            Log.w("MY_CHECK","ERRORE in choiceData["+ e.toString() +"]");
            return null;
        }
    }

    public Cursor sel_Location_Rows() {
        Cursor cursor = db.query("location", new String[]{"locationId", "latitude", "longitude", "altitude","timestamp"}, null, null, null, null, null);
        return cursor;
    }

    public void del_Location_AllRows(){
        try {
            db.execSQL("DELETE FROM location");
            db.execSQL("DELETE FROM waypoints WHERE trackId = " + FAKE_TRACK_RECORDING_WP);
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in del_Location_AllRows ["+e.toString()+"]");
        }
    }

    public void del_TrackSaved( String[] whereValues){
        try {
            for (int i = 0; i < whereValues.length; i++) {
                // Log.w("MY_CHECK", "del_TrackSaved --> DELETE FROM track_saved WHERE trackId = " + whereValues[i]);
                db.execSQL("DELETE FROM track WHERE trackId = " + whereValues[i]);
                db.execSQL("DELETE FROM track_saved WHERE trackId = " + whereValues[i]);
                db.execSQL("DELETE FROM waypoints WHERE trackId = " + whereValues[i]);

                Global.getTrackOsmCollection().removeTrackFromCollection(whereValues[i]);

            }
        }
        catch (Exception e){

            Log.w("MY_CHECK","ERRORE in del_TrackSaved["+ e.toString() +"]");
        }
    }

    public boolean isOpen(){

        return db.isOpen();
    }

    public String[] getVisibleTrackId(){
        try {
            Cursor cur = db.rawQuery("SELECT trackId FROM track_saved WHERE visible=1", null);
            cur.moveToFirst();

            String[] ret = new String[cur.getCount()];

            for (int i = 0; i < cur.getCount(); i++) {
                ret[i] = cur.getString(0);
                cur.moveToNext();
            }

            cur.close();

            return ret;
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in getVisibleTrackId["+ e.toString() +"]");
            return null;
        }

    }

    public ArrayList<String> getTrackDetails(String trackId){
        ArrayList<String> details = new ArrayList<String>();
        try {

            int NAME=0,VISIBLE=1,LENGTH=2,HMAX=3,HMIN=4,DELTAHPOS=5,DELTAHNEG=6,DURING=7, START=8,END=9;

            Cursor cur = db.rawQuery("SELECT description,visible,length,h_max,h_min,delta_h_pos,delta_h_neg, during, startDate, endDate FROM track_saved WHERE trackId=" + trackId, null);
            cur.moveToFirst();


            if(cur.getCount()>0) {
                details.add(NAME, cur.getString(NAME));
                details.add(VISIBLE, cur.getString(VISIBLE));
                details.add(LENGTH, cur.getString(LENGTH));
                details.add(HMAX, cur.getString(HMAX));
                details.add(HMIN, cur.getString(HMIN));
                details.add(DELTAHPOS, cur.getString(DELTAHPOS));
                details.add(DELTAHNEG, cur.getString(DELTAHNEG));
                details.add(DURING, cur.getString(DURING));
                details.add(START, cur.getString(START));
                details.add(END, cur.getString(END));
            }
            cur.close();

            return details;
        }
        catch (Exception e){

            Log.w("MY_CHECK","ERRORE in getTrackDetails["+ e.toString() +"]");
            return details;
        }

    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close(){

        dbHelper.close();

    }

}
