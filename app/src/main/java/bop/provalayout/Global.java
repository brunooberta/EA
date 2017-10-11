package bop.provalayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.osmdroid.tileprovider.MapTileProviderArray;

import java.util.ArrayList;

/**
 * Created by Adm on 18/07/2017.
 */

public class Global {


    static public int DLG_BTN_SIZE = 90;
    static public double H_PRECEDENTE = 0;
    static private boolean isSelectTrackLocked = false;
    static private boolean isSelectMode_ON = true;
    static private boolean isCentertMode_ON = false;
    static private boolean isPaused = false;
    static private long recordingTime = 0;
    static private MapTileProviderArray tileProviderArray = null;
    static private String appFolderPath = "";
    static private MainActivity.MyBroadcastReceiver mMyBroadcastReceiver;
    static private float mapOrientation = 0;
    static private boolean isLocked = false;

    static private Track_OSM selectTrack_osm = null;

    static public boolean pref_following_sound_out_of_path,pref_map_is_offline=false, pref_def_select_track_mode=false, pref_def_auto_center_mode=true;
    static public int pref_following_minum_distance;
    static public int pref_gps_geoid_correction;
    static public int pref_following_default_interval_ring;
    static public int pref_gps_minum_distance;
    static public int pref_gps_minum_time_for_direction;
    static public int pref_gps_minum_time = -1;
    static public int pref_map_offline_zoom_max=15;
    static public int pref_map_offline_zoom_min=0;
    static public int pref_default_zoom=-1;
    static public String pref_map_offline="";

    static private Track_OSM_Collection trackOsmCollection = null;

    public static Track_OSM_Collection getTrackOsmCollection() {
        return trackOsmCollection;
    }

    public static void setTrackOsmCollection(Track_OSM_Collection trackOsmCollection) {
        Global.trackOsmCollection = trackOsmCollection;
    }

    public void refresh_Pref_default_zoom(Context context) {
        Global.pref_default_zoom = getPreferenceValue_int(R.string.pref_def_key_zoom, context);;
    }

    public void setPreferences(Context context){
        pref_map_offline = getPreferenceValue_String(R.string.pref_offlinemap_key,context);
        pref_map_is_offline = getPreferenceValue_bool(R.string.pref_offlinemap_switch_key,context);
        pref_gps_minum_time = getPreferenceValue_int(R.string.pref_gps_key_minum_time,context);
        pref_following_default_interval_ring = getPreferenceValue_int(R.string.pref_following_key_default_interval_ring,context) * 1000;
        pref_following_sound_out_of_path = getPreferenceValue_bool(R.string.pref_following_key_sound_out_of_path,context);
        pref_following_minum_distance = getPreferenceValue_int(R.string.pref_following_key_minum_distance,context);
        pref_gps_geoid_correction = getPreferenceValue_int(R.string.pref_gps_key_geoid_correction,context);
        pref_gps_minum_distance = getPreferenceValue_int(R.string.pref_gps_key_minum_distance,context);
        pref_gps_minum_time_for_direction = getPreferenceValue_int(R.string.pref_gps_key_minum_time_for_direction,context) * 1000;
        pref_map_offline = getPreferenceValue_String(R.string.pref_offlinemap_key,context);
        pref_map_is_offline = getPreferenceValue_bool(R.string.pref_offlinemap_switch_key,context);
        pref_map_offline_zoom_max = getPreferenceValue_int(R.string.pref_offlinemap_zoom_max_key,context);
        pref_map_offline_zoom_min=  getPreferenceValue_int(R.string.pref_offlinemap_zoom_min_key,context);
        pref_def_select_track_mode= getPreferenceValue_bool(R.string.pref_def_key_select_track_mode,context);
        pref_def_auto_center_mode= getPreferenceValue_bool(R.string.pref_def_key_auto_center_mode,context);
        pref_default_zoom = getPreferenceValue_int(R.string.pref_def_key_zoom, context);
    }



    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }


    private  String LOG_TAG = "MY_CHECK";

    public void myLog(String text){

        Log.w(LOG_TAG,text);
    }

    public int getAverage(ArrayList<Double> lst){

        double max_value = 0, min_value = 99999999;
        int lst_size = lst.size(), i_max=-1, i_min=-1, cnt_avg = 0;
        double val = 0, sum = 0;

        ArrayList<Double> lst_tmp = new ArrayList<>();

        // Tolgo il valorie minimo e quello massimo
        for(int i=0;i<lst_size;i++){
            val = lst.get(i);
            if(val > max_value) {
                max_value = val;
                i_max = i;
            }else {

                if (val < min_value) {
                    min_value = val;
                    i_min = i;
                }
            }
        }

        for(int i=0;i<lst_size;i++){
            if(i != i_max && i != i_min){
                lst_tmp.add(lst.get(i));
            }
        }

        for(int i=0;i<lst_tmp.size();i++){
            val = lst_tmp.get(i);

            if(val >0 ) {
                cnt_avg++;
                sum += val;
            }
        }

        int avg = (int)(sum/cnt_avg);

        //myLog("min_value["+min_value+"] max_value["+max_value+"] cnt_avg["+cnt_avg+"] sum["+sum+"] avg["+avg+"]");


        return avg;
    }

    public static boolean isLocked() {
        return isLocked;
    }

    public static void setIsLocked(boolean isLocked) {
        Global.isLocked = isLocked;
    }

    public static boolean isCentertMode_ON() {
        return isCentertMode_ON;
    }

    public static void setIsCentertMode_ON(boolean isCentertMode_ON) {
        Global.isCentertMode_ON = isCentertMode_ON;
    }

    public static float getMapOrientation() {
        return mapOrientation;
    }

    public static boolean isSelectTrackLocked() {
        return isSelectTrackLocked;
    }

    public static void setIsSelectTrackLocked(boolean isSelectTrackLocked) {
        Global.isSelectTrackLocked = isSelectTrackLocked;
    }

    public static void setMapOrientation(float mapOrientation) {
        Global.mapOrientation = mapOrientation;
    }

    public static MainActivity.MyBroadcastReceiver getmMyBroadcastReceiver() {
        return mMyBroadcastReceiver;
    }

    public static void setmMyBroadcastReceiver(MainActivity.MyBroadcastReceiver mMyBroadcastReceiver) {
        Global.mMyBroadcastReceiver = mMyBroadcastReceiver;
    }

    public static String getAppFolderPath() {
        return appFolderPath;
    }

    public static void setAppFolderPath(String appFolderPath) {
        Global.appFolderPath = appFolderPath;
    }

    public static MapTileProviderArray getTileProviderArray() {
        return tileProviderArray;
    }

    public static void setTileProviderArray(MapTileProviderArray tileProviderArray) {
        Global.tileProviderArray = tileProviderArray;
    }

    public static long getRecordingTime() {
        return recordingTime;
    }

    public static void setRecordingTime(long recordingTime) {
        Global.recordingTime = recordingTime;
    }

    public static boolean isSelectMode_ON() {
        return isSelectMode_ON;
    }

    public static void setIsSelectMode_ON(boolean isSelectMode_ON) {
        Global.isSelectMode_ON = isSelectMode_ON;
    }

    public static Track_OSM getSelectTrack_osm() {
        return selectTrack_osm;
    }

    public static void setSelectTrack_osm(Track_OSM selectTrack_osm) {
        Global.selectTrack_osm = selectTrack_osm;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setIsPaused(boolean isPaused) {
        Global.isPaused = isPaused;
    }

    public static Animation getToolbarAnimation(Context context){
        return AnimationUtils.loadAnimation(context, R.anim.clockwise);
    }

    public int getPreferenceValue_int(int key, Context context) {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            return Integer.parseInt(pref.getString(context.getResources().getString(key), "0"));
        } catch (Exception e) {
            myLog("ERRORE in getPreferenceValue key[" + context.getResources().getString(key) + "] [" + e.toString() + "]");
            return 0;
        }
    }

    public String getPreferenceValue_String(int key, Context context) {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            return pref.getString(context.getResources().getString(key), "");
        } catch (Exception e) {
            myLog( "ERRORE in getPreferenceValue key[" + context.getResources().getString(key) + "] [" + e.toString() + "]");
            return "";
        }
    }

    public boolean getPreferenceValue_bool(int key, Context context) {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            return pref.getBoolean(context.getResources().getString(key), false);
        } catch (Exception e) {
            myLog( "ERRORE in getPreferenceValue key[" + context.getResources().getString(key) + "] [" + e.toString() + "]");
            return false;
        }
    }

    // Trasformo i secondi in ore:minuti:secondi
    public String formatTime(double x ){

        double h = 0, m = 0, s = 0;
        String txt = "", time="";
        try{
        h = Math.floor(x / 3600);
        m = Math.floor((x - h * 3600) / 60);
        s = Math.floor((x - h * 3600) - m*60);
        time = String.format("%02.0fh:%02.0f':%02.0f''", h, m, s);

        return time;
        }catch(Exception e){
            myLog("Errore in formatTime ["+e.toString()+"]");
            return time;
        }
    }

    public String formatTime(String sec){
        String timeString = "";
        try {
            int totalSecs = Integer.parseInt(sec);
            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;

            timeString = String.format("%02dh:%02d':%02d''", hours, minutes, seconds);

            return timeString;
        }catch(Exception e){
            myLog("Errore in formatTime ["+e.toString()+"]");
            return timeString;
        }
    }

    public String formatLength(String l ){
        String l_out = "";
        try{

        double l_double =  Double.parseDouble(l)/1000;

        l_out = String.format("%.2f", l_double);

        return l_out;
        }catch(Exception e){
            myLog("Errore in formatLength ["+e.toString()+"]");
            return l_out;
        }
    }

    public String formatLength(double l ){
        String l_out = "";
        try{
            l_out = String.format("%.2f", l/1000);

            return l_out;
        }catch(Exception e){
            myLog("Errore in formatLength ["+e.toString()+"]");
            return l_out;
        }
    }
}
