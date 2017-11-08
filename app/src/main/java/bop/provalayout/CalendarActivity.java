package bop.provalayout;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {
    private GPSDatabase myDatabase;
    private Animation toolBarAnimation;
    private Global gbl = new Global();

    private static final int FILL_LARGE_INDICATOR = 1;
    private static final int NO_FILL_LARGE_INDICATOR = 2;
    private static final int SMALL_INDICATOR = 3;
    private static int TRACK_EVENT_COLOR =0,DATE_EVENT_COLOR=0;
    private int screen_width =0;
    private int EVENT_DATA_SELECTIONDATE = -1;
    private Date date_start = null, date_end = null;
    private List<Event> lst_track_event = new ArrayList<Event>();
    private EditText et_start_date, et_end_date;
    private TextInputLayout til_start_date, til_end_date;
    private String dateFormat = "dd/MM/yyyy", not_selected = " --/--/---- ";
    @Override
    public void onBackPressed() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_calendar);

            TRACK_EVENT_COLOR = getColor(R.color.calendar_track_ev_indicator);
            DATE_EVENT_COLOR = getColor(R.color.calendar_date_ev_indicator);

            et_start_date = (EditText) findViewById(R.id.et_cal_start);
            til_start_date = (TextInputLayout) findViewById(R.id.til_cal_start);
            et_end_date = (EditText) findViewById(R.id.et_cal_end);
            til_end_date = (TextInputLayout) findViewById(R.id.til_cal_end);

            et_start_date.setInputType(InputType.TYPE_NULL);
            et_start_date.setEnabled(false);
            et_end_date.setInputType(InputType.TYPE_NULL);
            et_end_date.setEnabled(false);

            et_start_date.setText(not_selected);
            et_end_date.setText(not_selected);

            myDatabase = new GPSDatabase(this);
            myDatabase.open();

            Toolbar toolbar = (Toolbar) findViewById(R.id.tb_calendar_activity);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screen_width = size.x;

            final CompactCalendarView compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
            compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
            compactCalendarView.displayOtherMonthDays(true);
            compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
            compactCalendarView.setEventIndicatorStyle(NO_FILL_LARGE_INDICATOR);
            final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());

            final TextView tv_current_month = (TextView)  findViewById(R.id.tv_current_month);

            String curr_month = dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth());
            tv_current_month.setText(curr_month);

            // Aggiunge al calendario tutti gli eventi di tipo TRACK
            addTrackEvents(compactCalendarView);

            compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {
                    try{
                    long days = 0; // numero di giorni compreso tra date_start e date_end

                    if(date_start == null)
                        date_start = dateClicked;
                    else{
                        if(date_start.getTime() ==  dateClicked.getTime()){
                            /*cquando clicco su una data già contrassegnata come date_start la deseleziono */
                            // Elimino tutti gli eventi dal calendari
                            compactCalendarView.removeAllEvents();
                            // Aggiungo gli eventi TRACCE
                            compactCalendarView.addEvents(lst_track_event);

                            //resetto date
                            date_start = null;
                            date_end   = null;
                        }
                        else {
                            if (dateClicked.getTime() > date_start.getTime())
                                date_end = dateClicked;
                            else {
                                date_end = date_start;
                                date_start = dateClicked;
                            }
                        }
                    }


                    if(date_end != null) {
                        long diff = date_end.getTime() - date_start.getTime();
                        days = diff / (24 * 60 * 60 * 1000);

                        Calendar c = Calendar.getInstance();

                        // Elimino tutti gli eventi dal calendari
                        compactCalendarView.removeAllEvents();
                        compactCalendarView.addEvents(lst_track_event);

                        // Aggiungo gli eventi SELEZIONE DATE
                        for (int j = 0; j <= days; j++) {
                            c.setTime(date_start);
                            c.add(Calendar.DATE, j);
                            Event ev = new Event(DATE_EVENT_COLOR, c.getTimeInMillis(), EVENT_DATA_SELECTIONDATE);
                            compactCalendarView.addEvent(ev, true);

                        }
                    }
                    if (date_end!=null) {
                        String str_date_end = getDate(date_end.getTime(), dateFormat);
                        if(str_date_end.length()>0)
                            et_end_date.setText(str_date_end);
                        else
                            et_end_date.setText(not_selected);
                    } else
                        et_end_date.setText(not_selected);

                    if (date_start!=null) {
                        String str_date_start = getDate(date_start.getTime(), dateFormat);
                        if(str_date_start.length()>0)
                            et_start_date.setText(str_date_start);
                        else
                            et_start_date.setText(not_selected);
                    }else
                        et_start_date.setText(not_selected);

                    } catch (Exception e) {
                        gbl.myLog("ERRORE in CalendarActivity.onDayClick [" + e.toString() + "]");
                    }
                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth) {
                    SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
                    String curr_month = dateFormatForMonth.format(firstDayOfNewMonth);
                    tv_current_month.setText(curr_month);
                }
            });


         }
        catch (Exception e) {
            gbl.myLog("ERRORE in CalendarActivity.onCreate [" + e.toString() + "]");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDatabase.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_calendar, menu);

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

            switch (item.getItemId()) {

                case R.id.menu_cal_go_to_tracklist:
                    List<Event> lst = new ArrayList<Event>();

                    lst = getEvents(date_start,date_end);

                    if(lst.size()>0) {
                        String [] arr_trackId = new String[lst.size()];
                        int i=0;
                        for(Event ev:lst){
                            arr_trackId[i++] = (String)ev.getData();
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                        String str_date_start = sdf.format(date_start), str_date_end="";
                        if(date_end != null )
                            str_date_end = sdf.format(date_end);
                        Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                        intent.putExtra("arr_trackId",arr_trackId);
                        intent.putExtra("date_start", str_date_start);
                        intent.putExtra("date_end", str_date_end);
                        startActivity(intent);
                    }else{
                        Toast.makeText(this.getApplicationContext(),R.string.nothig_to_show,Toast.LENGTH_LONG).show();
                    }
                    return true;
                case R.id.menu_cal_item_go_to_map:
                    startMainActivity();
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

    // Dal DB prende tutti gli eventi di tipo TRACK
    private ArrayList<TrackEvent> getEvents() throws ParseException {
        return myDatabase.getEvents();
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    // Dalla lista completa di tutti gli eventi di tipo TRACK prende gli eenti compresi tra le date in input
    private List<Event> getEvents(Date ds, Date de) throws ParseException {

        List<Event> lst = new ArrayList<Event>();

        if(ds!=null) {
            for (Event e : lst_track_event) {

                if (de == null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                    String str_date_start = sdf.format(ds);

                    if (getDate(e.getTimeInMillis(), dateFormat).equals(getDate(ds.getTime(), dateFormat)))
                        lst.add(e);
                } else {
                    if (e.getTimeInMillis() >= ds.getTime() && e.getTimeInMillis() <= de.getTime())
                        lst.add(e);
                }
            }
        }

        return lst;
    }

    // Aggiunge al calendario gli eventi di tipo TRACK
    private void addTrackEvents(CompactCalendarView v) throws ParseException {

        ArrayList<TrackEvent> lst_trackEvent = getEvents();

        for(TrackEvent te:lst_trackEvent){
            Event ev = new Event(TRACK_EVENT_COLOR, te.get_date_ms(), te.get_trackId());
            lst_track_event.add(ev);
        }

        v.addEvents(lst_track_event);
    }

    // Consente di spostarsi nella vista con la mappa
    public void startMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        catch(Exception e){

            gbl.myLog("startMainActivity ERRORE["+ e.toString() +"]" );
        }
    }

}
