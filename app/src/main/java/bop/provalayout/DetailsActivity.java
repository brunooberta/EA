package bop.provalayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private String[] arr_itemsChecked = new String[]{};
    private int array_color[] = {Color.BLUE, Color.RED, Color.GREEN, Color.BLACK, Color.GRAY, Color.MAGENTA, Color.YELLOW, Color.WHITE};
    private SeekBar seekBar_dt;
    private TextView tv_length_0, tv_h_max_0, tv_h_min_0, tv_delta_h_pos_0, tv_delta_h_neg_0, tv_during_0, tv_name_0, tv_startdate_0, tv_endate_0;
    private TextView tv_length_1, tv_h_max_1, tv_h_min_1, tv_delta_h_pos_1, tv_delta_h_neg_1, tv_during_1, tv_name_1, tv_startdate_1, tv_endate_1;
    private Switch sw_visible_0;
    private Switch sw_visible_1;
    private String trackId;
    private GPSDatabase myDatabase;
    private int animationLength = 2500;
    private Animation iconAnimation;
    private String[] arr_trackId = new String[]{};
    private String selected_date="";
    private Global gbl = new Global();
    private int screen_width = 0;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDatabase.close();
    }

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iconAnimation = gbl.getToolbarAnimation(getApplicationContext());
        myDatabase = new GPSDatabase(this);
        myDatabase.open();

        this.setTitle("Charts & Details");

        setContentView(R.layout.activity_details);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            arr_trackId = extras.getStringArray("arr_trackId");
            arr_itemsChecked = extras.getStringArray("arr_itemsChecked");
            selected_date = extras.getString("selected_date");

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;

       // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    // Consente di spostarsi nella vista con la mappa
    public void startMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            Log.w("MY_CHECK","startMainActivity ERRORE["+ e.toString() +"]" );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_details, menu);

            FontManager.manageMenuItem(menu, screen_width,getBaseContext(),this);

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
                case R.id.menu_det_item_go_to_map:
                    startMainActivity();
                    return true;

                case R.id.menu_det_item_go_to_share:
                    goToShare();
                    return true;

                case R.id.menu_det_item_go_to_tracklist:
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

            View rootView=null;
            TextView textView;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_details_time_charts, container, false);
                    ((DetailsActivity)getActivity()).initializeTimeCharts(rootView);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_details_metric_charts, container, false);
                    ((DetailsActivity)getActivity()).initializeMetricCharts(rootView);
                    break;
                case 3:
                    //rootView = inflater.inflate(R.layout.fragment_details, container, false);
                    rootView = inflater.inflate(R.layout.fragment_details_data, container, false);
                    ((DetailsActivity)getActivity()).initializeDetails(rootView);
                    break;
            }

            return rootView;
        }



    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Time Charts";
                case 1:
                    return "Metric Charts";
                case 2:
                    return "Details";
            }
            return null;
        }
    }

    // *********** INIZIO GESTIONE TAB DETTAGLI e GRAFICI

    // Classe utile per ottenere le etichette dell'asse X (il tempo) formattato come hh:mm
    public class TimeAxisValueFormatter implements IAxisValueFormatter {
        private DateFormat mFormat;

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            try {

                double h = 0, m = 0, s = 0;
                h = Math.floor(value / 3600);
                m = Math.floor((value - h * 3600) / 60);
                return String.format("%02.0f:%02.0f", h, m);
            }
            catch(Exception e){
                Log.w("MY_CHECK", "MyXAxisValueFormatter.getFormattedValue --> ERRORE["+e.toString()+"]");
                return null;
            }
        }
    }

    // Classe utile per ottenere le etichette dell'asse Y (distanza/altezza) formattato come "VALORE m"
    public class MetricAxisValueFormatter implements IAxisValueFormatter {
        private DateFormat mFormat;

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            try {
                String retValue = "";

                retValue =String.format("%.0f" + "m", value) ;

                return retValue;
            }
            catch(Exception e){
                Log.w("MY_CHECK", "MyYAxisValueFormatter.getFormattedValue --> ERRORE["+e.toString()+"]");
                return null;
            }
        }
    }

    public void initializeDetails(View rootView) {
        try {

            for (int i=0; i<arr_itemsChecked.length;i++) {

                trackId = arr_itemsChecked[i];

                Track_OSM t = Global.getTrackOsmCollection().getTrackFromCollectionByTrackId(trackId);

                //Track_OSM t = new Track_OSM(trackId, myDatabase);

                if ( i == 0) {

                    tv_name_0 = (TextView) rootView.findViewById(R.id.tv_name_0);
                    tv_name_0.setText("Name: " + t.getTrackName());

                    tv_startdate_0 = (TextView) rootView.findViewById(R.id.tv_startdate_0);
                    tv_startdate_0.setText(t.getStartDate());

                    tv_endate_0 = (TextView) rootView.findViewById(R.id.tv_enddate_0);
                    tv_endate_0.setText(t.getEndDate());

                    tv_length_0 = (TextView) rootView.findViewById(R.id.tv_length_0);
                    tv_length_0.setText(t.getLength() + " m");

                    tv_h_max_0 = (TextView) rootView.findViewById(R.id.tv_h_max_0);
                    tv_h_max_0.setText(t.getHmax() + " m");

                    tv_h_min_0 = (TextView) rootView.findViewById(R.id.tv_h_min_0);
                    tv_h_min_0.setText(t.getHmin() + " m");

                    tv_delta_h_pos_0 = (TextView) rootView.findViewById(R.id.tv_delta_h_pos_0);
                    tv_delta_h_pos_0.setText(t.getDeltaHPos() + " m");

                    tv_delta_h_neg_0 = (TextView) rootView.findViewById(R.id.tv_delta_h_neg_0);
                    tv_delta_h_neg_0.setText(t.getDeltaHNeg() + " m");

                    tv_during_0 = (TextView) rootView.findViewById(R.id.tv_during_0);
                    tv_during_0.setText(gbl.formatTime(t.getDuring()));

                    sw_visible_0 = (Switch) rootView.findViewById(R.id.sw_visible_0);
                    sw_visible_0.setChecked(t.isVisible());

                    sw_visible_0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            String visible = "0";
                            if (isChecked) visible = "1";

                            String[] setFields = new String[]{"visible"};

                            String[] setValues = new String[]{visible};
                            String whereFields = "trackID=?";
                            String[] whereValues = new String[]{arr_itemsChecked[0]};

                            myDatabase.upd_TrackSaved_Rows(setFields, setValues, whereFields, whereValues);
                        }
                    });
                } else {

                    GridLayout gl = (GridLayout)rootView.findViewById(R.id.gridLayout);
                    for( int j = 0; j < gl.getChildCount(); j++ ){

                        if (gl.getChildAt(j).getVisibility() == TextView.INVISIBLE)
                            gl.getChildAt(j).setVisibility(TextView.VISIBLE);

                    }


                    tv_name_1 = (TextView) rootView.findViewById(R.id.tv_name_1);
                    tv_name_1.setText("Name: " + t.getTrackName());

                    tv_startdate_1 = (TextView) rootView.findViewById(R.id.tv_startdate_1);
                    tv_startdate_1.setText(t.getStartDate());

                    tv_endate_1 = (TextView) rootView.findViewById(R.id.tv_enddate_1);
                    tv_endate_1.setText(t.getEndDate());

                    tv_length_1 = (TextView) rootView.findViewById(R.id.tv_length_1);
                    tv_length_1.setText(t.getLength() + " m");

                    tv_h_max_1 = (TextView) rootView.findViewById(R.id.tv_h_max_1);
                    tv_h_max_1.setText(t.getHmax() + " m");

                    tv_h_min_1 = (TextView) rootView.findViewById(R.id.tv_h_min_1);
                    tv_h_min_1.setText(t.getHmin() + " m");

                    tv_delta_h_pos_1 = (TextView) rootView.findViewById(R.id.tv_delta_h_pos_1);
                    tv_delta_h_pos_1.setText(t.getDeltaHPos() + " m");

                    tv_delta_h_neg_1 = (TextView) rootView.findViewById(R.id.tv_delta_h_neg_1);
                    tv_delta_h_neg_1.setText(t.getDeltaHNeg() + " m");

                    tv_during_1 = (TextView) rootView.findViewById(R.id.tv_during_1);
                    tv_during_1.setText(gbl.formatTime(t.getDuring()));

                    sw_visible_1 = (Switch) rootView.findViewById(R.id.sw_visible_1);
                    sw_visible_1.setChecked(t.isVisible());

                    sw_visible_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            String visible = "0";
                            if (isChecked) visible = "1";
                            String[] setFields = new String[]{"visible"};
                            String[] setValues = new String[]{visible};
                            String whereFields = "trackID=?";
                            String[] whereValues = new String[]{arr_itemsChecked[1]};

                            myDatabase.upd_TrackSaved_Rows(setFields, setValues, whereFields, whereValues);
                        }
                    });
                }
            }
        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in initializeDetails [" + e.toString() + "]");
        }
    }

    public void initializeTimeCharts(View rootView) {
        try {
            LineDataSet dataSet = null,dataSet_alt=null;// add entries to dataset
            final LineChart chart_distance = (LineChart) rootView.findViewById(R.id.chart_distance);
            final LineChart chart_height = (LineChart) rootView.findViewById(R.id.chart_height);
            seekBar_dt = (SeekBar) rootView.findViewById(R.id.seekBar_dt);
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>(),dataSets_alt = new ArrayList<ILineDataSet>();

            DataPoint[] arr_dp = null, arr_dp_alt = null;
            List<Entry> entries =null,entries_alt =null,entries_hd =null;
            float x = 0, y = 0;

            if (arr_itemsChecked.length>1) {
                if (seekBar_dt != null )
                    seekBar_dt.setVisibility(SeekBar.INVISIBLE);
            }

            String[] retTracksName = new String[arr_itemsChecked.length];
            retTracksName = getTracksName();

            //Prendo i dati da inserire nel grafico dal DB
            // j è l'indice che punto una delle tracce che ho selezionato nella View delle tracce salvate
            for (int j = 0; j < arr_itemsChecked.length; j++) {
                x = 0;
                y = 0;

                ArrayList<DataPoint[]> lst_DP_Array = new ArrayList<DataPoint[]>();
                lst_DP_Array = getDataPoints(arr_itemsChecked[j]);

                // Raccolgo i dati relativi al grafico DISTANZA / TEMPO
                entries = new ArrayList<Entry>();

                arr_dp=lst_DP_Array.get(0);
                for (int i = 0; i < arr_dp.length; i++) {
                    x = (float) arr_dp[i].getX();
                    y = (float) arr_dp[i].getY();
                    entries.add(new Entry(x, y));
                }
                dataSet = new LineDataSet(entries, retTracksName[j]);
                dataSet.setDrawCircles(false); // Disabilita gli indicatori sul grafico
                dataSet.setColor(array_color[j]);
                dataSets.add(dataSet);

                // Raccolgo i dati relativi al grafico ALTEZZA / TEMPO
                entries_alt = new ArrayList<Entry>();
                arr_dp_alt =  new DataPoint[]{};
                arr_dp_alt = lst_DP_Array.get(1);
                for (int i = 0; i < arr_dp_alt.length; i++) {
                    x = (float) arr_dp_alt[i].getX();
                    y = (float) arr_dp_alt[i].getY();
                    entries_alt.add(new Entry(x, y));
                }
                dataSet_alt = new LineDataSet(entries_alt, retTracksName[j]);
                dataSet_alt.setDrawCircles(false);
                dataSet_alt.setColor(array_color[j]);
                dataSets_alt.add(dataSet_alt);
            }

            Description title_chart = new Description();
            title_chart.setText("");

            // Creo il grafico della DISTANZA in funzione del TEMPO
            LineData lineData = new LineData(dataSets);
            chart_distance.setData(lineData);

            XAxis xAxis_distance = chart_distance.getXAxis();
            xAxis_distance.setValueFormatter(new DetailsActivity.TimeAxisValueFormatter());
            YAxis yAxis_L_distance = chart_distance.getAxisLeft();
            YAxis yAxis_R_distance = chart_distance.getAxisRight();
            yAxis_R_distance.setDrawLabels(false);
            yAxis_L_distance.setValueFormatter(new DetailsActivity.MetricAxisValueFormatter());
            chart_distance.setTouchEnabled(true);
            chart_distance.setMarker(new DetailsActivity.CustomMarkerView(getApplicationContext(),R.layout.fragment_details_time_charts,"DIST-TEMPO"));
            chart_distance.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    try {
                        chart_height.highlightValue(e.getX(), h.getDataSetIndex(), false);
                    }
                    catch(Exception ex){Log.w("MY_CHECK","onValueSelected -> ERRORE["+ex.toString()+"]");}
                }
                @Override
                public void onNothingSelected() {

                }
            });
            chart_distance.setDescription(title_chart);
            chart_distance.invalidate(); // refresh

            seekBar_dt.setMax((int)xAxis_distance.getAxisMaximum());

            seekBar_dt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    chart_distance.highlightValue(progress,0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // Creo il grafico della ALTEZZA in funzione del TEMPO
            LineData lineData_alt = new LineData(dataSets_alt);
            chart_height.setData(lineData_alt);

            XAxis xAxis_height = chart_height.getXAxis();
            xAxis_height.setValueFormatter(new DetailsActivity.TimeAxisValueFormatter());
            YAxis yAxis_L_height = chart_height.getAxisLeft();
            YAxis yAxis_R_height = chart_height.getAxisRight();
            yAxis_L_height.setValueFormatter(new DetailsActivity.MetricAxisValueFormatter());
            yAxis_R_height.setDrawLabels(false);
            chart_height.setMarker(new DetailsActivity.CustomMarkerView(getApplicationContext(),R.layout.fragment_details_time_charts,"ALT-TEMPO"));
            chart_height.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    try {
                        chart_distance.highlightValue(e.getX(), h.getDataSetIndex(), false);
                    } catch (Exception ex) {Log.w("MY_CHECK", "onValueSelected -> ERRORE[" + ex.toString() + "]");}
                }
                @Override
                public void onNothingSelected() {

                }
            });
            chart_height.setDescription(title_chart);
            chart_height.invalidate(); // refresh

        }
        catch(Exception e){Log.w("MY_CHECK", "onCreate --> ERRORE["+e.toString()+"]");}
    }

    public void initializeMetricCharts(View rootView) {
        try {
            LineDataSet dataSet = null,dataSet_alt=null,dataSet_hd=null; // add entries to dataset

            final LineChart chart_hd = (LineChart) rootView.findViewById(R.id.chart_d_h);

            seekBar_dt = (SeekBar) rootView.findViewById(R.id.seekBar_hd);
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>(),dataSets_alt = new ArrayList<ILineDataSet>(),dataSets_hd = new ArrayList<ILineDataSet>();

            DataPoint[] arr_dp = null, arr_dp_alt = null;
            List<Entry> entries =null,entries_alt =null,entries_hd =null;
            float x = 0, y = 0;

            //arr_itemsChecked = myDatabase.getVisibleTrackId();

            if (arr_itemsChecked.length>1) {
                if (seekBar_dt != null )
                    seekBar_dt.setVisibility(SeekBar.INVISIBLE);
            }

            String[] retTracksName = new String[arr_itemsChecked.length];
            retTracksName = getTracksName();

            //Prendo i dati da inserire nel grafico dal DB
            // j è l'indice che punto una delle tracce che ho selezionato nella View delle tracce salvate
            for (int j = 0; j < arr_itemsChecked.length; j++) {
                x = 0;
                y = 0;

                ArrayList<DataPoint[]> lst_DP_Array = new ArrayList<DataPoint[]>();
                lst_DP_Array = getDataPoints(arr_itemsChecked[j]);

                // Raccolgo i dati relativi al grafico DISTANZA / TEMPO
                entries = new ArrayList<Entry>();
                entries_hd = new ArrayList<Entry>();
                arr_dp = new DataPoint[]{};
                arr_dp=lst_DP_Array.get(0);
                for (int i = 0; i < arr_dp.length; i++) {
                    x = (float) arr_dp[i].getX();
                    y = (float) arr_dp[i].getY();
                    entries.add(new Entry(x, y));
                }
                dataSet = new LineDataSet(entries, retTracksName[j]);
                dataSet.setDrawCircles(false); // Disabilita gli indicatori sul grafico
                dataSet.setColor(array_color[j]);
                dataSets.add(dataSet);

                // Raccolgo i dati relativi al grafico ALTEZZA / TEMPO
                entries_alt = new ArrayList<Entry>();
                arr_dp_alt = lst_DP_Array.get(1);
                for (int i = 0; i < arr_dp_alt.length; i++) {
                    x = (float) arr_dp_alt[i].getX();
                    y = (float) arr_dp_alt[i].getY();
                    entries_alt.add(new Entry(x, y));
                }
                dataSet_alt = new LineDataSet(entries_alt, retTracksName[j]);
                dataSet_alt.setDrawCircles(false);
                dataSet_alt.setColor(array_color[j]);
                dataSets_alt.add(dataSet_alt);

                // Raccolgo i dati relativi al grafico ALTEZZA / DISTANZA
                for (int i = 0; i < arr_dp_alt.length; i++) {
                    x = entries.get(i).getY();
                    y = entries_alt.get(i).getY();
                    entries_hd.add(new Entry(x, y));
                }

                dataSet_hd = new LineDataSet(entries_hd, retTracksName[j]);
                dataSet_hd.setDrawCircles(false); // Disabilita gli indicatori sul grafico
                dataSet_hd.setColor(array_color[j]);
                dataSets_hd.add(dataSet_hd);
            }

            Description title_chart = new Description();
            title_chart.setText("");

            // Creo il grafico della ALTEZZA in funzione della DISTANZA
            LineData lineData_hd = new LineData(dataSets_hd);
            chart_hd.setData(lineData_hd);
            XAxis xAxis_hd = chart_hd.getXAxis();
            xAxis_hd.setValueFormatter(new DetailsActivity.MetricAxisValueFormatter());
            YAxis yAxis_L_hd = chart_hd.getAxisLeft();
            YAxis yAxis_R_hd = chart_hd.getAxisRight();
            yAxis_L_hd.setValueFormatter(new DetailsActivity.MetricAxisValueFormatter());
            yAxis_R_hd.setDrawLabels(false);
            chart_hd.setMarker(new DetailsActivity.CustomMarkerView(getApplicationContext(),R.layout.fragment_details_time_charts,"ALT-DIST"));
            chart_hd.setDescription(title_chart);
            chart_hd.invalidate(); // refresh

            seekBar_dt.setMax((int)xAxis_hd.getAxisMaximum());

            seekBar_dt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    chart_hd.highlightValue(progress,0);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }
        catch(Exception e){Log.w("MY_CHECK", "onCreate --> ERRORE["+e.toString()+"]");}
    }

    private String[] getTracksName() {
        try {

            String[] retTracksName = new String[arr_itemsChecked.length];

            for (int i = 0; i < arr_itemsChecked.length; i++) {
                retTracksName[i]= myDatabase.getTrackName(arr_itemsChecked[i]);
            }

            return retTracksName;
        } catch (Exception e) {
            Log.w("MY_CHECK", "getTracksName --> ERRORE[" + e.toString() + "]");
            return null;
        }
    }

    //Recupero 2 array di DataPoint, uno per la Distanza e uno per l'Altezza della traccia passat in input
    private ArrayList<DataPoint[]> getDataPoints(String trackId) {
        try {
            int LAT = 2, LON = 3, ALT = 4, TIME = 5;
            double cur_lat = 0, cur_lon = 0, old_lat = -1, old_lon = -1,alt=0;
            long cur_time = 0, old_time = 0, deltaT = 0;
            float distance = 0, incremental_D = 0, incremental_T = 0;

            Cursor cursor = myDatabase.choiceData("track", "trackId=?", new String[]{trackId});
            cursor.moveToFirst();

            DataPoint[] arr_dp = new DataPoint[cursor.getCount()];
            DataPoint[] arr_dp_alt = new DataPoint[cursor.getCount()];

            for (int i = 0; i < cursor.getCount(); i++) {
                cur_lat = cursor.getDouble(LAT);
                cur_lon = cursor.getDouble(LON);
                alt = cursor.getDouble(ALT);
                cur_time = cursor.getLong(TIME);

                if (old_lat == -1) {
                    old_lat = cur_lat;
                    old_lon = cur_lon;
                    old_time = cur_time;
                }

                Location locationA = new Location("point A");
                Location locationB = new Location("point B");

                locationA.setLatitude(cur_lat);
                locationA.setLongitude(cur_lon);
                locationB.setLatitude(old_lat);
                locationB.setLongitude(old_lon);

                distance = locationA.distanceTo(locationB);
                deltaT = Math.round(cur_time - old_time);

                incremental_T += deltaT;
                incremental_D += distance;

                DataPoint dp = new DataPoint( incremental_T,Math.round(incremental_D));
                DataPoint dp_alt = new DataPoint( incremental_T,Math.round(alt));
                arr_dp[i] = dp;
                arr_dp_alt[i] = dp_alt;

                old_lat = cur_lat;
                old_lon = cur_lon;
                old_time = cur_time;

                cursor.moveToNext();
            }

            cursor.close();

            ArrayList<DataPoint[]> lst_dp_arr = new ArrayList<DataPoint[]>();

            lst_dp_arr.add(arr_dp);
            lst_dp_arr.add(arr_dp_alt);

            return lst_dp_arr;

        } catch (Exception e) {
            Log.w("MY_CHECK","ERRORE in getDistance ["+e.toString()+"]");
            return null;
        }
    }

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
    // *********** FINE GESTIONE TAB GRAFICI

    public class CustomMarkerView extends MarkerView {

        private String mtypeOfChart = "";
        private TextView tv_content;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
        }
        public CustomMarkerView(Context context, int layoutResource, String typeOfChart) {
            super(context, layoutResource);
            tv_content = (TextView) findViewById(R.id.tv_content);
            tv_content.setTextSize(15);
            tv_content.setTextColor(Color.BLUE);
            mtypeOfChart = typeOfChart;
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            int uiScreenWidth = getResources().getDisplayMetrics().widthPixels;
            // Check marker position and update offsets.
            int w = getWidth();
            float x_right = posX + w;
            if(x_right > uiScreenWidth) posX = posX-w;
            // translate to the correct position and draw
            canvas.translate(posX, posY);
            draw(canvas);
            canvas.translate(-posX, -posY);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            super.refreshContent(e, highlight);
            try {
                String txt = "", time="";
                time = gbl.formatTime(e.getX());

                switch(mtypeOfChart){
                    case "DIST-TEMPO":
                        txt = String.format("d=%02.0fm t=" +time, e.getY());
                        tv_content.setText(txt);
                        break;
                    case "ALT-TEMPO":
                        txt = String.format("h=%02.0fm t=" + time, e.getY());
                        tv_content.setText(txt);
                        break;
                    case "ALT-DIST":
                        txt = String.format("h=%02.0fm d=%02.0fm", e.getY(), e.getX());
                        tv_content.setText(txt);
                        break;
                    default:
                        tv_content.setText("");
                        break;

                }
                if (arr_itemsChecked.length == 1) {
                    seekBar_dt.setProgress((int) e.getX());
                    seekBar_dt.invalidate();
                }
            }

            catch(Exception ex){Log.w("MY_CHECK", "refreshContent --> ERRORE["+ex.toString()+"]");};
        }

    }

    private void goToShare() {
        try {

            //Track_OSM t = new Track_OSM(arr_itemsChecked[0], myDatabase);

            Track_OSM t = Global.getTrackOsmCollection().getTrackFromCollectionByTrackId(arr_itemsChecked[0]);

            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra("Distance", t.getLength() + " m");
            intent.putExtra("Time", gbl.formatTime(t.getDuring()));
            intent.putExtra("D_plus", t.getDeltaHPos() + " m");
            intent.putExtra("D_minus", t.getDeltaHNeg() + " m");
            intent.putExtra("trackId", t.getTrackId());
            intent.putExtra("trackName", t.getTrackName());
            intent.putExtra("arr_trackId",arr_trackId);
            intent.putExtra("selected_date",selected_date);

            startActivity(intent);
            //finish();

        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goToShare [" + e.toString() + "]");
        }
    }
}
