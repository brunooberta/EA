package bop.provalayout;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognizedService extends IntentService {

    private Global gbl = new Global();
    
    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.UNKNOWN,100);
        int maxConfidence = 0;

        for( DetectedActivity activity : probableActivities ) {

            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    //gbl.myLog1("ActivityRecogition - In Vehicle: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    //gbl.myLog1("ActivityRecogition - On Bicycle: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    //gbl.myLog1("ActivityRecogition - On Foot: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.RUNNING: {
                    //gbl.myLog1("ActivityRecogition - Running: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.STILL: {
                    //gbl.myLog1("ActivityRecogition - Still: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.TILTING: {
                    //gbl.myLog1("ActivityRecogition - Tilting: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.WALKING: {
                    //gbl.myLog1("ActivityRecogition - Walking: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    //gbl.myLog1("ActivityRecogition - Unknown: " + activity.getConfidence() );
                    break;
                }
            }

            if(maxConfidence < activity.getConfidence()){
                maxConfidence = activity.getConfidence();
                detectedActivity =  activity;
            }
        }

        if(maxConfidence > 75)
            gbl.setActivity(detectedActivity);
    }
}
