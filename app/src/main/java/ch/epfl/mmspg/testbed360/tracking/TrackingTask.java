package ch.epfl.mmspg.testbed360.tracking;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.image.ImageGrade;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 17/11/2017
 */

public class TrackingTask extends AsyncTask<VRScene, String[], ImageGrade> {
    private final static String TAG = "TrackingTask";
    private final static long LOOP_DELAY = TimeUnit.SECONDS.toMillis(1);

    private long sceneTimestamp;
    private CSVWriter csvWriter;

    public void init(Context context) {
        sceneTimestamp = System.currentTimeMillis();
        File trackFile = TrackUtils.getTrackFile(sceneTimestamp, context);
        try {
            csvWriter = new CSVWriter(new FileWriter(trackFile));
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to file " + trackFile);
            e.printStackTrace();
        }
    }

    @Override
    protected ImageGrade doInBackground(VRScene... vrScenes) {
        while (vrScenes[0].getVrImage().getGrade().equals(ImageGrade.NONE)) {
            long start = System.currentTimeMillis();
            String[] angles = new String[]{
                    //TODO convert to pitch, yaw and roll
                    Double.toString(vrScenes[0].getCamera().getRotX()),
                    Double.toString(vrScenes[0].getCamera().getRotY()),
                    Double.toString(vrScenes[0].getCamera().getRotZ())
            };
            publishProgress(angles);
            long end = System.currentTimeMillis();

            try {
                //ensures that we run every LOOP_DELAY ms, independent from the time to save data !
                long sleep = LOOP_DELAY - (end - start);
                //TODO remove following logging
                Log.d(TAG, "Sleep : " + sleep);
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
        }
        return vrScenes[0].getVrImage().getGrade();
    }

    @Override
    protected void onProgressUpdate(String[]... progress) {
        if (csvWriter != null) {
            csvWriter.writeNext(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(ImageGrade result) {
        //TODO save grade here

        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {
        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
