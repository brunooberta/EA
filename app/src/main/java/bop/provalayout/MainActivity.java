package bop.provalayout;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.SphericalUtil;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    MapView map_osm;
    private static double h = 0;   //  altitudine
    private static LatLng POS_ATTUALE = new LatLng(0.0, 0.0);
    private LatLng POS_FOLLOWING = new LatLng(0.0, 0.0);
    private static GeoPoint GP_POS_ATTUALE = new GeoPoint(0.0,0.0), GP_POS_FOLLOWING = new GeoPoint(0.0,0.0);
    private static GeoPoint GP_POS_PRECEDENTE= new GeoPoint(0.0,0.0);
    private static LatLng POS_PRECEDENTE= new LatLng(0.0,0.0);
    private static GeoPoint last_gp_saved_on_db = new GeoPoint(0.0,0.0);
    private static double h_precedente_saved = 0;
    static private boolean isRecording = false; // = true se si è in fase di registrazione (non in pausa)
    static private Chronometer  crono_rec_dlg, // Utilizzato nel dialogo di registrazione principale per misurare la durata della registrazione
                                crono_track_follow_dlg, // Utilizzato nel dialogo di inseguimento traccia
                                crono_wp_follow_dlg; // Utilizzato nel dialogo di inseguimento wp
    private SeekBar seekBar_map = null;
    private ArrayList<Marker> mMarkerList_locate = new ArrayList<Marker>(); // Lista dei Marker indicanti la posizione attuale
    private ImageView img_outOfPath, img_zoomIn, img_zoomOut, img_bell;
    private ImageView img_manageFollow, btn_rec;
    private static boolean  isJustCreated = false, // Inizialmente è FALSE alla prima esecuzione di OnCreate diventa TRUE
                            isJustStarted=false;   // Inizialmente è FALSE alla prima esecuzione di OmStart diventa TRUE
    private static boolean isFollowing = false;
    private static boolean drawisFollowing = false;
    private static String altitude = "?";
    private static long stop_timer=0,
                        showLocation_timer = 0,
                        direction_timer = 0,
                        savePoint_timer = 0,
                        showLayout_timer =0; // Utilizzato per misurare la durata di visualizzazione del lay_function;
    private static int zoom_rotator_index = 0;
    private Bitmap bMap_largeIcon;
    private GPSDatabase myDatabase;
    static private boolean isAcusticAlarmSuppressed = true;
    private MyBroadcastReceiver mMyBroadcastReceiver;
    private TextView tv_trackName_seekbar;
    private double trackLength = 0, deltaH_pos = 0, deltaH_neg = 0, h_max = 0, h_min = 99999, during = 0;
    private LatLng wp_LatLng;
    private String FAKE_TRACK_RECORDING_WP = "-1", // indice traccia fake su cui mi appoggio per i wp della traccia che sto registrando
                   FAKE_TRACK_GENERIC_WP   = "-2"; // indice traccia fake su cui mi appoggio per i wp non legati ad alcuna traccia
    private TextView tv_alt, tv_img_start_stop_follow,tv_wp_img_start_stop_follow;

    private static LatLng old_direction_pos = null;

    private boolean isFirstPointToTrack = false;
    private List<org.osmdroid.views.overlay.Polyline> lst_polyline;
    static private long startTime_for_ringTimer = 0;
    private IMapController mapController;
    private ArrayList<Track_OSM> lst_track_osm; // lista delle tracce visualizzate sulla mappa
    private ArrayList<GeoPoint> lst_geoPoint_rec_live;
    private org.osmdroid.views.overlay.Polyline polyline_rec_live,      // Polyline della traccia che sto registrando
                                                polyline_wp_to_follow;  // Polyline unisce la posizione attuale con il wp selezionato
    private org.osmdroid.views.overlay.Marker curr_position_marker, follow_marker, direction_marker;
    private GeoPoint wp_ToShow = null;

    private double distance_live_rec = 0;
    private boolean is_wp_selected=false;   // indica se ho selezionato o meno un marker di un wp
    private int curr_wp_index = -1;     // indice del marker del wp che sto scorrendo sulla traccia con NEXT/PREV
    private int closestMarker_index = 0;    // indice del punto più vicino a quello tappato sullo schermo
    private org.osmdroid.views.overlay.Marker selected_marker_wp = null; // marker del wp che ho selezionato con il pulsante
    private EA_Marker current_marker_wp = null; // marker del wp che si andrà  and aggiungere o alla traccia selezionata  o alla traccia generica
    private ArrayList<EA_Marker> fake_track_generic_marker_wp = new ArrayList<>(); // Lista dei marker dei WP non legati ad alcuna traccia
    private Global gbl = new Global();
    private Handler myHandler = new Handler();
    private MyRunnable myRunnable = new MyRunnable();
    private boolean isDirectionDrawed = false; // Mi dice se ho già disegnato il layer con i directionmarker sulla traccia selezioanta
    private Toolbar toolbar;
    private TextView tv_offline_mode;
    private static double h_max_live = 0, h_min_live = 99999, d_plus_live = 0, d_minus_live = 0;
    private ImageView img_addMarker;
    private ImageView img_select_wp;
    private double d_track_following = 0, d_wp_following = 0;
    private static long start_track_following_time = 0, start_wp_following_time=0;
    private int animation_length_ms = 0,  // durata animazione in ms
                threshold_track_select_m = 0, // soglia entro la quale seleziono la traccia vicina al click sullo schermo
                threshold_zoom_show_markers_direction = 0; // soglia di zoom oltre la quale mostro i marker di direzione sulla traccia
    private NotificationManager mNotifyMgr;

    private static ArrayList<Double> lst_h = new ArrayList<>(); // Buffer circolare che utilizzo per calcolare l'altezza media
    private static int index_h = 0;                             // Indice di lst_h
    private static long refresh_altitude_timer = 0;             // Timer per forzare il refresh della altitudine sulla GUI

    private ImageView img_select_track_mode;
    private ImageView img_manage_lock_selected_track;
    private int cnt_ActionMove = 0;
    private int screen_width =0;
    private Typeface iconFont;
    private Dialog dlg_rec = null,  dlg_foll = null;

    private long T_STOP = 0; // tempo limite in minuti di attività nella versione FREE

    private static int recMarkerId = 0;

    private GoogleApiClient mApiClient;
    private PendingIntent pendingIntent;
    private static boolean isMoving = false;

    //Riceve i messaggi dal Servizio di localizzazione LocationService
    public class MyBroadcastReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Location mLocation = intent.getParcelableExtra("LOCATION");
                myOnLocationChanged(mLocation);

                //gbl.myLog( "MyBroadcastReceiver.onReceive");

            } catch (Exception e) {
                gbl.myLog( "ERRORE in onReceive [" + e.toString() + "]");
            }
        }

    }

    private void initializeMap(){

        map_osm = (MapView) findViewById(R.id.mapView_osm);
        map_osm.setTileProvider(gbl.getTileProviderArray());
        map_osm.setMultiTouchControls(true);
        map_osm.setTilesScaledToDpi(true);
        map_osm.setMinZoomLevel(gbl.pref_map_offline_zoom_min);
        map_osm.setMaxZoomLevel(gbl.pref_map_offline_zoom_max);

        map_osm.setMapOrientation(gbl.getMapOrientation());
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map_osm);
        mRotationGestureOverlay.setEnabled(true);

        map_osm.getOverlays().add(mRotationGestureOverlay);
        CompassOverlay mCompassOverlay = new CompassOverlay(this,new InternalCompassOrientationProvider(this), map_osm);
        mCompassOverlay.enableCompass();
        mCompassOverlay.setEnabled(true);
        map_osm.getOverlays().add(mCompassOverlay);

        // gestione della scala
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map_osm);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setAlignRight(true);
        mScaleBarOverlay.setTextSize(35);
        mScaleBarOverlay.setUnitsOfMeasure(ScaleBarOverlay.UnitsOfMeasure.metric);
        mScaleBarOverlay.setEnabled(true);
        map_osm.getOverlays().add(mScaleBarOverlay);

        map_osm.invalidate();
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            isJustStarted = true;

        } catch (Exception e) {
            gbl.myLog("ERRORE in onStart [" + e.toString() + "]");
        }
    }

    @Override
    protected void onDestroy() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            //20170805 MODIFICA per mantenere attiva la registrazione al RESTART manageTrackRecording("STOP");
            myDatabase.close();
            mNotifyMgr.cancelAll();

            map_osm = null;

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMyBroadcastReceiver);

            EA_Logger.close();

            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mApiClient,pendingIntent);

            super.onDestroy();

        } catch (Exception e) {
            gbl.myLog("ERRORE in onDestroy [" + e.toString() + "]");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(gbl.APP_FREE){
            T_STOP = 20;
        }else{
            T_STOP = 0;
        }

        try {

            setContentView(R.layout.activity_main);

            Intent intent = new Intent( this, ActivityRecognizedService.class );
            pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mApiClient.connect();

            if(!isJustCreated){
                gbl.setIsSelectMode_ON(gbl.pref_def_select_track_mode);
                gbl.setIsCentertMode_ON(gbl.pref_def_auto_center_mode);
                gbl.setPref_following_sound_out_of_path(gbl.pref_following_sound_out_of_path);
            }
            settingValues();

            iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);

            toolbar = (Toolbar) findViewById(R.id.tb_activy_main);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }

            showNotification();

            setVisbilityLayoutFunction(false);

            // Se provengo dalla lista dei WP e ne ho selezionato 1 --> lo setto e in fase di zoom lo mostro
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                String lat = extras.getString("lat");
                String lon = extras.getString("lon");
                if (lat != null && lon != null) {
                    wp_ToShow = new GeoPoint(Double.valueOf(lat), Double.valueOf(lon));
                }
                else {
                    wp_ToShow = null;
                }

            } else {
                wp_ToShow = null;
            }

            myDatabase = new GPSDatabase(this);
            myDatabase.open();

            if(!gbl.APP_FREE)
                isRecording = get_StartTracking(); // Verifico se è in corso una registrazione
            else
                isRecording = false;

            if(isRecording)  {
                isFirstPointToTrack = true;
                gbl.setRecordingTime(getStartDateOfRec()); // recupero il primo istante campionato
            }

            tv_offline_mode = (TextView) findViewById(R.id.tv_offline_mode);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
               showGPSDisabledAlertToUser();
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Context ctx = getApplicationContext();
            //important! set your user agent to prevent getting banned from the osm servers
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

            MapTileProviderArray tileProviderArray = getMapTileProviderArray();
            gbl.setTileProviderArray(tileProviderArray);

            initializeMap();

            mMyBroadcastReceiver = new MyBroadcastReceiver();
            IntentFilter i_f = new IntentFilter();
            i_f.addAction("intLocationChanged");
            // Elimino la registrazione del MyBroadcastReceiver della precedente sessione
            if (gbl.getmMyBroadcastReceiver()!=null){
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(gbl.getmMyBroadcastReceiver());
            }
            gbl.setmMyBroadcastReceiver(mMyBroadcastReceiver);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMyBroadcastReceiver, i_f);

            curr_position_marker = new org.osmdroid.views.overlay.Marker(map_osm);
            follow_marker = new org.osmdroid.views.overlay.Marker(map_osm);
            direction_marker= new org.osmdroid.views.overlay.Marker(map_osm);

            GeoPoint startPoint = new GeoPoint(45.208456, 7.137358);
            mapController = map_osm.getController();
            mapController.setZoom(gbl.pref_default_zoom);
            mapController.setCenter(startPoint);

            tv_trackName_seekbar = (TextView) findViewById(R.id.tv_trackName);
            tv_trackName_seekbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map_osm.zoomToBoundingBox(gbl.getSelectTrack_osm().boundingBox,true);
                }
            });

            tv_alt = (TextView) findViewById(R.id.tv_alt);
            tv_alt.setText(getString(R.string.str_load));

            seekBar_map = (SeekBar) findViewById(R.id.seekBar_map);

            ImageView img_info_track = (ImageView) findViewById(R.id.img_info_track);
            img_info_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDlg_INFO_TRACK();
                }
            });

            img_manage_lock_selected_track = (ImageView) findViewById(R.id.img_manage_lock_selected_track);
            if(gbl.isSelectTrackLocked())
                img_manage_lock_selected_track.setImageResource(R.mipmap.lock_track);
            else
                img_manage_lock_selected_track.setImageResource(R.mipmap.unlock_track);

            img_manage_lock_selected_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gbl.isSelectTrackLocked()){
                        gbl.setIsSelectTrackLocked(false);
                        img_manage_lock_selected_track.setImageResource(R.mipmap.unlock_track);
                    }
                    else{
                        gbl.setIsSelectTrackLocked(true);
                        img_manage_lock_selected_track.setImageResource(R.mipmap.lock_track);
                    }
                }
            });

            if (isJustCreated) {
                resetFollowingTrack();
            }

            btn_rec = (ImageView) findViewById(R.id.btn_rec);
            if (isRecording)
                btn_rec.setImageResource(R.drawable.rec_rec);
            if(gbl.isPaused())
                btn_rec.setImageResource(R.drawable.rec_pause);

            btn_rec.setOnClickListener(new ImageView.OnClickListener() {
                public void onClick(View v) {
                    String str_tv_alt = tv_alt.getText().toString();
                    if(str_tv_alt.equals(getString(R.string.str_load))){
                        String txt = getString(R.string.load_in_progess);
                        Toast.makeText(getApplicationContext(),txt, Toast.LENGTH_LONG).show();
                    }else
                        showDlg_REC();
                }
            });

            img_outOfPath = (ImageView) findViewById(R.id.img_outOfPath);
            img_outOfPath.setVisibility(ImageView.INVISIBLE);

            img_addMarker = (ImageView) findViewById(R.id.img_add_wp);
            img_addMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addMarkerOnMap();
                }
            });

            ImageView img_next_wp = (ImageView) findViewById(R.id.img_next_wp);
            img_next_wp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show_wp("NEXT");
                }
            });


            final ImageView img_prev_wp = (ImageView) findViewById(R.id.img_prev_wp);
            img_prev_wp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show_wp("PREV");
                }
            });

            img_select_track_mode = (ImageView) findViewById(R.id.img_select_track_mode);

            if(gbl.isSelectMode_ON())
                img_select_track_mode.setImageResource(R.mipmap.selectmode_on);
            else
                img_select_track_mode.setImageResource(R.mipmap.selectmode_off);

            img_select_track_mode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(gbl.isSelectMode_ON()) {

                        gbl.setIsSelectTrackLocked(false);
                        img_manage_lock_selected_track.setImageResource(R.mipmap.unlock_track);

                        if(!isFollowing) {
                            showMarkerDirectionOnTrack(true);
                            img_select_track_mode.setImageResource(R.mipmap.selectmode_off);
                            gbl.setIsSelectMode_ON(false);
                            gbl.setSelectTrack_osm(null);
                            showSelectTrackLayout(false, 0);
                        }else{
                            Dlg_Confirm("FOLLOWING_IN_PROGRESS",null);
                        }
                    }
                    else {
                        img_select_track_mode.setImageResource(R.mipmap.selectmode_on);
                        gbl.setIsSelectMode_ON(true);

                    }


                }
            });

            final ImageView img_center_mode = (ImageView) findViewById(R.id.img_center_mode);
            if(gbl.isCentertMode_ON())
                img_center_mode.setImageResource(R.mipmap.center_on);

            img_center_mode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(gbl.isCentertMode_ON()) {
                        img_center_mode.setImageResource(R.mipmap.center_off);
                        gbl.setIsCentertMode_ON(false);
                    }
                    else {
                        img_center_mode.setImageResource(R.mipmap.center_on);
                        gbl.setIsCentertMode_ON(true);
                    }


                }
            });

            img_select_wp = (ImageView) findViewById(R.id.img_select_wp);
            img_select_wp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(selected_marker_wp == null && current_marker_wp==null)
                        Dlg_Confirm("NO_WP_SELECTED",null);
                    else
                        showDlg_WP_FOLLOW();

                }
            });

            img_bell = (ImageView) findViewById(R.id.img_bell);
            img_bell.setVisibility(ImageView.INVISIBLE);
            img_bell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isAcusticAlarmSuppressed) {
                        isAcusticAlarmSuppressed = false;
                        img_bell.setImageResource(R.mipmap.bell);
                        Toast.makeText(getApplicationContext(), getString(R.string.alarm_on), Toast.LENGTH_LONG).show();
                    }
                    else{
                        isAcusticAlarmSuppressed = true;
                        img_bell.setImageResource(R.mipmap.bell_slash);
                        Toast.makeText(getApplicationContext(),getString(R.string.alarm_off), Toast.LENGTH_LONG).show();
                    }
                }
            });

            img_manageFollow = (ImageView) findViewById(R.id.img_manageFollow);
            img_manageFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                if (gbl.getSelectTrack_osm() != null) {
                    showDlg_TRACK_FOLLOW();

                } else {
                    Dlg_Confirm("NO_TRACK_SELECTED", null);
                }

                }
            });

            if (isFollowing) img_manageFollow.setImageResource(R.mipmap.follow_red);

            img_zoomIn = (ImageView) findViewById(R.id.img_zoomIn);
            img_zoomIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mapController.zoomIn();
                    } catch (Exception e) {
                        gbl.myLog( "ERRORE in img_zoomIn.setOnClickListener [" + e.toString() + "]");
                    }
                }
            });

            img_zoomOut = (ImageView) findViewById(R.id.img_zoomOut);
            img_zoomOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mapController.zoomOut();
                    } catch (Exception e) {
                        gbl.myLog( "ERRORE in img_zoomIn.setOnClickListener [" + e.toString() + "]");
                    }
                }
            });

            if(gbl.getTrackOsmCollection()==null){

                gbl.setTrackOsmCollection( new Track_OSM_Collection(myDatabase,map_osm,getApplicationContext()));
            }

            drawAllTracks_OSM();

            // gestione della traccia/punto visualizzato in partenza
            showSelectTrackLayout(false, 0);

            if (wp_ToShow == null) {
                if(isRecording)

                    zoomOnPoint(GP_POS_ATTUALE);
                else if (gbl.getSelectTrack_osm() != null) {
                    /*Per qualche motivo che non ho capito se nella onCreate faccio zoomBoundingBox una sola volta non funziona
                    * Probabilmente è un problema sul mapcontroller ma non ho approfondito */
                    zoomBoundingBox(gbl.getSelectTrack_osm());
                    zoomBoundingBox(gbl.getSelectTrack_osm());
                    showSelectTrackLayout(true, 0);
                } else {
                    zoomTrack(0);
                }

            }else{
                zoomOnPoint(wp_ToShow);
                wp_ToShow = null;
            }

            seekBar_map.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    map_osm.getOverlays().removeAll(gbl.getSelectTrack_osm().lst_marker);
                    map_osm.getOverlays().add(gbl.getSelectTrack_osm().lst_marker.get(progress));
                    gbl.getSelectTrack_osm().lst_marker.get(progress).showInfoWindow();
                    map_osm.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            map_osm.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                try {
                    myMapOnTouchListener(event);
                    return false;
                }
                catch (Exception e) {
                    gbl.myLog( "ERRORE in onTouch [" + e.toString() + "]");
                    return false;
                }
                }
            });

            map_osm.setMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    showMarkerDirectionOnTrack(false);
                    return false;
                }
            });

            isJustCreated = true;

            if (gbl.isLocked()){
                showDlg_REC();
            }
        }
        catch (Exception e) {
            gbl.myLog( "ERRORE in onCreate [" + e.toString() + "]");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main_map, menu);

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
            case R.id.menu_item_go_to_tracklist:
                if (!isRecording && !isFollowing && !is_wp_selected) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMyBroadcastReceiver);
                    goToTrackList();
                }
                else {
                    Dlg_Confirm("ACTIVITY_IN_PROGRESS", null);
                }
                return true;
            case R.id.menu_item_recycle_track:
                if(lst_track_osm!=null){
                    int num_of_tracks_on_map = lst_track_osm.size();
                    if(num_of_tracks_on_map>0){
                        zoomTrack(zoom_rotator_index++);
                    }

                    if (zoom_rotator_index > num_of_tracks_on_map - 1)
                        zoom_rotator_index = 0;
                }

                return true;
            case R.id.menu_item_go_to_wp_list:
                if (!isFollowing && !is_wp_selected) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMyBroadcastReceiver);
                    goWPList();
                }
                else
                    Dlg_Confirm("ACTIVITY_IN_PROGRESS", null);
                return true;
            case R.id.menu_item_get_position:
                showCurrentPosition(true);
                return true;
            case R.id.menu_item_settings:
                if (!isRecording && !isFollowing && !is_wp_selected) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMyBroadcastReceiver);
                    goToSettings();
                }
                else {
                    Dlg_Confirm("ACTIVITY_IN_PROGRESS", null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Attenzione: Permesso di Accesso alla posizione GPS NEGATO!", Toast.LENGTH_LONG);
                }
                return;
            }

        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 0, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    private long getStartDateOfRec(){

        int TIME=4;
        long startDate_sec =0;
        //Prendo i dati da Location
        Cursor cur = myDatabase.choiceData("location","", new String[]{});
        cur.moveToFirst();
        if(cur.getCount()>0)
            startDate_sec = cur.getLong(TIME)*1000;

        cur.close();
        return startDate_sec;
    }
    private void showMarkerDirectionOnTrack(boolean isForcedToHide){
        if(!isForcedToHide) {
            if (map_osm.getZoomLevel() > threshold_zoom_show_markers_direction) {
                if (gbl.getSelectTrack_osm() != null && !isDirectionDrawed) {

                    map_osm.getOverlays().addAll(gbl.getSelectTrack_osm().getLst_direction_marker());
                    isDirectionDrawed = true;
                }

            } else {
                if (gbl.getSelectTrack_osm() != null) {
                    map_osm.getOverlays().removeAll(gbl.getSelectTrack_osm().getLst_direction_marker());
                    isDirectionDrawed = false;
                }

            }
        }else {
            if (gbl.getSelectTrack_osm()!=null) {
                map_osm.getOverlays().removeAll(gbl.getSelectTrack_osm().getLst_direction_marker());
                isDirectionDrawed = false;
            }
        }
    }

    private void show_wp(String operation){
        ArrayList<EA_Marker> wp_list = new ArrayList<>();
        try {
            if (gbl.getSelectTrack_osm() != null) {
                wp_list.add(gbl.getSelectTrack_osm().startMarker);
                wp_list.addAll(gbl.getSelectTrack_osm().lst_waypoints_marker);
                wp_list.add(gbl.getSelectTrack_osm().endMarker);
            }
            else
                wp_list = fake_track_generic_marker_wp;

            int wp_list_size = wp_list.size();

            if (wp_list_size > 0) {
                switch (operation) {
                    case "NEXT":
                        curr_wp_index++;
                        if (curr_wp_index == wp_list_size)
                            curr_wp_index = 0;
                        break;
                    case "PREV":
                        curr_wp_index--;
                        if (curr_wp_index < 0)
                            curr_wp_index = wp_list_size - 1;
                        break;
                }
                wp_list.get(curr_wp_index).showInfoWindow();

                current_marker_wp =  wp_list.get(curr_wp_index);

                mapController.setCenter(current_marker_wp.getPosition());
            }

        }catch (Exception e) {
            gbl.myLog( "ERRORE in show_wp [" + e.toString() + "]");
        }
    }

    /**
     * Gestione dell'aggiunta dwi WP nei casi:
     * 1. traccia in fase di registrazione --> il WP viene aggiunto alla traccia
     * 2. nessuna traccia selezionata --> il wp viene aggiunto al centro dello schermo
     * 3. una traccia selezionata --> il WP viene aggiunto alla traccia sul marker selezionato dalla seekbar
     */
    private void addMarkerOnMap(){

        try {
            // se sono in fase di registrazione traccia WP da aggiungere è la posizione attuale
            GeoPoint gp = convert_LatLng_to_GeoPoint(POS_ATTUALE);
            if (isRecording) {
                wp_LatLng = convert_GeoPoint_to_LatLng(gp);
            } else {
                if (gbl.getSelectTrack_osm() == null) {
                    Projection proj = map_osm.getProjection();
                    int y = map_osm.getHeight() / 2;
                    int x = map_osm.getWidth() / 2;
                    gp = (GeoPoint) proj.fromPixels(x, y);
                    current_marker_wp = new EA_Marker(map_osm,recMarkerId++);
                    current_marker_wp.setPosition(gp);

                    wp_LatLng = convert_GeoPoint_to_LatLng(gp);
                } else {
                    gp = gbl.getSelectTrack_osm().lst_marker.get(closestMarker_index).getPosition();
                    wp_LatLng = convert_GeoPoint_to_LatLng(gp);

                    current_marker_wp = new EA_Marker(map_osm,recMarkerId++);
                    current_marker_wp.setPosition(gp);
                    current_marker_wp.setSnippet(gbl.getSelectTrack_osm().lst_marker.get(closestMarker_index).getSnippet());

                }

                current_marker_wp.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
                       selected_marker_wp = current_marker_wp;
                        selected_marker_wp.showInfoWindow();
                        return false;
                    }
                });

            }

            //Dlg_Confirm("INS_WAYPOINT", null);
            showDlg_ADD_WP();
        }
        catch (Exception e) {
            gbl.myLog( "ERRORE in addMarkerOnMap  [" + e.toString() + "]");
        }

    }

    private void setVisbilityLayoutFunction(boolean isVisible){
        try {
            final ConstraintLayout layout_function = (ConstraintLayout) findViewById(R.id.main_function);
            ConstraintLayout content_main = (ConstraintLayout) findViewById(R.id.mainConstraintLayout);
            final ImageView img_center = (ImageView)content_main.findViewById(R.id.centerScreen);

            if (isVisible) {
                img_center.animate().alpha(1).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).start();
                img_center.setVisibility(ConstraintLayout.VISIBLE);
                layout_function.animate().alpha(1).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).start();
                layout_function.setVisibility(ConstraintLayout.VISIBLE);
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    img_center.animate().alpha(0).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            img_center.setVisibility(ConstraintLayout.INVISIBLE);
                        }
                    }).start();

                    layout_function.animate().alpha(0).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            layout_function.setVisibility(ConstraintLayout.INVISIBLE);
                        }
                    }).start();
                }

                //;
            }
        }catch(Exception e){gbl.myLog( "ERRORE in setVisbilityLayoutFunction  [" + e.toString() + "]");}

    }

    private org.osmdroid.views.overlay.Marker getClosestMarker(MotionEvent ev){
        double d = 999999999;
        org.osmdroid.views.overlay.Marker closest_marker = null;
        Projection proj = map_osm.getProjection();
        GeoPoint loc = (GeoPoint) proj.fromPixels((int) ev.getX(), (int) ev.getY()); // punto della mappa che ho toccato

        int index = 0;
        closestMarker_index = 0;
        for (Track_OSM t : lst_track_osm) {
            // Se sono in fase di FOLLOWING la ricerca del WP più vicino è limitata alla traccia che sto seguendo
            if (isFollowing) {
                if (t.getTrackId() != gbl.getSelectTrack_osm().getTrackId())
                    break;
            }

            index = 0;
            map_osm.getOverlays().removeAll(t.lst_marker);
            for (org.osmdroid.views.overlay.Marker m : t.lst_marker) {

                double cur_d = loc.distanceTo(m.getPosition()); // distanza tra marker della traccia e punto che ho toccato
                if (cur_d < d) {
                    gbl.setSelectTrack_osm(t); // Se sto seguendo una traccia la selezione della traccia non viene cambiata
                    d = cur_d;
                    closest_marker = m;
                    closestMarker_index = index;
                }
                index++;
            }
        }

        return closest_marker;
    }

    private boolean myMapOnTouchListener(MotionEvent ev) {
        try {
            int actionType = ev.getAction();

            double d = 999999999;
            org.osmdroid.views.overlay.Marker near_marker = null;

            switch (actionType) {
                case MotionEvent.ACTION_MOVE:
                    cnt_ActionMove++;
                    break;
                case MotionEvent.ACTION_DOWN:
                    cnt_ActionMove = 0;
                    myHandler.removeCallbacks(myRunnable);
                    setVisbilityLayoutFunction(true);
                    myHandler.postDelayed(myRunnable,10000);
                    break;
                case MotionEvent.ACTION_UP:
                    if ( gbl.getMapOrientation() != map_osm.getMapOrientation()) {
                        isDirectionDrawed=false;
                        showMarkerDirectionOnTrack(false);
                        gbl.setMapOrientation(map_osm.getMapOrientation());
                        //2017.10.10 - START  ID = 3
                        addMarkerForMarchDirection();
                        if (img_outOfPath.getVisibility() == ImageView.VISIBLE)
                            showFollowMarker(POS_ATTUALE, POS_FOLLOWING, getString(R.string.current_location), get_distance_from_track());
                        //2017.10.10 - END  ID = 3
                    }

                    // Quando effettuo una GESTURE --> esco e non cambio le selezioni di tracce o wp
                    if(cnt_ActionMove > 6) {
                        break;
                    }

                    if(gbl.isSelectMode_ON() && !gbl.isSelectTrackLocked()) {

                        near_marker = getClosestMarker(ev);

                        if (near_marker != null ) {
                            map_osm.getOverlays().add(near_marker);
                            near_marker.showInfoWindow();
                            //map_osm.invalidate();

                            showSelectTrackLayout(true, closestMarker_index, near_marker.getPosition());
                        } else {
                            // Se sto seguendo una traccia non devo eliminare la selezione della traccia
                            if (!isFollowing) {
                                gbl.setSelectTrack_osm(null);
                                showSelectTrackLayout(false, closestMarker_index);
                            }
                        }

                        showMarkerDirectionOnTrack(false);
                    }
                    break;
            }
            return false;

        }catch (Exception e) {
            gbl.myLog( "ERRORE in myMapOnTouchListener [" + e.toString() + "]");
            return super.dispatchTouchEvent(ev);
        }
    }

    // Visualizza sulla mappa il smarker della posizione attuale
    private void showCurrentPosition(boolean moveCamera) {
        try {

            map_osm.getOverlays().remove(follow_marker);
            map_osm.getOverlays().remove(curr_position_marker);

            if(moveCamera) {
                zoomOnPoint(GP_POS_ATTUALE);
            }

            curr_position_marker.setTitle("");

            if(is_wp_selected){
                String title="";
                float d = GP_POS_ATTUALE.distanceTo(selected_marker_wp.getPosition());
                d_wp_following = d;
                if (d>=threshold_track_select_m) {
                    d = d / 1000;
                    title = String.format("D=%.2fkm", d );
                }else{
                    title = String.format("D=%.0fm", d );
                }
                curr_position_marker.setTitle(title);

            }else
                curr_position_marker.setTitle("");

            curr_position_marker.setPosition(GP_POS_ATTUALE);
            curr_position_marker.setIcon(ResourcesCompat.getDrawable(this.getResources(),R.mipmap.current_location_marker,null));

            if(curr_position_marker.getTitle().length()==0)
                curr_position_marker.closeInfoWindow();
            else
                curr_position_marker.showInfoWindow();

            map_osm.getOverlays().add(curr_position_marker);

        } catch (Exception e) {
            gbl.myLog( "ERRORE in showCurrentPosition [" + e.toString() + "]");
        }
    }

    private void goToTrackList() {
        try {
            showDlg_SELECT_TRACK();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in goToTrackList [" + e.toString() + "]");
        }
    }

    //Resetto polylineId e toFollow da tutte le tracce
    private void resetFollowingTrack() {
        try {

            myDatabase.upd_TrackSaved_Rows(new String[]{"polylineId", "toFollow"}, new String[]{"", "0"}, "", new String[]{});

        } catch (Exception e) {
            gbl.myLog( "ERRORE in resetFollowingTrack [" + e.toString() + "]");
        }
    }

    private void setTrackToFollow_OSM(String trackId, String valore) {
        try {

            myDatabase.set_trackToFollow_OSM(trackId, valore);

        } catch (Exception e) {
            gbl.myLog( "ERRORE in setTrackToFollow_OSM [" + e.toString() + "]");
        }
    }

    //Recupera se sul DB il flag di REC traccia
    private boolean get_StartTracking() {
        boolean retValue = false;

        try {
            Cursor cur = null;
            cur = myDatabase.choiceData("trackInfo", "name=? and value=?", new String[]{"start tracking", "1"});
            cur.moveToFirst();

            if (cur.getCount() > 0)
                retValue = true;

            cur.close();

            return retValue;


        } catch (Exception e) {
            gbl.myLog( "ERRORE in set_StartTracking [" + e.toString() + "]");
            return retValue;
        }
    }

    //Aggiorna sul DB il flag di REC traccia
    public void manageTrackRecording(String value) {
        try {

            String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            switch (value){
                case "START":
                    myDatabase.manage_TrackInfo_Rows("START_DATE", currentDateandTime);
                    isFirstPointToTrack = true;
                    myDatabase.manage_TrackInfo_Rows("start tracking", "1");
                    break;
                case "STOP":
                    myDatabase.manage_TrackInfo_Rows("END_DATE",currentDateandTime);
                    myDatabase.manage_TrackInfo_Rows("start tracking", "0");
                    isFirstPointToTrack = false;
                    break;
            }

        } catch (Exception e) {
            gbl.myLog( "ERRORE in manageTrackRecording [" + e.toString() + "]");
        }

    }

    //Consente di disegnare la traccia sulla mappa OSM
    private void drawTrackOnMap_OSM(Track_OSM track_osm) {

        try {

            org.osmdroid.views.overlay.Polyline polyLine = track_osm.polyline;
            polyLine.setColor(Color.BLACK);

            lst_polyline.add(polyLine);
            lst_polyline.add(polyLine);
            map_osm.getOverlays().add(polyLine);
            map_osm.getOverlays().add(track_osm.startMarker);
            map_osm.getOverlays().add(track_osm.endMarker);
            map_osm.getOverlays().addAll(track_osm.lst_waypoints_marker);

            for(final org.osmdroid.views.overlay.Marker m:track_osm.lst_waypoints_marker){

                m.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
                        selected_marker_wp = m;
                        selected_marker_wp.showInfoWindow();
                        return false;
                    }
                });
            }

        } catch (Exception e) {
            gbl.myLog( "ERRORE in drawTrackOnMap_OSM [" + e.toString() + "]");

        }
    }

    //Consente di disegnare la traccia sulla mappa OSM
    private Track_OSM drawTrackOnMap_OSM(String trackId, int trackColor) {
        Track_OSM track_osm = new Track_OSM(map_osm,trackId, myDatabase, getBaseContext() );
        try {

            org.osmdroid.views.overlay.Polyline polyLine = track_osm.polyline;
            polyLine.setColor(Color.BLACK);

            lst_polyline.add(polyLine);
            lst_polyline.add(polyLine);
            map_osm.getOverlays().add(polyLine);
            map_osm.getOverlays().add(track_osm.startMarker);
            map_osm.getOverlays().add(track_osm.endMarker);
            map_osm.getOverlays().addAll(track_osm.lst_waypoints_marker);

            for(final org.osmdroid.views.overlay.Marker m:track_osm.lst_waypoints_marker){

                m.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
                        selected_marker_wp = m;
                        selected_marker_wp.showInfoWindow();
                        return false;
                    }
                });
            }

            return track_osm;

        } catch (Exception e) {
            gbl.myLog( "ERRORE in drawTrackOnMap_OSM [" + e.toString() + "]");
            return track_osm;

        }
    }

    private void zoomBoundingBox(Track_OSM t){
        if (t!=null) {
            BoundingBox b = t.boundingBox;
            mapController.setCenter(b.getCenter());
            mapController.zoomToSpan(b.getLatitudeSpan(), b.getLongitudeSpan());
        }
    }

    private void zoomTrack(Track_OSM t) {
        try {
            zoomBoundingBox(t);
        } catch (Exception e) {
            gbl.myLog( "ERRORE in zoomTrack [" + e.toString() + "]");

        }
    }

    private void zoomOnPoint(GeoPoint gp){
gbl.myLog1("gbl.pref_default_zoom["+gbl.pref_default_zoom+"]");
        mapController.setZoom(gbl.pref_default_zoom);
        mapController.setCenter(gp);
    }

    private void zoomTrack(int id) {
    try {
        if(lst_track_osm != null) {
            if(lst_track_osm.size() > 0) {
                Track_OSM t = lst_track_osm.get(id);
                if(t!=null) {
                    t.startMarker.showInfoWindow();
                    zoomBoundingBox(t);
                } else{
                    mapController.setCenter(GP_POS_ATTUALE);
                    mapController.setZoom(gbl.pref_default_zoom);
                }
            }
            else{// Se non ci sono tracce salvate e non ho ancora acquisito nessuna posizione --> imposto una posizione di default
                double lat = 45.05, lon  = 7.666667;
                GeoPoint def_gp = new GeoPoint(lat,lon);
                mapController.setZoom(gbl.pref_default_zoom);
                mapController.setCenter(def_gp);}
        }
        else{
            // Se non ci sono tracce salvate e non ho ancora acquisito nessuna posizione --> imposto una posizione di default
            double lat = 45.05, lon  = 7.666667;
            GeoPoint def_gp = new GeoPoint(lat,lon);
            mapController.setZoom(gbl.pref_default_zoom);
            mapController.setCenter(def_gp);
        }
    }
    catch (Exception e) {
        gbl.myLog( "ERRORE in zoomTrack [" + e.toString() + "]");

    }

    }


    private void drawTrackOnMap_Live(GeoPoint gp) {
        try {

            if (map_osm != null) {
                map_osm.getOverlays().remove(polyline_rec_live);
                polyline_rec_live.setColor(Color.RED);
                lst_geoPoint_rec_live.add(gp);
                polyline_rec_live.setPoints(lst_geoPoint_rec_live);
                map_osm.getOverlays().add(polyline_rec_live);
                map_osm.invalidate();

            }
        } catch (Exception e) {
            gbl.myLog( "ERRORE in drawTrackOnMap_Live [" + e.toString() + "]");

        }
    }

    //Salva la traccia sul DB
    private int SaveTrackOnDB(String trackName) {
        try {
            // segnalo che ho finito la registrazione della traccia
            manageTrackRecording("STOP");
            //Calcolo dati della traccia da salvare
            get_track_data();
            int trackId = myDatabase.ins_TrackSaved_Rows(trackName, trackLength, h_max, h_min, deltaH_pos, deltaH_neg, during);
            myDatabase.del_Location_AllRows();

            gbl.getTrackOsmCollection().addTrackToCollection(""+trackId);

            return trackId;

        }
        catch (Exception e) {
            gbl.myLog( "ERRORE in SaveTrackOnDB [" + e.toString() + "]");
            return 0;
        }

    }

    //Rimuove l'attuale traccia dal DB
    private void delTrackOnMemory() {
        myDatabase.del_Location_AllRows();
    }

    // Nella versione FREE gestisce il tempo di attività delle varie funzionalità: trascorso il T_soglia le stoppa
    private boolean isTimeToStop(long T_soglia_ms) {

        if (T_soglia_ms==0) return false;

        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;
        if(isRecording || isFollowing) {

            if (stop_timer == 0) stop_timer = now;

            else delta = (long) (now - stop_timer);

            if (delta > T_soglia_ms) {
                stop_timer = 0;
                ret = true;
            }
        }
        else stop_timer = 0;

        return ret;
    }

    // Segnala se dalla sua prima chiamata valuta se è trascorso il T_soglia
    private boolean isTimeToShowLocation(long T_soglia_ms) {
        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;

        if (showLocation_timer == 0) showLocation_timer = now;

        else delta = (long) (now - showLocation_timer);

        if (delta > T_soglia_ms) {
            showLocation_timer = 0;
            ret = true;
        }

        return ret;
    }

    // Segnala se è passato più di T_soglia_ms dal momento che ho aggiornato l'ultima volta l'altezza
    private boolean isTimeToRefreshAltitude(long T_soglia_ms) {
        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;

        if (refresh_altitude_timer == 0) refresh_altitude_timer = now;

        else delta = (long) (now - refresh_altitude_timer);

        if (delta > T_soglia_ms) {
            refresh_altitude_timer = 0;
            ret = true;
        }

        //if(ret) gbl.myLog("isTimeToRefreshAltitude --> AGGIORNAMENTO FORZATO ALTITUDINE ret["+ret+"]");

        return ret;
    }

    // Segnala se dalla sua prima chiamata valuta se è trascorso il T_soglia
    private boolean isTimeToShowLayoutFunction(long T_soglia_ms) {
        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;

        if (showLayout_timer == 0) showLayout_timer = now;

        else delta = (long) (now - showLayout_timer);

        if (delta > T_soglia_ms) {
            showLayout_timer = 0;
            ret = true;
        }

        return ret;
    }

    // Segnala se dalla sua prima chiamata valuta se è trascorso il T_soglia
    private boolean isTimeToShowDirection(long T_soglia_ms) {
        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;

        if (direction_timer == 0) direction_timer = now;

        else delta = (long) (now - direction_timer);

        if (delta > T_soglia_ms) {
            direction_timer = 0;
            ret = true;
        }

        return ret;
    }

    // Segnala se dalla sua prima chiamata valuta se è trascorso il T_soglia
    private boolean isTimeToSavePointToDB(long T_soglia_ms) {
        boolean ret = false;
        long now = System.currentTimeMillis(), delta = 0;

        if (savePoint_timer == 0) savePoint_timer = now;

        else delta = (long) (now - savePoint_timer);

        if (delta > T_soglia_ms) {
            savePoint_timer = 0;
            ret = true;
        }

        return ret;
    }

    // Gestione del Marker relativo alla posizione attuale
    private void setCurrentPositionOnMap_OSM(int t_soglia_ms, String snippet) {
        try {
            if (isTimeToShowLocation(t_soglia_ms)) {
                // se sono in fase di following w sono fuori percorso rendo visibile la freccia rossa
                if (img_outOfPath.getVisibility() == ImageView.VISIBLE)
                    showFollowMarker(POS_ATTUALE, POS_FOLLOWING, getString(R.string.current_location), snippet);

                // ogni TOT secondi verifica se è il caso di cambiare la direzione della freccia...
                if(isTimeToShowDirection(gbl.pref_gps_minum_time_for_direction) && isMoving) {
                     addMarkerForMarchDirection();
                }

            }
            // Se non mi sto muovendo elimino il marker
            if (!isMoving)
                map_osm.getOverlays().remove(direction_marker);

        } catch (Exception e) {
            gbl.myLog( "ERRORE in setCurrentPositionOnMap_OSM [" + e.toString() + "]");
        }
    }

    // Determina la lunghezza della traccia che sto registrando
    private void get_track_data() {
        try {
            GeoPoint p_prec = null, p = null;
            double h=0, h_prec=0;
            int LAT = 1, LON = 2, ALT = 3, TIME=4;
            //Prendo i dati da Location
            Cursor cur = myDatabase.choiceData("location","", new String[]{});
            cur.moveToFirst();

            //Resetto le variabili globali
            trackLength=0;
            deltaH_pos=0;
            deltaH_neg=0;
            h_max=0;
            h_min=99999;
            during = 0;
            double duringStart = 0;

            //Prendo i dati relativi alla traccia
            for(int i=0;i<cur.getCount();i++){
                p = new GeoPoint(cur.getDouble(LAT),cur.getDouble(LON),0.0);
                h = cur.getDouble(ALT);

                if (i==0){
                    deltaH_pos = 0;
                    duringStart = cur.getDouble(TIME);
                }
                else{
                    if ( h >= h_prec ){
                        deltaH_pos += h - h_prec;
                    }
                    else{
                        deltaH_neg +=h_prec - h;
                    }
                }

                if (h>h_max) h_max = h;
                if (h<h_min) h_min = h;

                if (i > 0) {
                    trackLength += p_prec.distanceTo(p);
                    // Tengo conto del dislivello con il teorema di pitagora ??? per coerenza con gli altri calcoli per ora lo commento...
                    //double delta_h = Math.abs(h_prec-h);
                    //double delta_d = SphericalUtil.computeDistanceBetween(p_prec, p);
                    //double distance = Math.sqrt(delta_h*delta_h+delta_d*delta_d);
                    //trackLength += distance;
                }

                p_prec = p;
                h_prec = h;

                during = cur.getDouble(TIME)- duringStart;

                cur.moveToNext();
            }

            cur.close();

        } catch (Exception e) {
            gbl.myLog("ERRORE in get_distance_from_track ["+e.toString()+"]");

        }
    }

    // Determina la distanza dalla traccia che sto seguendo quando sono fuori percorso
    private String get_distance_from_track() {
        try {
            String d = "";
            if (isFollowing && POS_ATTUALE != null) {
                d = String.format("D=%.0fm", getMinumDistancePoint(POS_ATTUALE));
            }
            return d;
        } catch (Exception e) {
            gbl.myLog("ERRORE in get_distance_from_track ["+e.toString()+"]");
            return "";
        }
    }

    private boolean isCorrectAltitude(double h){
        /*
        Ritengo che l'altezza sia corretta se:
            1. h > 0
            2. Assumo che andando a piedi non sia possibile procedere con un dislivello > 5m al secondo (che è la mia unità di campinamento della posizione)-->
               --> dico che se in un secondo il dislivello percorso è > 5m --> la misura è errata.
    */
        boolean ret=true;

        //gbl.myLog("isCorrectAltitude --> h["+h+"] H_PRECEDENTE["+H_PRECEDENTE+"] DELTA["+Math.abs(h - H_PRECEDENTE)+"]");
        int delta_T = 60000;
        if (h>0) {
            // Se è da almeno delta_T che non aggiorno l'altitudine forzo l'aggiornamento
            if (isTimeToRefreshAltitude(delta_T)) {
                ret = true;
            }else if(Math.abs(h - gbl.H_PRECEDENTE) > 5 && gbl.H_PRECEDENTE!=0){
                ret = false;
            }
        }
        else
            ret = false;

        return ret;
    }

    // Popola la lista di valori che concorrono a calcolare l'altezza media
    private boolean populate_array_of_h(double h) {
        int DIM = 60,       // dimensione del BUFFER circolare
            MIN_DIM = 20;   // dimensione del buffer cirdcolare oltre la quale inizio a calcolare la media
        boolean retValue = true;

        if(h>0) {

            if (index_h >= DIM - 1)
                index_h = 0;
            else
                index_h++;

            if (lst_h.size() < DIM)
                lst_h.add(h);
            else
                lst_h.set(index_h, h);

            if (lst_h.size() < MIN_DIM)
                retValue = false;
       }
        else
            retValue = false;

        //gbl.myLog("populate_array_of_h --> index_h["+index_h+"] h["+h+"] lst_h.size()["+lst_h.size()+"] retValue=["+retValue+"]");

        return retValue;

    }

    // Procedura che gestisce il calcolo dell'altitudine in base ad un buffer circolare di 60 posizioni
    private int altitudeManager(double h){

        if(!populate_array_of_h(h)) {
            //gbl.myLog("altitudeManager --> populate_array_of_h --> h = 0");
            return (int)gbl.H_PRECEDENTE;
        }
        //double h_avg = sum_h/lst_h.size();
        double h_avg = gbl.getAverage(lst_h);
        h_avg = h_avg + gbl.pref_gps_geoid_correction;

        int ret_h = 0;
        if(isCorrectAltitude(h_avg)) {
            ret_h = (int) h_avg;
        }else
            ret_h = (int) gbl.H_PRECEDENTE;

        //gbl.myLog("altitudeManager --> isCorrectAltitude --> ret_h["+ret_h+"]");

        return ret_h;


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void myOnLocationChanged(Location location) {
        try {
            POS_ATTUALE = new LatLng(location.getLatitude(), location.getLongitude());
            GP_POS_ATTUALE = new GeoPoint(location.getLatitude(), location.getLongitude());

            h = altitudeManager(location.getAltitude());
            int delta_d_absolute = GP_POS_ATTUALE.distanceTo(GP_POS_PRECEDENTE);
            int delta_d_saved = GP_POS_ATTUALE.distanceTo(last_gp_saved_on_db);

            altitude = "" + (int)h;

            motionDetect();

            //if(!altitude.contains("-") && altitude == "0")
            if(h > 0 ) {
                tv_alt.setText(String.format("H=" + altitude + "m"));
            }

            setCurrentPositionOnMap_OSM(2000, get_distance_from_track());

            if (location.getLatitude() == 0) {
                POS_PRECEDENTE = POS_ATTUALE;
                GP_POS_PRECEDENTE = GP_POS_ATTUALE;
            }

            // LIMITAZIONE ALL'USO NELLA VERSIONE FREE
            if (isTimeToStop(T_STOP * 60 * 1000)) {
                if (isFollowing) {
                    isFollowing = false;
                    if (dlg_foll != null) {
                        showNotification();
                        setTrackToFollow_OSM(gbl.getSelectTrack_osm().getTrackId(), "0");
                        img_manageFollow.setImageResource(R.mipmap.follow_green);
                        img_outOfPath.setVisibility(ImageView.INVISIBLE);
                        TextView tv_img_start_stop_follow = (TextView) dlg_foll.findViewById(R.id.tv_img_start_stop_follow);
                        tv_img_start_stop_follow.setText(R.string.fa_play_circle_o);
                        FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont, gbl.DLG_BTN_SIZE, Color.GREEN);
                        crono_track_follow_dlg.stop();
                        crono_track_follow_dlg.setBase(SystemClock.elapsedRealtime());
                    }
                }

                if (isRecording) {

                    isRecording = false;
                    if (dlg_rec != null) {
                        TextView tv_img_play_pause_rec = (TextView) dlg_rec.findViewById(R.id.tv_img_play_pause_rec);
                        TextView tv_img_padlock = (TextView) dlg_rec.findViewById(R.id.tv_img_padlock);

                        tv_img_play_pause_rec.setText(R.string.fa_play_circle_o);
                        FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont, gbl.DLG_BTN_SIZE, Color.RED);
                        btn_rec.setImageResource(R.drawable.rec_stop);
                        ins_location_DB(); // questo è l'ultimo punto tracciato
                        SaveTrackOnDB("RECORDED_TRACK");
                        int trackIndex = drawAllTracks_OSM();
                        zoomTrack(trackIndex);
                        isRecording = false;
                        gbl.setIsPaused(false);
                        gbl.setRecordingTime(0);
                        crono_rec_dlg.stop();
                        crono_rec_dlg.setBase(SystemClock.elapsedRealtime());
                        resetLiveData();

                        tv_img_padlock.setText(R.string.fa_unlock_alt);
                        gbl.setIsLocked(false);
                        dlg_rec.setCanceledOnTouchOutside(true);

                        showNotification();
                    }
                }
            }



            // registra sul DB se se siamo in REC ma non in PAUSE
            if (isRecording && !gbl.isPaused()) {

                showNotification();

                // ritengo valida la misura se lo spostamento è superiore alla soglia prefissata
                if (delta_d_absolute > gbl.pref_gps_minum_distance) {
                    distance_live_rec += delta_d_absolute; // Calcolo della distanza percorsa in fase di recording
                }

                wp_LatLng = POS_ATTUALE; // serve per l'eventuale aggiunta di un WP sulla traccia che sto registrando

                /*
                   La posizione nel DB viene salvata ogni "pref_gps_minum_time" minuti in modo da non riempire troppo il DB
                   In effetti così facendo ho un effetto collaterale: quando faccio STOP/PAUSA potrei perde il punto finale della registrazione,
                   per ovviare a questo ogni volta che premo STOP/PAUSA vado ad inserire il punto attuale nel DB
                */
                //gbl.myLog("myOnLocationChanged --> h["+h+"] delta_d_saved["+delta_d_saved+"]");
                if (h > 0) {
                    // ------------------------------------------------------------
                    // Inserisco i dati nel DB se:
                    // E' il primo punto della traccia
                    // OPPURE
                    // E' passato un certo dT da quando ho salvato l'ultima volta AND mi sono mosso di una distanza superiore alla soglia
                    // ------------------------------------------------------------
                    /*
                    if (isFirstPointToTrack || 
                            (isTimeToSavePointToDB(gbl.pref_gps_minum_time * 60 * 1000) && delta_d_saved > gbl.pref_gps_minum_distance) ) {
                            */
                    if (isFirstPointToTrack ||
                            (isTimeToSavePointToDB(gbl.pref_gps_minum_time * 60 * 1000) && isMoving)) {
                        isMoving = false;

                        if (isFirstPointToTrack) {
                            loadGeopoints(); // qua dentro setto anche  distance_live_rec = 0 prima di iniziare a misurare
                            drawTrackOnMap_Live(GP_POS_ATTUALE); // Disegno la polyline sulla mappa con continuità
                            isFirstPointToTrack = false;
                        }

                        ///Gestione dei dislivelli positivo e negativo uilizzando la H media
                        if (h_precedente_saved > 0) {
                            double delta_h = h - h_precedente_saved;

                            if (delta_h > 0)
                                d_plus_live += delta_h;
                            else
                                d_minus_live += delta_h * (-1);
                        }

                        if (h > h_max_live)
                            h_max_live = h;

                        if (h < h_min_live && h > 0)
                            h_min_live = h;

                        ins_location_DB();
                        h_precedente_saved = h;
                        last_gp_saved_on_db = GP_POS_ATTUALE;
                    }

                    // Il disegno della traccia deve essere fatto dopo che ho caricato i punti con loadGeopoints
                    if (delta_d_absolute > gbl.pref_gps_minum_distance) {
                        drawTrackOnMap_Live(GP_POS_ATTUALE); // Disegno la polyline sulla mappa con continuità
                    }
                }
            }

            if(is_wp_selected) { // Se c'è un WP selezionato

                if (polyline_wp_to_follow == null)
                    polyline_wp_to_follow = new org.osmdroid.views.overlay.Polyline();

                ArrayList<GeoPoint> lst = new ArrayList<>();
                lst.add(GP_POS_ATTUALE);
                lst.add(selected_marker_wp.getPosition());
                polyline_wp_to_follow.setPoints(lst);
                map_osm.getOverlays().remove(polyline_wp_to_follow);
                map_osm.getOverlays().add(polyline_wp_to_follow);
                map_osm.invalidate();
            }else{
                map_osm.getOverlays().remove(polyline_wp_to_follow);
                map_osm.invalidate();
            }


            if (isFollowing) {
                double d = getMinumDistancePoint(POS_ATTUALE);
                d_track_following = d;
                if (d > gbl.pref_following_minum_distance) {
                    long isTimeToRing = System.currentTimeMillis() - startTime_for_ringTimer; // calcolo il tempo da quando sono uscito dal sentiero
                    img_outOfPath.setVisibility(ImageView.VISIBLE);

                   if (gbl.pref_following_sound_out_of_path && !isAcusticAlarmSuppressed) {
                        img_bell.setVisibility(ImageView.VISIBLE);
                        if (isTimeToRing >= gbl.pref_following_default_interval_ring) {
                            ringtone();
                            startTime_for_ringTimer = System.currentTimeMillis();
                        }
                    }
                }
                else {
                    img_outOfPath.setVisibility(ImageView.INVISIBLE);
                    img_bell.setVisibility(ImageView.INVISIBLE);
                    isAcusticAlarmSuppressed = false;
                    startTime_for_ringTimer =0;
                }

            } else {
                isAcusticAlarmSuppressed = false;
                img_outOfPath.setVisibility(ImageView.INVISIBLE);
                img_bell.setVisibility(ImageView.INVISIBLE);
                startTime_for_ringTimer =0;
            }

            if(gbl.isCentertMode_ON()){
                mapController.setZoom(15);
                mapController.animateTo(GP_POS_ATTUALE);

            }

            map_osm.invalidate();

            // Se mi sono mosso "sufficientemente" --> Salvo la posizione, altrimenti resta valida la posizione precedente.
            if(delta_d_absolute > gbl.pref_gps_minum_distance) {
                POS_PRECEDENTE = POS_ATTUALE;
                GP_POS_PRECEDENTE = GP_POS_ATTUALE;
            }
            if(h>0) gbl.H_PRECEDENTE = h;

        } catch (Exception e) {
            gbl.myLog("ERRORE in myOnLocationChanged ["+e.toString()+"]");
        }

    }

    /*
     * Verifica il valore della ATTIVITA se individua il movimento --> isMoving = true e lo mantiene
     *  viene richiamata in myOnLocationChanged in modo da verificare in ogni momento se c'è stato movimento
     *  Nel momento in cui viene salvato il punto nel DB la variabile isMoving viene settata a false
     */
    private void motionDetect(){
        try {
            if (!isMoving || !isRecording) {
                int activityType = gbl.getActivity().getType();

                switch (activityType) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.ON_FOOT:
                    case DetectedActivity.RUNNING:
                    case DetectedActivity.TILTING:
                    case DetectedActivity.WALKING:
                        isMoving = true;
                        break;
                    default:
                        isMoving = false;
                        break;
                }
            }

        }catch (Exception e) {
            gbl.myLog("ERRORE in motionDetect ["+e.toString()+"]");
        }
    }

    // Produce il suono per l'uscita dalla traccia
    private void ringtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Aggiorno il DB con la posizione attuale
    private void ins_location_DB() {
        // NB: inserisco l'altezza media altezza media
        //gbl.myLog("myOnLocationChanged --> ins_location_DB --> h["+h+"]");
        myDatabase.ins_Location_Rows(("" + GP_POS_ATTUALE.getLatitude()), ("" + GP_POS_ATTUALE.getLongitude()), "" + h);

    }

    private int drawAllTracks_OSM(){
        try {
            if (map_osm != null) {
                String trackId;

                lst_polyline = new ArrayList<>();
                lst_track_osm = new ArrayList<>();

                lst_track_osm = gbl.getTrackOsmCollection().get_All_track_osm();

                for(Track_OSM t: lst_track_osm){
                    if(t.isVisible())
                        drawTrackOnMap_OSM(t);
                }

                //Disegna sulla mappa i marker dei WayPoints generici
                int WP_ID = 1, WP_NAME = 3, WP_LAT = 4, WP_LON = 5, WP_ALT = 6, WP_CMT = 6, WP_DESC = 7, WP_SYM = 9;
                Cursor cur = myDatabase.get_waypoints(FAKE_TRACK_GENERIC_WP);
                cur.moveToFirst();
                for (int i = 0; i < cur.getCount(); i++) {
                    final EA_Marker m = new EA_Marker(map_osm,cur.getInt(WP_ID));
                    m.setPosition(new GeoPoint(cur.getDouble(WP_LAT), cur.getDouble(WP_LON)));
                    m.setTitle(cur.getString(WP_NAME));
                    m.setIcon(ResourcesCompat.getDrawable(getResources(), cur.getInt(WP_SYM), null));

                    m.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
                            selected_marker_wp = m;
                            selected_marker_wp.showInfoWindow();
                            return false;
                        }
                    });

                    fake_track_generic_marker_wp.add(m);

                    cur.moveToNext();
                }

                map_osm.getOverlays().addAll(fake_track_generic_marker_wp);

                cur.close();
            }

            return lst_track_osm.size()-1;
        }catch(Exception e){
            gbl.myLog("Errore in drawAllTracks_OSM ["+e.toString()+"]");
            return 0;
        }
    }

    private double getMinumDistancePoint(LatLng pos){
        try {

            double min_d = 999999.9;

            if (gbl.getSelectTrack_osm() != null) {
                for (GeoPoint p : gbl.getSelectTrack_osm().lst_geoPoint) {
                    double d = p.distanceTo(convert_LatLng_to_GeoPoint(pos));
                    if (d < min_d) {
                        min_d = d;

                        POS_FOLLOWING = convert_GeoPoint_to_LatLng(p);
                    }
                }
            }
            else
                gbl.myLog("getMinumDistancePoint gbl.getSelectTrack_osm() [NULL]");

            return min_d;

        }catch(Exception e){
            gbl.myLog("ERRORE in getMinumDistancePoint ["+e.toString()+"]");
            return -1;
        }

    }

   private void resetLiveData(){
        h_max_live = 0;
        h_min_live =99999;
        d_plus_live=0;
        d_minus_live=0;
        distance_live_rec=0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDlg_REC() {
        try {
            dlg_rec = new Dialog(this);
            dlg_rec.setTitle("Recording Track Data");
            dlg_rec.setContentView(R.layout.dlg_recording_data);
            dlg_rec.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

            final TextView tv_img_play_pause_rec = (TextView) dlg_rec.findViewById(R.id.tv_img_play_pause_rec);
            final TextView tv_img_padlock = (TextView) dlg_rec.findViewById(R.id.tv_img_padlock);
            TextView tv_img_stop = (TextView) dlg_rec.findViewById(R.id.tv_img_stop);

            FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);
            FontManager.markAsIconContainer(tv_img_padlock, iconFont,gbl.DLG_BTN_SIZE,Color.LTGRAY);
            FontManager.markAsIconContainer(tv_img_stop, iconFont,gbl.DLG_BTN_SIZE,Color.BLUE);

            if(gbl.isLocked()) {
                tv_img_padlock.setText(R.string.fa_lock);
            }

            tv_img_padlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!gbl.isLocked()){
                        tv_img_padlock.setText(R.string.fa_lock);
                        gbl.setIsLocked(true);
                        dlg_rec.setCanceledOnTouchOutside(false);
                    }
                }
            });

            tv_img_padlock.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(gbl.isLocked()){
                        tv_img_padlock.setText(R.string.fa_unlock_alt);
                        gbl.setIsLocked(false);
                        dlg_rec.setCanceledOnTouchOutside(true);
                    }
                    return true;
                }
            });

            final TextView tv_distance = (TextView) dlg_rec.findViewById(R.id.tv_distance);
            final TextView tv_speed = (TextView) dlg_rec.findViewById(R.id.tv_speed);
            final TextView tv_h_max = (TextView) dlg_rec.findViewById(R.id.tv_H_max);
            final TextView tv_h_min = (TextView) dlg_rec.findViewById(R.id.tv_H_min);
            final TextView tv_d_plus = (TextView) dlg_rec.findViewById(R.id.tv_D_plus);
            final TextView tv_d_minus = (TextView) dlg_rec.findViewById(R.id.tv_D_minus);

            double zero = 0;
            tv_distance.setText(String.format("%.2f", zero));
            tv_speed.setText(String.format("%.2f", zero));
            tv_h_max.setText("-");
            tv_h_min.setText("-");
            tv_d_plus.setText(String.format("%.0f", zero));
            tv_d_minus.setText(String.format("%.0f", zero));

            crono_rec_dlg = (Chronometer) dlg_rec.findViewById(R.id.chrono_rec_dlg);
            crono_rec_dlg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    long elapsedMillis = SystemClock.elapsedRealtime() - crono_rec_dlg.getBase();

                    double speed;

                    if(elapsedMillis != 0 )
                        speed = (distance_live_rec / (elapsedMillis/1000) ) * 3.6;
                    else
                        speed = 0;
                    if(Double.isNaN(distance_live_rec)) distance_live_rec = 0;
                    if(Double.isNaN(speed)) speed = 0;

                    tv_distance.setText(String.format("%.2f", (distance_live_rec/1000)));
                    tv_speed.setText(String.format("%.2f", speed));
                    if(h_max_live == 0)
                        tv_h_min.setText("-");
                    else
                        tv_h_max.setText(String.format("%.0f", h_max_live));
                    if(h_min_live == 99999)
                        tv_h_min.setText("-");
                    else
                        tv_h_min.setText(String.format("%.0f", h_min_live));

                    tv_d_plus.setText(String.format("%.0f", d_plus_live));
                    tv_d_minus.setText(String.format("%.0f", d_minus_live));
                }
            });

            if (isRecording) {
                crono_rec_dlg.setBase(gbl.getRecordingTime());
                crono_rec_dlg.start();

                if (gbl.isPaused()) {
                    tv_img_play_pause_rec.setText(R.string.fa_play_circle_o);
                    FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);
                    showNotification();
                }
                else {
                    tv_img_play_pause_rec.setText(R.string.fa_pause_circle_o);
                    FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.YELLOW);
                    showNotification();
                }
            } else {
                crono_rec_dlg.setBase(SystemClock.elapsedRealtime());

                tv_distance.setText("0,00");
                tv_speed.setText("0,00");
                tv_d_minus.setText("0");
                tv_d_plus.setText("0");
                tv_h_max.setText("-");
                tv_h_min.setText("-");
                tv_img_play_pause_rec.setText(R.string.fa_play_circle_o);
                FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.RED);

            }

            tv_img_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!gbl.isLocked()) {
                        if (isRecording)
                            showDlg_STOP_REC(tv_img_play_pause_rec);
                    }
                }
            });
            tv_img_play_pause_rec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!gbl.isLocked()) {

                        if (isRecording) {

                            if (gbl.isPaused()) {
                                // UNPAUSE
                                ins_location_DB(); // Questo per inserire il punto di partenza dopo il PAUSE
                                gbl.setIsPaused(false);
                                tv_img_play_pause_rec.setText(R.string.fa_pause_circle_o);
                                FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.YELLOW);
                                btn_rec.setImageResource(R.drawable.rec_rec);

                                showNotification();
                            } else {
                                //PAUSE
                                ins_location_DB(); // Questo è l'ultimo punto tracciato prima del PAUSE
                                gbl.setIsPaused(true);
                                tv_img_play_pause_rec.setText(R.string.fa_play_circle_o);
                                FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);
                                btn_rec.setImageResource(R.drawable.rec_pause);

                                showNotification();
                            }
                        } else {
                            manageTrackRecording("START");

                            double zero = 0;
                            tv_distance.setText(String.format("%.2f", zero));
                            tv_speed.setText(String.format("%.2f", zero));
                            tv_h_max.setText("-");
                            tv_h_min.setText("-");
                            tv_d_plus.setText(String.format("%.0f", zero));
                            tv_d_minus.setText(String.format("%.0f", zero));

                            lst_geoPoint_rec_live = new ArrayList<>();
                            polyline_rec_live = new org.osmdroid.views.overlay.Polyline();
                            isRecording = true;

                            delTrackOnMemory(); // in caso ci siano tracce in fase di registrazione abortisco tutto
                            gbl.setRecordingTime(SystemClock.elapsedRealtime()); // Istante di inizio registrazione
                            crono_rec_dlg.setBase(SystemClock.elapsedRealtime());

                            crono_rec_dlg.start();
                            btn_rec.setImageResource(R.drawable.rec_rec);
                            mapController.setCenter(convert_LatLng_to_GeoPoint(POS_ATTUALE));
                            tv_img_play_pause_rec.setText(R.string.fa_pause_circle_o);
                            FontManager.markAsIconContainer(tv_img_play_pause_rec, iconFont,gbl.DLG_BTN_SIZE,Color.YELLOW);

                            showNotification();

                        }
                    }
                }
            });


            dlg_rec.show();
        } catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_REC ["+e.toString()+"]");
        }
    }

    private void showDlg_STOP_REC(final TextView tv){
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);
        final Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);

        myBuilder.setCancelable(true);
        final AlertDialog dlg = myBuilder.create();
       String defaultext = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
       LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        final EA_EditText et_trackName = new EA_EditText(this);
        TextInputLayout til_trackName  = new TextInputLayout(this);
        MyTextWatcher tw_fe_editText = new MyTextWatcher(et_trackName,til_trackName, getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            et_trackName.setId(View.generateViewId());
            til_trackName.setId(View.generateViewId());
        }
        et_trackName.setLayoutParams(lp);
        til_trackName.setLayoutParams(lp);
        dlg.setView(et_trackName);
        dlg.setView(til_trackName);
        et_trackName.addTextChangedListener(tw_fe_editText);
        et_trackName.setText(defaultext);

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if(!Character.isSpaceChar(source.charAt(i))) {
                        if (!Character.isLetterOrDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                }
                return null;
            }
        };

        et_trackName.setFilters(new InputFilter[]{filter});
        et_trackName.setHint("Track name");
        til_trackName.addView(et_trackName);

        dlg.setTitle("Save Track");
        dlg.setMessage("Insert track name:");
        dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "DISCARD",
                new DialogInterface.OnClickListener()
                {   @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    map_osm.getOverlays().remove(polyline_rec_live);
                    map_osm.invalidate();
                    delTrackOnMemory();
                    manageTrackRecording("STOP");
                    isRecording = false;
                    gbl.setIsPaused(false);
                    gbl.setRecordingTime(0);
                    crono_rec_dlg.stop();
                    crono_rec_dlg.setBase(SystemClock.elapsedRealtime());

                    btn_rec.setImageResource(R.drawable.rec_stop);
                    tv.setText(R.string.fa_play_circle_o);
                    FontManager.markAsIconContainer(tv, iconFont,100,Color.RED);
                    resetLiveData();
                    showNotification();
                }
                }
        );

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener(){   @Override public void onClick(DialogInterface dialog, int id){} });

        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try{
                            if ( et_trackName.getText().length() > 0 ) {
                                ins_location_DB(); // questo è l'ultimo punto tracciato
                                SaveTrackOnDB(et_trackName.getText().toString());
                                int trackIndex = drawAllTracks_OSM();
                                zoomTrack(trackIndex);
                                isRecording = false;
                                gbl.setIsPaused(false);
                                gbl.setRecordingTime(0);
                                crono_rec_dlg.stop();
                                crono_rec_dlg.setBase(SystemClock.elapsedRealtime());

                                btn_rec.setImageResource(R.drawable.rec_stop);
                                tv.setText(R.string.fa_play_circle_o);
                                FontManager.markAsIconContainer(tv, iconFont,100,Color.RED);
                                resetLiveData();
                                showNotification();

                            }
                        }
                        catch (Exception e){
                            gbl.myLog("ERRORE in DialogInterface.OnClickListener --> OnClick["+ e.toString() +"]");
                        }
                    }
                });

        dlg.show();
    }

    private void showDlg_SELECT_TRACK() {
        try {
            Dialog dlg = new Dialog(this);
            dlg.setTitle("");
            dlg.setContentView(R.layout.dlg_sel_tracklist);
            dlg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

            TextView tv_img_list = (TextView) dlg.findViewById(R.id.tv_img_list);
            TextView tv_img_calendar = (TextView) dlg.findViewById(R.id.tv_img_calendar);

            Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
            FontManager.markAsIconContainer(tv_img_list, iconFont,100,Color.GREEN);
            FontManager.markAsIconContainer(tv_img_calendar, iconFont,100,Color.YELLOW);

            tv_img_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((TextView) v).setTextColor(Color.BLUE);

                    new CountDownTimer(500, 100) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {((TextView) v).setTextColor(Color.GREEN);}
                    }.start();
                    Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                    startActivity(intent);
                }
            });

            tv_img_calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((TextView) v).setTextColor(Color.BLUE);

                    new CountDownTimer(500, 100) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {((TextView) v).setTextColor(Color.YELLOW);}
                    }.start();
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    startActivity(intent);
                }
            });

            dlg.show();
        } catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_REC ["+e.toString()+"]");
        }
    }

//TODO: completare funzionalità
    private boolean checkTimeOfLastTrackRecorded() throws ParseException {
        int TIME = 4;
        long time_sec=-1;
        Boolean retValue = true;
        Cursor cur = myDatabase.sel_Location_Rows();
        cur.moveToLast();
        if(cur.getCount()>0)
            time_sec = cur.getLong(TIME)*1000;

        if (time_sec >0){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = dateFormat.parse("" + time_sec);
            Date now = new Date(System.currentTimeMillis());
            if(date.getTime() - now.getTime() < 3600 * 1000)
                retValue=true;

        }
        else{
            retValue = false;
        }

        return retValue;
    }

    private void showDlg_STOP_TRACK_FOLLOW(){
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);

        myBuilder.setCancelable(true);
        final AlertDialog dlg = myBuilder.create();

        dlg.setTitle("Attention");
        dlg.setMessage("Do you want to stop following?");
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override  public void onClick(DialogInterface dialog, int id){  }});
        dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                "STOP FOLLOWING", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            isFollowing = false;
                            //10.10.2017 START ID=4: rimozione dei marker
                            map_osm.getOverlays().remove(follow_marker);
                            map_osm.getOverlays().remove(curr_position_marker);
                            //10.10.2017 END ID=4: rimozione dei marker
                            showNotification();
                            setTrackToFollow_OSM(gbl.getSelectTrack_osm().getTrackId(), "0");
                            img_manageFollow.setImageResource(R.mipmap.follow_green);
                            img_outOfPath.setVisibility(ImageView.INVISIBLE);
                            tv_img_start_stop_follow.setText(R.string.fa_play_circle_o);
                            FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);
                            crono_track_follow_dlg.stop();
                            crono_track_follow_dlg.setBase(SystemClock.elapsedRealtime());
                        }
                        catch(Exception e){gbl.myLog("Errore in STOP FOLLOWING ["+e.toString()+"]");}

                    }
                });

        dlg.show();
    }

    private void showDlg_TRACK_FOLLOW() {
        try {
            dlg_foll = new Dialog(this);
            dlg_foll.setTitle("Following Track Data");
            dlg_foll.setContentView(R.layout.dlg_foll_track_data);
            dlg_foll.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

            tv_img_start_stop_follow = (TextView) dlg_foll.findViewById(R.id.tv_img_start_stop_follow);
            FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);

            final TextView tv_distance = (TextView) dlg_foll.findViewById(R.id.tv_distance);
            final TextView tv_outOPath = (TextView) dlg_foll.findViewById(R.id.tv_outOPath);
            final TextView tv_trackName = (TextView) dlg_foll.findViewById(R.id.tv_trackName);

            crono_track_follow_dlg = (Chronometer) dlg_foll.findViewById(R.id.chrono_follow_dlg);

            if(!isFollowing) {
                tv_distance.setText(String.format("%.2f", 0.0));
                tv_outOPath.setText("-");
                tv_trackName.setText(gbl.getSelectTrack_osm().getTrackName());
                tv_img_start_stop_follow.setText(R.string.fa_play_circle_o);
                FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);

            }else{
                long deltaT = (SystemClock.elapsedRealtime() - start_track_following_time);
                crono_track_follow_dlg.setBase(SystemClock.elapsedRealtime() - deltaT);
                crono_track_follow_dlg.start();

                tv_distance.setText(String.format("%.2f", d_track_following /1000));
                if( img_outOfPath.getVisibility() == ImageView.VISIBLE )
                    tv_outOPath.setText(getString(R.string.str_SI));
                else
                    tv_outOPath.setText(getString(R.string.str_NO));
                tv_trackName.setText(gbl.getSelectTrack_osm().getTrackName());

                tv_img_start_stop_follow.setText(R.string.fa_stop_circle_o);
                FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.RED);

            }

            crono_track_follow_dlg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    tv_distance.setText(String.format("%.2f", d_track_following /1000));

                    if( img_outOfPath.getVisibility() == ImageView.VISIBLE )
                        tv_outOPath.setText(getString(R.string.str_SI));
                    else
                        tv_outOPath.setText(getString(R.string.str_NO));

                }

            });

            tv_img_start_stop_follow.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    try {
                        if (!isFollowing) {
                            isFollowing = true;
                            showNotification();
                            tv_trackName.setText(gbl.getSelectTrack_osm().getTrackName());
                            setTrackToFollow_OSM(gbl.getSelectTrack_osm().getTrackId(), "1");
                            img_manageFollow.setImageResource(R.mipmap.follow_red);
                            tv_img_start_stop_follow.setText(R.string.fa_stop_circle_o);
                            FontManager.markAsIconContainer(tv_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.RED);
                            crono_track_follow_dlg.setBase(SystemClock.elapsedRealtime());
                            crono_track_follow_dlg.start();
                            start_track_following_time = crono_track_follow_dlg.getBase();
                        } else {
                            showDlg_STOP_TRACK_FOLLOW();
                        }
                    } catch (Exception e) {
                        gbl.myLog("ERRORE in img_start_stop_follow.setOnClickListener ["+e.toString()+"]");
                    }
                }
            });

            dlg_foll.show();

        } catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_TRACK_FOLLOW ["+e.toString()+"]");
        }
    }

    private void showDlg_STOP_WP_FOLLOW(){
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);

        myBuilder.setCancelable(true);
        final AlertDialog dlg = myBuilder.create();

        dlg.setTitle("Attention");
        dlg.setMessage("Do you want to stop following?");
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override  public void onClick(DialogInterface dialog, int id){  }});
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "STOP FOLLOWING", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            is_wp_selected = false;
                            selected_marker_wp = null;
                            img_select_wp.setImageResource(R.mipmap.marker_unchecked);
                            tv_wp_img_start_stop_follow.setText(R.string.fa_play_circle_o);
                            FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont, gbl.DLG_BTN_SIZE, Color.GREEN);
                            showNotification();
                            crono_wp_follow_dlg.stop();
                            crono_wp_follow_dlg.setBase(SystemClock.elapsedRealtime());
                        }
                        catch(Exception e){gbl.myLog("Errore in STOP FOLLOWING ["+e.toString()+"]");}

                    }
                });

        dlg.show();
    }

    private void showDlg_INFO_TRACK(){
        try {
            Dialog dlg = new Dialog(this);
            dlg.setTitle("Info Track Data");
            dlg.setContentView(R.layout.dlg_info_track);
            dlg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            TextView tv_info_track_name = (TextView) dlg.findViewById(R.id.tv_info_track_name);
            TextView tv_info_length = (TextView) dlg.findViewById(R.id.tv_info_length);
            TextView tv_info_time = (TextView) dlg.findViewById(R.id.tv_info_time);
            TextView tv_info_H_max = (TextView) dlg.findViewById(R.id.tv_info_H_max);
            TextView tv_info_H_min = (TextView) dlg.findViewById(R.id.tv_info_H_min);
            TextView tv_info_D_plus = (TextView) dlg.findViewById(R.id.tv_info_D_plus);
            TextView tv_info_D_minus = (TextView) dlg.findViewById(R.id.tv_info_D_minus);

            Track_OSM t = gbl.getSelectTrack_osm();
            tv_info_track_name.setText(t.getTrackName());
            tv_info_length.setText(gbl.formatLength(t.getLength()));
            tv_info_time.setText(gbl.formatTime(t.getDuring()));
            tv_info_H_max.setText(t.getHmax());
            tv_info_H_min.setText(t.getHmin());
            tv_info_D_plus.setText(t.getDeltaHPos());
            tv_info_D_minus.setText(t.getDeltaHNeg());

            dlg.show();
        }
        catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_INFO_TRACK ["+e.toString()+"]");
        }
    }

    private void showDlg_ADD_WP(){
        try {
            final Dialog dlg = new Dialog(this);
            dlg.setTitle("Add Waypoint");
            dlg.setContentView(R.layout.dlg_add_waypoint);
            dlg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            final EA_EditText et_awp_name = (EA_EditText) dlg.findViewById(R.id.et_awp_name);
            final EA_EditText et_awp_alt = (EA_EditText) dlg.findViewById(R.id.et_awp_alt);
            TextView tv_awp_img_save = (TextView) dlg.findViewById(R.id.tv_awp_img_save);
            TextView tv_awp_img_cancel = (TextView) dlg.findViewById(R.id.tv_awp_img_cancel);
            TextInputLayout til_awp_name = (TextInputLayout) dlg.findViewById(R.id.til_awp_name);

            et_awp_name.set_ic_color("GREY");
            et_awp_alt.set_ic_color("GREY");

            Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
            FontManager.markAsIconContainer(tv_awp_img_save, iconFont,100,Color.BLUE);
            FontManager.markAsIconContainer(tv_awp_img_cancel, iconFont,100,Color.RED);

            MyTextWatcher tw_name = new MyTextWatcher(et_awp_name,til_awp_name, getApplicationContext());
            et_awp_name.addTextChangedListener(tw_name);
            et_awp_name.setText("MY WAYPOINT NAME"); // default

            final Spinner mySpinner = (Spinner) dlg.findViewById(R.id.sp_awp_icon);
            mySpinner.setAdapter(new SpinnerAdapter(this, R.layout.spinner_row_layout, getAllList(),false));

            if(isRecording)
                et_awp_alt.setText(""+altitude);
            else
                et_awp_alt.setText("");

            tv_awp_img_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dlg.dismiss();
                }
            });

            tv_awp_img_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String trackId = FAKE_TRACK_GENERIC_WP;

                    if (isRecording)
                        trackId = FAKE_TRACK_RECORDING_WP; // Traccia temporanea di registrazione
                    else {
                        if (gbl.getSelectTrack_osm() != null) {
                            trackId = gbl.getSelectTrack_osm().getTrackId();
                        }
                    }

                    if (et_awp_name.getText().length() > 0 && trackId.length()>0) {

                        Double lat = wp_LatLng.latitude;
                        Double lon = wp_LatLng.longitude;
                        GeoPoint gp = new GeoPoint(lat, lon);

                        int wp_id = Save_WP_on_DB(trackId, et_awp_name.getText().toString(), lat.toString(), lon.toString(), et_awp_alt.getText().toString(), ((ListItem) mySpinner.getSelectedItem()).imgThumb);
                        EA_Marker m = new EA_Marker(map_osm,wp_id);

                        m.setPosition(gp);
                        m.setTitle(et_awp_name.getText().toString());
                        m.setIcon(ResourcesCompat.getDrawable(getResources(), ((ListItem) mySpinner.getSelectedItem()).imgThumb, null));

                        if (!isRecording) {
                            current_marker_wp.setId(wp_id);
                            current_marker_wp.setTitle(et_awp_name.getText().toString());

                            if (gbl.getSelectTrack_osm() != null)
                                gbl.getSelectTrack_osm().lst_waypoints_marker.add(current_marker_wp);
                            else
                                fake_track_generic_marker_wp.add(current_marker_wp);
                        }

                        map_osm.getOverlays().add(m);
                        map_osm.invalidate();

                        dlg.dismiss();
                    }
                }
            });


            dlg.show();
        }
        catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_INFO_TRACK ["+e.toString()+"]");
        }
    }

    private void showDlg_WP_FOLLOW() {
        try {
            Dialog dlg = new Dialog(this);
            dlg.setTitle("Following WP Data");
            dlg.setContentView(R.layout.dlg_foll_wp);
            dlg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

            tv_wp_img_start_stop_follow = (TextView) dlg.findViewById(R.id.tv_wp_img_start_stop_follow);
            FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.GREEN);

            final TextView tv_distance = (TextView) dlg.findViewById(R.id.tv_distance);
            final TextView tv_wpName = (TextView) dlg.findViewById(R.id.tv_wpName);

            crono_wp_follow_dlg = (Chronometer) dlg.findViewById(R.id.chrono_follow_dlg);

            if(is_wp_selected) {
                long deltaT = (SystemClock.elapsedRealtime() - start_wp_following_time);
                crono_wp_follow_dlg.setBase(SystemClock.elapsedRealtime() - deltaT);
                crono_wp_follow_dlg.start();

                tv_distance.setText(String.format("%.2f", d_wp_following / 1000));
                tv_wpName.setText(selected_marker_wp.getTitle());
                tv_wp_img_start_stop_follow.setText(R.string.fa_stop_circle_o);
                FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont,gbl.DLG_BTN_SIZE,Color.RED);

            }else {
                tv_distance.setText(String.format("%.2f", 0.0));

                if (selected_marker_wp != null) {
                    tv_wpName.setText(selected_marker_wp.getTitle());
                } else
                    tv_wpName.setText(current_marker_wp.getTitle());

                tv_wp_img_start_stop_follow.setText(R.string.fa_play_circle_o);
                FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont, gbl.DLG_BTN_SIZE, Color.GREEN);
            }

            crono_wp_follow_dlg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    d_wp_following = GP_POS_ATTUALE.distanceTo(selected_marker_wp.getPosition());
                    tv_distance.setText(String.format("%.2f", d_wp_following /1000));
                }

            });

            tv_wp_img_start_stop_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(selected_marker_wp != null && !is_wp_selected){
                            is_wp_selected = true;
                            img_select_wp.setImageResource(R.mipmap.marker_checked);
                            tv_wp_img_start_stop_follow.setText(R.string.fa_stop_circle_o);
                            FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont, gbl.DLG_BTN_SIZE, Color.RED);
                            crono_wp_follow_dlg.setBase(SystemClock.elapsedRealtime());
                            start_wp_following_time = crono_wp_follow_dlg.getBase();
                            crono_wp_follow_dlg.start();
                            showNotification();

                        }
                        else {
                            if (curr_wp_index >= 0 && !is_wp_selected) {
                                is_wp_selected = true;
                                selected_marker_wp = new org.osmdroid.views.overlay.Marker(map_osm);
                                selected_marker_wp = current_marker_wp;
                                img_select_wp.setImageResource(R.mipmap.marker_checked);
                                tv_wp_img_start_stop_follow.setText(R.string.fa_stop_circle_o);
                                FontManager.markAsIconContainer(tv_wp_img_start_stop_follow, iconFont, gbl.DLG_BTN_SIZE, Color.RED);
                                crono_wp_follow_dlg.setBase(SystemClock.elapsedRealtime());
                                start_wp_following_time = crono_wp_follow_dlg.getBase();
                                crono_wp_follow_dlg.start();
                                showNotification();

                            } else {
                                if (is_wp_selected) {
                                    showDlg_STOP_WP_FOLLOW();
                                }else{
                                    Dlg_Confirm("NO_WP_SELECTED",null);
                                }
                            }
                        }
                    } catch (Exception e) {
                        gbl.myLog("ERRORE in img_start_stop_follow.setOnClickListener ["+e.toString()+"]");
                    }
                }
            });

            dlg.show();
        } catch (Exception e) {
            gbl.myLog("ERRORE in showDlg_TRACK_FOLLOW ["+e.toString()+"]");
        }
    }

    private void Dlg_Confirm(String operation, final Polyline  polyline) {
        try {

            AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);

            myBuilder.setCancelable(true);
            final AlertDialog dlg = myBuilder.create();

            switch(operation){

                case "FOLLOWING_IN_PROGRESS":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Operation not possibile: Following is in progress");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

                case "STOP_TO_FOLLOW":
                case "FOLLOW_IS_ACTIVE":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Do you want to stop followig?");
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",new DialogInterface.OnClickListener(){@Override  public void onClick(DialogInterface dialog, int id){  }});
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                            "STOP FOLLOWING", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        isFollowing = false;
                                        showNotification();
                                        setTrackToFollow_OSM(gbl.getSelectTrack_osm().getTrackId(), "0");
                                        img_manageFollow.setImageResource(R.mipmap.follow_green);
                                        img_outOfPath.setVisibility(ImageView.INVISIBLE);
                                     }
                                    catch(Exception e){gbl.myLog("Errore in STOP FOLLOWING ["+e.toString()+"]");}

                                }
                            });
                    break;
                case "NO_ITEM_TO_SHOW":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Trere are no items to show.");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isFollowing=false;
                            img_manageFollow.setImageResource(R.mipmap.follow_green);
                        }
                    });
                    break;
                case "NO_TRACK_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Please select at least a track");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isFollowing=false;
                            img_manageFollow.setImageResource(R.mipmap.follow_green);
                        }
                    });
                    break;
                case "NO_WP_SELECTED":
                    dlg.setTitle("Attention");
                    dlg.setMessage("Please select at least a WayPoint");
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;
                case "ACTIVITY_IN_PROGRESS":
                    dlg.setTitle("Activity in Progress!");
                    if(isRecording)
                        dlg.setMessage("Press the STOP button to stop the activity.");
                    if(isFollowing)
                        dlg.setMessage("Press Following icon for stop the activity.");

                    dlg.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
                    break;

            }

            if(     !operation.equals("ACTIVITY_IN_PROGRESS") &&
                    !operation.equals("FOLLOWING_IN_PROGRESS") &&
                    !operation.equals("NO_ITEM_TO_SHOW") &&
                    !operation.equals("NO_WP_SELECTED") &&
                    !operation.equals("NO_TRACK_SELECTED")){
                dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
            }

            dlg.show();

            if(operation == "STOP")
                dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
        catch (Exception e){
            gbl.myLog("ERRORE in Dlg_Confirm["+ e.toString() +"]");
        }

    }

    private static float pxFromDp(float dp, Context mContext) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }

    // Popolo la lista delle icone relative ai WayPoints
    public ArrayList<ListItem> getAllList() {
        try {
            ArrayList<ListItem> allList = new ArrayList<ListItem>();

            ListItem item = new ListItem();

            TypedArray descriptions = getResources().obtainTypedArray(R.array.wp_spinner_descriptions);
            TypedArray icons = getResources().obtainTypedArray(R.array.wp_spinner_icons);

            for (int i = 0; i < icons.length(); i++) {
                item = new ListItem();
                item.setData(descriptions.getString(i),icons.getResourceId(i,0));
                allList.add(item);
            }

            return allList;
        }
        catch (Exception e) {
            gbl.myLog( "ERRORE in getAllList[" + e.toString() + "]");
            return null;
        }

    }

    private GeoPoint convert_LatLng_to_GeoPoint(LatLng l){
        return new GeoPoint(l.latitude,l.longitude);
    }

    private LatLng convert_GeoPoint_to_LatLng(GeoPoint gp){
        return new LatLng(gp.getLatitude(),gp.getLongitude());
    }

    private Location convert_LatLng_to_Location(LatLng latLng){

        Location loc = new Location("dummyProvider");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);

        return loc;
    }

    // Setta un il marker relativo alla fase di FOLLOW TRACCIA sulla mappa
    private void showFollowMarker(LatLng pos_curr, LatLng pos_dest, String title, String snippet){
        try {
            map_osm.getOverlays().remove(follow_marker);
            map_osm.getOverlays().remove(curr_position_marker);

            follow_marker.setPosition(convert_LatLng_to_GeoPoint(pos_curr));

            if(title.length()>0)
                follow_marker.setTitle(title) ;

            if(snippet.length()>0)
                follow_marker.setSnippet(snippet);

            follow_marker.showInfoWindow();

            follow_marker.setIcon(ResourcesCompat.getDrawable(getResources(),R.mipmap.navigation,null));

            Location l_curr=convert_LatLng_to_Location(pos_curr),l_dest=convert_LatLng_to_Location(pos_dest);
            float rotation = l_curr.bearingTo(l_dest);
            gbl.setMapOrientation(map_osm.getMapOrientation());
            follow_marker.setRotation(rotation + map_osm.getMapOrientation());

            map_osm.getOverlays().add(follow_marker);

        }catch (Exception e){
            gbl.myLog("ERRORE in addFollowMarker_OSM["+ e.toString() +"]");

        }

    }

    // Setta un marker sulla mappa che indica la direzione di marcia
    //2017.10.10 ID = 2: Cambiato il valore di ritorno poiché era sempre null
    private void addMarkerForMarchDirection(){
        try {

            double d=0;
            // Calcolo la distanza rispetto al check precedente
            if ( old_direction_pos != null ){
                d = SphericalUtil.computeDistanceBetween(POS_ATTUALE, old_direction_pos);
            }

            // se rispetto al check precedente la distanza è oltre la soglia setto il marker con l'indicatore
            if (d > gbl.pref_gps_minum_distance) {

                map_osm.getOverlays().remove(direction_marker);

                direction_marker.setPosition(convert_LatLng_to_GeoPoint(POS_ATTUALE));
                direction_marker.setIcon(ResourcesCompat.getDrawable(getResources(),R.mipmap.direction,null));
                direction_marker.setRotation((float) GetBearing(POS_ATTUALE,old_direction_pos) + gbl.getMapOrientation() );
                direction_marker.setTitle(getString(R.string.marker_direction));
                direction_marker.isInfoWindowShown();

                map_osm.getOverlays().add(direction_marker);
            }

            old_direction_pos = POS_ATTUALE;

        }catch (Exception e){
            gbl.myLog("ERRORE in addMarkerForMarchDirection["+ e.toString() +"]");
        }

    }

    // Calcola l'angolo in gradi dell'inclinazione della direzione di una linea passante per due punti
    private double GetBearing(LatLng from, LatLng to){
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;

        // Compute the angle.
        double angle = - Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ), Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );

        if (angle < 0.0)
            angle += Math.PI * 2.0;

        return 180+Math.toDegrees(angle);
    }

    // Determina se il GeoPoint è visualizzato sullo schermo
    private boolean isCurrentLocationVisible(GeoPoint gp){
        Projection prj = map_osm.getProjection();
        BoundingBox bb = prj.getBoundingBox();

        return  bb.contains(gp);
    }

    private void showSelectTrackLayout(boolean isVisible, int nearestPoint_index, GeoPoint nearest_gp){

        showSelectTrackLayout(isVisible, nearestPoint_index);
        // Se il nearest_gp non è visualizzato sullo schermo visualizzo la traccia a cui appartiene
        if(isVisible ){
            if(!isCurrentLocationVisible(nearest_gp))
                map_osm.zoomToBoundingBox(gbl.getSelectTrack_osm().boundingBox,true);
        }
    }

    /*Colora la traccia selezionata in BLU e posiziona il cursore della seekbar*/
    private void showSelectTrackLayout(boolean isVisible, int nearestPoint_index){
        final LinearLayout container = (LinearLayout)findViewById(R.id.layout_sel_track);

        for(Track_OSM t:lst_track_osm){
            t.setColor(Color.BLACK);
            if(gbl.getSelectTrack_osm()!=null){
                if (gbl.getSelectTrack_osm().getTrackId().equals(t.getTrackId()))
                    t.setColor(Color.BLUE);
            }
        }

        if (isVisible) {
            if (gbl.getSelectTrack_osm() != null && !isFollowing) {
                gbl.getSelectTrack_osm().setColor(Color.BLUE);
                seekBar_map.setMax(gbl.getSelectTrack_osm().lst_geoPoint.size() - 1);
                seekBar_map.setProgress(nearestPoint_index);
                seekBar_map.invalidate();
                tv_trackName_seekbar.setText(gbl.getSelectTrack_osm().getTrackName());
                container.animate().alpha(1).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).start();
                container.setVisibility(LinearLayout.VISIBLE);
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                container.animate().alpha(0).setDuration(animation_length_ms).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        container.setVisibility(LinearLayout.INVISIBLE);
                    }
                }).start();
            }

        }
    }

    private void goToFB() {
        try {
            Intent intent = new Intent(this, FBLoginActivity.class);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in goToFB [" + e.toString() + "]");
        }
    }

    private void goToSettings() {
        try {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            gbl.myLog( "ERRORE in goToSettings [" + e.toString() + "]");
        }
    }

    private void goWPList() {
        try {

            String trackId = "", trackName="";
            Intent intent = new Intent(this, WayPointsListActivity.class);
            // devo dare precedenza alla traccia che sto registrando
            if (isRecording) {
                trackId = FAKE_TRACK_RECORDING_WP;
                trackName = getString(R.string.fake_track_rec_name);
            }
            else {
                if ( gbl.getSelectTrack_osm() != null ) {
                    trackId = gbl.getSelectTrack_osm().getTrackId();
                    trackName  = gbl.getSelectTrack_osm().getTrackName();
                }
                else {
                    trackId = FAKE_TRACK_GENERIC_WP;
                    trackName = getString(R.string.fake_track_generic);
                }
            }

            Cursor cur = myDatabase.get_waypoints(trackId);
            cur.moveToFirst();
            if(cur.getCount()==0){
                Dlg_Confirm("NO_ITEM_TO_SHOW",null);
            }else{
                intent.putExtra("trackId",trackId);
                intent.putExtra("trackName",trackName);
                startActivity(intent);
                //finish();
            }
            cur.close();



        } catch (Exception e) {
            gbl.myLog( "ERRORE in goWPList [" + e.toString() + "]");
        }
    }

    private int Save_WP_on_DB(String trackId, String name, String lat, String lon, String alt, int icon_res_id){

        String id="", cmt="", desc="", sym=""+icon_res_id;

        return myDatabase.ins_WayPoint(id,trackId, lat, lon, alt, name, cmt, desc, sym);
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // Quando rientro nella activity e scopro che ho una registrazione pending --> Ricreo la struttura dati
    private void loadGeopoints(){
        try {
            int LAT = 1, LON = 2;

            if(lst_geoPoint_rec_live==null)
                lst_geoPoint_rec_live = new ArrayList<>();
            if (polyline_rec_live==null)
                polyline_rec_live = new org.osmdroid.views.overlay.Polyline();

            Cursor cur = null;
            cur = myDatabase.sel_Location_Rows();
            cur.moveToFirst();
            GeoPoint gp_prec = new GeoPoint(0.0, 0.0);

            distance_live_rec = 0;
            for (int i = 0; i < cur.getCount(); i++) {

                GeoPoint gp = new GeoPoint(cur.getDouble(LAT), cur.getDouble(LON));

                if (i>0)
                    distance_live_rec += gp.distanceTo(gp_prec);

                lst_geoPoint_rec_live.add(gp);
                cur.moveToNext();
                gp_prec = gp;
            }
            cur.close();

            polyline_rec_live.setPoints(lst_geoPoint_rec_live);
        }catch(Exception e){
            gbl.myLog( "ERRORE in loadGeopoints [" + e.toString() + "]");
        }

    }

    // Classe utilizzata per rendere invisibile il Layout_Function
    private class MyRunnable implements Runnable{

        @Override
        public void run() {
            setVisbilityLayoutFunction(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                int message_id, small_icon_id;
                ConstraintLayout main_constrLayout = (ConstraintLayout) findViewById(R.id.mainConstraintLayout);

                if (isRecording) {
                    if (gbl.isPaused())
                        main_constrLayout.setBackgroundColor(Color.YELLOW);
                    else
                        main_constrLayout.setBackgroundColor(Color.RED);
                } else
                    main_constrLayout.setBackgroundColor(Color.WHITE);

                if (isRecording) {
                    if (gbl.isPaused()) {
                        message_id = R.string.str_reg_in_pausa;
                        small_icon_id = R.drawable.pause_dot;
                    } else {
                        message_id = R.string.str_reg_in_corso;
                        small_icon_id = R.drawable.rec_dot;
                    }
                } else {
                    if (isFollowing) {
                        message_id = R.string.str_app_in_following;
                        small_icon_id = R.drawable.following_dot;
                    } else {
                        if (is_wp_selected) {
                            message_id = R.string.str_app_in_wp_selected;
                            small_icon_id = R.drawable.wpselected_dot;
                        } else {
                            message_id = R.string.str_app_attiva;
                            small_icon_id = R.drawable.normal_dot;
                        }
                    }
                }

                bMap_largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                Intent resultIntent = new Intent(this, MainActivity.class);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );


                NotificationCompat.Builder mBuilder = null;
                mBuilder = new NotificationCompat.Builder(this)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setLargeIcon(bMap_largeIcon)
                        .setSmallIcon(small_icon_id)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(message_id))
                        .setContentIntent(resultPendingIntent);

                int mNotificationId = 001;
                mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());


            } catch (Exception e) {
                gbl.myLog("ERRORE Notification [" + e.toString() + "]");
            }
        }
    }


    /*
    * Crea la CATENA di TILE PROVIDER per il funzionamento ONLINE e OFFILINE della OSM
    */
    private MapTileProviderArray getMapTileProviderArray(){

        final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(this);
        // Create a custom tile source
        ITileSource tileSource;
        ITileSource onlineTileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;

        // Create a file cache modular provider
        final TileWriter tileWriter = new TileWriter();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/" + gbl.pref_map_offline;
        File myMapTileSource = new File(path);

/*
            ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            if (netInfo != null && !pref_map_is_offline){

                myMapTileSource = null;
            }
*/
        IArchiveFile[] archives = {ArchiveFileFactory.getArchiveFile(myMapTileSource)};

        // Se nellepreference ho selezionato una mappa --> mi metto in modalità OFFLINE e la uso
        // Se nelle preference NON ho selezionato una mappa --> mi metto in modalità ONLINE
        String source = "";
        if (gbl.pref_map_is_offline && myMapTileSource != null) {

            if (myMapTileSource.exists() && myMapTileSource.isFile()) {
                Set<String> tileSources = archives[0].getTileSources();
                if (!tileSources.isEmpty()) {
                    tv_offline_mode.setText(getString(R.string.str_offline_mode));
                    tv_offline_mode.setVisibility(TextView.VISIBLE);
                    source = tileSources.iterator().next();
                    tileSource = new XYTileSource(FileBasedTileSource.getSource(source).name(), 0, 19, 256, ".png", new String[]{FileBasedTileSource.getSource(source).name()});
                } else {
                    tv_offline_mode.setText(getString(R.string.str_offline_map_empty));
                    tv_offline_mode.setVisibility(TextView.VISIBLE);
                    tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
                }
            } else {
                tv_offline_mode.setText(getString(R.string.str_offline_map_not_exist));
                tv_offline_mode.setVisibility(TextView.VISIBLE);
                tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
            }
        }
        else {
            tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
            tv_offline_mode.setVisibility(TextView.INVISIBLE);
        }

        if(!gbl.pref_map_is_offline) {
            gbl.pref_map_offline_zoom_max = tileSource.getMaximumZoomLevel();
            gbl.pref_map_offline_zoom_min= tileSource.getMinimumZoomLevel();
        }

        MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(registerReceiver, tileSource);
        MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(registerReceiver, tileSource, archives);
        NetworkAvailabliltyCheck networkAvailablilityCheck = new NetworkAvailabliltyCheck(this);
        MapTileDownloader downloaderProvider = new MapTileDownloader(onlineTileSource, tileWriter, networkAvailablilityCheck);

        // Create a custom tile provider array with the custom tile source and the custom tile providers
        MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, registerReceiver, new MapTileModuleProviderBase[] { fileSystemProvider, fileArchiveProvider, downloaderProvider });

        return tileProviderArray;
    }

    private void settingValues(){
        animation_length_ms = getResources().getInteger(R.integer.animation_length_ms);
        threshold_track_select_m = getResources().getInteger(R.integer.threshold_track_select_m);
        threshold_zoom_show_markers_direction = getResources().getInteger(R.integer.threshold_zoom_show_markers_direction);
        gbl.refresh_Pref_default_zoom(this);
        gbl.setAppFolderPath(createAppFolder());
    }

    private String createAppFolder(){
        String path = Environment.getExternalStorageDirectory() + File.separator + "ExplorerAssistant";

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return path;
    }


}
