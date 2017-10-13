package bop.ea_free;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Adm on 13/06/2017.
 */

public class ExportToGpxFormat {

    private FileOutputStream fileos;
    private  XmlSerializer xmlSerializer;
    private StringWriter writer;
    private GPSDatabase my_db;
    private String myTrackId;

    public ExportToGpxFormat(GPSDatabase db, String trackId) {

        try {
            my_db = db;
            myTrackId = trackId;
            String name = getTrackName();
            String path = Environment.getExternalStorageDirectory().getPath() + "/Download/" + name + ".gpx";

            fileos = new  FileOutputStream(new File(path));

            xmlSerializer = Xml.newSerializer();
            writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "gpx");
            xmlSerializer.attribute(null,"version","1.1");
            xmlSerializer.attribute(null,"creator","BOP TRACKER");
            xmlSerializer.attribute(null,"xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd");
            xmlSerializer.attribute(null,"xmlns", "http://www.topografix.com/GPX/1/1");
            xmlSerializer.attribute(null,"xmlns:gpxtpx","http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
            xmlSerializer.attribute(null,"xmlns:gpxx","http://www.garmin.com/xmlschemas/GpxExtensions/v3");
            xmlSerializer.attribute(null,"xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");

            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(name);
            xmlSerializer.endTag(null, "name");

            build_wpt();

            build_trkseg();

            xmlSerializer.startTag(null, "trk");

            xmlSerializer.endTag(null, "trk");

            xmlSerializer.endTag(null, "gpx");

            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();

            fileos.write(dataWrite.getBytes());
            fileos.close();
/*
            path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
            final File file = new File(path, "userData2.xml");
            Log.w("MY_CHECK", "ExportToGpxFormat file.toString()[" + file.toString() + "]");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();
            NodeList nodelist = elementRoot.getElementsByTagName("userName");
            Node node = nodelist.item(0);

            Log.w("MY_CHECK", "ExportToGpxFormat elementRoot[" + node.getTextContent().toString() + "]");
*/
        }
        catch (FileNotFoundException e) {
            Log.w("MY_CHECK", "FileNotFoundException in ExportToGpxFormat [" + e.toString() + "]");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            Log.w("MY_CHECK", "IllegalArgumentException in ExportToGpxFormat [" + e.toString() + "]");
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            Log.w("MY_CHECK", "IllegalStateException in ExportToGpxFormat [" + e.toString() + "]");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.w("MY_CHECK", "IOException in ExportToGpxFormat [" + e.toString() + "]");
            e.printStackTrace();
        } catch (ParseException e) {
            Log.w("MY_CHECK", "ParseException in ExportToGpxFormat [" + e.toString() + "]");
            e.printStackTrace();
        }
    }

    private String getTrackName(){
        int NAME=0;
        String name = "";
        Cursor cur_track_data = my_db.get_track_data(myTrackId);
        cur_track_data.moveToFirst();

        if(cur_track_data.getCount()>0)
            name = cur_track_data.getString(NAME);
        cur_track_data.close();
        return name;
    }

    private void build_trkseg() throws IOException, ParseException {

        int LAT=1,LON=2, ELE=3, TIME=4, STARTDATE=5;

        Cursor cur_track_data = my_db.get_track_data(myTrackId);
        cur_track_data.moveToFirst();

        xmlSerializer.startTag(null, "trkseg");

        for(int i=0;i<cur_track_data.getCount();i++) {

            xmlSerializer.startTag(null, "trkpt");

            xmlSerializer.attribute(null, "lon", cur_track_data.getString(LON));
            xmlSerializer.attribute(null, "lat",  cur_track_data.getString(LAT));

            xmlSerializer.startTag(null, "ele");
            xmlSerializer.text(cur_track_data.getString(ELE));



            xmlSerializer.endTag(null, "ele");

            xmlSerializer.startTag(null, "time");
            //Log.w("MY_CHECK"," STARTDATE ["+cur_track_data.getString(STARTDATE)+"] sd["+getDate(cur_track_data.getLong(TIME), "yyyy-MM-dd'T'HH:mm:ss")+"] TIME["+cur_track_data.getLong(TIME)+"]");
            xmlSerializer.text(getDate(cur_track_data.getLong(TIME), "yyyy-MM-dd'T'HH:mm:ss"));
            xmlSerializer.endTag(null, "time");

            xmlSerializer.endTag(null, "trkpt");


            cur_track_data.moveToNext();
        }

        xmlSerializer.endTag(null, "trkseg");
        cur_track_data.close();
    }

    public String getDate(long seconds, String dateFormat){
        try {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            String dateString = formatter.format(new Date(seconds*1000));
            return dateString + "Z";
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in getDate ["+e.toString()+"]");
            return "";
        }
    }


    private void build_wpt() throws IOException {
        try {
        int  WP_NAME = 0, WP_LAT=1,WP_LON=2, WP_ALT=3,WP_CMT=4,WP_DESC=5,WP_SYM=6;

        Cursor cur_wp_data = my_db.get_waypoint_data_for_track(myTrackId);
        cur_wp_data.moveToFirst();



        for(int i=0;i<cur_wp_data.getCount();i++){

            xmlSerializer.startTag(null, "wpt");
            xmlSerializer.attribute(null,"lon",cur_wp_data.getString(WP_LON));
            xmlSerializer.attribute(null,"lat",cur_wp_data.getString(WP_LAT));

            xmlSerializer.startTag(null, "ele");
            xmlSerializer.text(cur_wp_data.getString(WP_ALT));
            xmlSerializer.endTag(null, "ele");
/*
            xmlSerializer.startTag(null, "time");
            xmlSerializer.text(time);
            xmlSerializer.endTag(null, "time");
*/
            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(cur_wp_data.getString(WP_NAME));
            xmlSerializer.endTag(null, "name");

            xmlSerializer.startTag(null, "cmt");
            xmlSerializer.text(cur_wp_data.getString(WP_CMT));
            xmlSerializer.endTag(null, "cmt");

            xmlSerializer.startTag(null, "desc");
            xmlSerializer.text(cur_wp_data.getString(WP_DESC));
            xmlSerializer.endTag(null, "desc");

            xmlSerializer.startTag(null, "sym");
            xmlSerializer.text(cur_wp_data.getString(WP_SYM));
            xmlSerializer.endTag(null, "sym");

            xmlSerializer.endTag(null, "wpt");

            cur_wp_data.moveToNext();
        }

        cur_wp_data.close();
        }
        catch (Exception e){
            Log.w("MY_CHECK","ERRORE in build_wpt ["+e.toString()+"]");
        }


    }
}
