package bop.ea_free;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.TextView;

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
    private static int EVENT_COLOR=0;
    private int screen_width =0;

    @Override
    public void onBackPressed() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_calendar);

            EVENT_COLOR = getColor(R.color.calendar_eventindicator);

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

            compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {

                    List<Event> events = compactCalendarView.getEvents(dateClicked.getTime());
                    String [] arr_trackId = new String[events.size()];
                    int i=0;
                    for(Event e: events){
                        arr_trackId[i++] = (String)e.getData();
                    }
                    String date="";
                    SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
                    date = sdf.format(dateClicked);
                    if(events.size()>0){
                        Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                        intent.putExtra("arr_trackId",arr_trackId);
                        intent.putExtra("selected_date", date);
                        startActivity(intent);
                    }

                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth) {
                    SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
                    String curr_month = dateFormatForMonth.format(firstDayOfNewMonth);
                    tv_current_month.setText(curr_month);
                }
            });

            ArrayList<TrackEvent> lst_trackEvent = getEvents();

            for(TrackEvent te:lst_trackEvent){
                Event ev = new Event(EVENT_COLOR, te.get_date_ms(), te.get_trackId());
                compactCalendarView.addEvent(ev, true);

            }

         }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in CalendarActivity.onCreate [" + e.toString() + "]");
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

    private ArrayList<TrackEvent> getEvents() throws ParseException {
        return myDatabase.getEvents();
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

}
