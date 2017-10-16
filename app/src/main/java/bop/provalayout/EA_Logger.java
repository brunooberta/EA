package bop.provalayout;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EA_Logger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    static private String path = Environment.getExternalStorageDirectory().getPath() +"/ExplorerAssistant" ;
    static private String fileName = "_Log";
    static private String fileExt = "txt";
    static private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static public void setup() throws IOException {

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        removeOlderLogFiles();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.ITALY);
        Date now = new Date();

        fileName = formatter.format(now) + fileName + "." + fileExt;

        logger.setLevel(Level.INFO);

        fileTxt = new FileHandler(path+"/"+fileName,true);

        // create a TXT formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

    }

    static public void log (String s){
        logger.info(s + "\n\r");
    }

    static public void close (){
        fileTxt.close();
    }

    static private Calendar convertDateToCalendar(Date d){

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

       return cal;
    }

    private static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    //Cancella i file di log più vecchi di deltaMax_day
    private static void removeOlderLogFiles(){

        Date now = new Date();
        long deltaMax_day = 5;
        long deltaMax_ms = deltaMax_day*24*3600*1000;
        Calendar cal_now = convertDateToCalendar(now);

        File parentDir = new File(path);
        File[] files = parentDir.listFiles();

        for(File f:files){
            if(getFileExt(f.getName()).equals(fileExt)) {
                if (cal_now.getTimeInMillis() - f.lastModified() > deltaMax_ms) {
                    f.delete();
                }
            }
        }
    }


}
