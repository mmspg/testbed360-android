package ch.epfl.mmspg.testbed360.tracking;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.epfl.mmspg.testbed360.VRScene;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 17/11/2017
 */

public final class TrackUtils {
    private final static String TAG = "TrackUtils";
    private final static String TRACKING_DIR = "tracking";

    private TrackUtils(){
        //private constructor so that you cannot build a TrackUtils instance
    }

    public static TrackingTask startTracking(VRScene scene, Context context){
        TrackingTask trackingTask = new TrackingTask();
        trackingTask.init(context);
        trackingTask.execute(scene);
        return trackingTask;
    }



    @NonNull
    public static File getTrackFile(long trackId, Context context){
        File trackDir = new File(context.getExternalFilesDir(null), TRACKING_DIR);
        trackDir.mkdirs();

        File trackFile = new File(trackDir, Long.toString(trackId)+"t"); //Getting a file within the dir.
        Log.d(TAG, "Writing to " + trackFile);
        try (FileOutputStream out = new FileOutputStream(trackFile)) {
            trackFile.createNewFile();
            out.write("0".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackFile;
    }


}
