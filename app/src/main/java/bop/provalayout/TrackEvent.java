package bop.provalayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class  TrackEvent {

    private String _trackId="";
    private Date _date = new Date();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public TrackEvent(String trackId, String date) throws ParseException {
        _trackId = trackId;
        _date = dateFormat.parse(date);
    };

    public String get_trackId() {return _trackId;}
    public Date get_date() {return _date;}
    public long get_date_ms() {return _date.getTime();}
}