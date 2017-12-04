package ch.epfl.mmspg.testbed360.tracking;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.VRViewActivity;
import ch.epfl.mmspg.testbed360.image.ImageGrade;
import ch.epfl.mmspg.testbed360.image.VRImage;

/**
 * Represents an {@link AsyncTask} responsible for logging all movements/rotation of the head to a
 * log file for the {@link #vrScene} associated. Its lifecycle starts with its creation, which needs
 * a given {@link #vrScene} that should be in {@link VRScene#MODE_EVALUATION}.
 * Then the {@link VRScene} can {@link #startTracking()} when it is ready. When the user gives the
 * {@link VRScene#vrImage} a grade (see {@link VRScene#setGrade(ImageGrade)}, the task is stopped by
 * calling {@link #stopTracking()}, which will cause the task to call {@link #onPostExecute(VRImage)}
 * so that the grade is logged too.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 17/11/2017
 */

public class TrackingTask extends AsyncTask<VRScene, String[], VRImage> {
    private final static String TAG = "TrackingTask";

    /**
     * Directory name where we will store log files
     */
    public final static String TRACKING_DIR = "tracking";

    /**
     * ID of the current session of tracking, i.e. the exact time the user started the {@link VRScene#MODE_EVALUATION}
     */
    private final static long SESSION_TRACK_ID = System.currentTimeMillis();

    /**
     * The delay between logs of rotation of the {@link VRScene#getCamera()}
     */
    private final static long LOOP_DELAY = 33;

    /**
     * The {@link CSVWriter} used to log every grades of images. The same used through the app, as
     * the implementation of {@link CSVWriter} does not allow to write, close and reopen to append new
     * lines (will erase previous content instead).
     * Is closed whenever the {@link ch.epfl.mmspg.testbed360.VRViewActivity} is being destroyed
     * see {@link VRViewActivity#onDestroy()}
     */
    private static CSVWriter SESSION_TRACK_CSV_WRITER;

    /**
     * The track id associated to the displayed {@link VRScene}. Used to name the file containing logs
     * of the camera's rotation (see {@link #getTrackFile(long, Context)}, and thus uniquely identify
     * the file containing data we want for a given {@link VRScene}.
     */
    private long trackId;

    /**
     * Used to log the {@link VRScene#getCamera()} rotation angles. Is initialized for every new task,
     * and closed whenever the task has finished or is cancelled
     * see {@link #onCancelled()}
     * see {@link #onPostExecute(VRImage)}
     */
    private CSVWriter trackCSVWriter;

    /**
     * The {@link VRScene} we are tracking.
     */
    private VRScene vrScene;

    /**
     * Boolean used to keep tracking or stop tracking the camera's rotation.
     */
    private volatile boolean track = true;

    /**
     * Creates a new {@link TrackingTask} associated to the given {@link VRScene}.
     *
     * @param vrScene the {@link VRScene} in which we want the {@link VRScene#getCamera()}'s rotation
     *                angles to be logged.
     * @param context the app context, used to initialize our logging files
     *                see {@link #getSessionTrackFile(Context)}
     *                see {@link #getTrackFile(long, Context)}
     */
    public TrackingTask(@NonNull VRScene vrScene,@NonNull Context context) {
        if (vrScene.getMode() != VRScene.MODE_EVALUATION) {
            throw new IllegalStateException("The VRScene must be in MODE_EVALUATION to be tracked");
        }
        this.vrScene = vrScene;

        initSessionTrackCSVWriter(context);

        trackId = System.currentTimeMillis();
        File trackFile = getTrackFile(trackId, context);
        try {
            trackCSVWriter = new CSVWriter(new FileWriter(trackFile));
            trackCSVWriter.writeNext(new String[]{"Time", "Roll", "Pitch", "Yaw"}, false);
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to file " + trackFile);
            e.printStackTrace();
        }
    }

    /**
     * Starts tracking the {@link #vrScene}.
     * see {@link #doInBackground(VRScene...)}
     */
    public void startTracking() {
        execute(vrScene);
    }

    /**
     * Stops tracking the {@link #vrScene}
     * see {@link #doInBackground(VRScene...)}
     * see {@link #onPostExecute(VRImage)}
     */
    public void stopTracking() {
        track = false;
    }

    /**
     * Executes the logging on the {@link VRScene#getCamera()} in background. Its execution is stopped
     * by changing {@link #track} to false.
     * Rotation angles of the camera are logged at it iteration, which happens every {@link #LOOP_DELAY}
     *
     * @param vrScenes the {@link VRScene} to be logged. Shouldn't be null, and only the first element
     *                 is used.
     * @return the {@link VRImage} of the scene, which should be graded at this time.
     */
    @Override
    @Nullable
    protected VRImage doInBackground(@NonNull VRScene... vrScenes) {
        if (vrScenes[0] == null) {
            throw new IllegalArgumentException("Given VRScene was null !");
        }
        while (track) {
            long start = System.currentTimeMillis();
            String[] values = new String[]{
                    Long.toString(start),
                    Double.toString(vrScenes[0].getCamera().getRotX()),
                    Double.toString(vrScenes[0].getCamera().getRotY()),
                    Double.toString(vrScenes[0].getCamera().getRotZ())
            };
            //we do not call publishProgress as it would overload the main thread message queue, hence
            //onPostExecute would not be called/would be called way too late !
            if (trackCSVWriter != null) {
                trackCSVWriter.writeNext(values,false);
            }
            long end = System.currentTimeMillis();

            try {
                //ensures that we run every LOOP_DELAY ms, independent from the time to save data !
                long sleep = LOOP_DELAY - (end - start);
                //Log.d(TAG,"Delay : "+sleep);
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
        }
        return vrScenes[0].getVrImage();
    }

    /**
     * Logs the {@link VRImage#getGrade()} associated to the task's {@link #trackId};
     * then closes the {@link #trackCSVWriter}
     *
     * @param result the graded {@link VRImage} of the {@link #vrScene}
     */
    @Override
    protected void onPostExecute(@Nullable VRImage result) {
        if (result != null) {
            try {
                logGrade(
                        result.getFile().getName(),
                        result.getGrade(),
                        trackId
                );
                trackCSVWriter.flush();
                trackCSVWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Directly closes the {@link #trackCSVWriter}
     */
    @Override
    protected void onCancelled() {
        try {
            trackCSVWriter.flush();
            trackCSVWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes and creates all the necessary folder structure to hold the {@link File} used to
     * track, with id trackId to name and identify it.
     *
     * @param trackId the id used to name the file. see {@link #trackId}
     * @param context {@link Context} used to init and create files and folders
     * @return a writable {@link File}, in which data can be logged data !
     */
    @NonNull
    private static File getTrackFile(long trackId, @NonNull Context context) {
        File trackDir = new File(VRViewActivity.getCurrentSession().getSessionDir(), TRACKING_DIR);
        trackDir.mkdirs();

        File trackFile = new File(trackDir, Long.toString(trackId) + "t"); //Getting a file within the dir.
        Log.d(TAG, "Writing to " + trackFile);
        if (!trackFile.exists()) {
            try {
                trackFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return trackFile;
    }

    /**
     * Works pretty much like {@link #getTrackFile(long, Context)}, but with the difference that this
     * file will hold information written by {@link #logGrade(String, ImageGrade, long)}, so that a
     * human reading this file can identify the {@link VRImage} viewed, the grade it was given, and
     * the name of the file that contains the logging of camera angles (see {@link #trackId}.
     *
     * @param context {@link Context} used to init and create files and folders
     * @return a writable {@link File}, in which {@link ImageGrade} and {@link #trackId}s can be logged!
     */
    @NonNull
    private static File getSessionTrackFile(@NonNull Context context) {
        File trackDir = new File(VRViewActivity.getCurrentSession().getSessionDir(), TRACKING_DIR);
        trackDir.mkdirs();

        File trackFile = new File(trackDir, Long.toString(SESSION_TRACK_ID) + "g"); //Getting a file within the dir.
        Log.d(TAG, "Writing to " + trackFile);
        if (!trackFile.exists()) {
            try {
                trackFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return trackFile;
    }

    /**
     * Creates if necessary (=is null) the {@link CSVWriter} used to log {@link ImageGrade}s and
     * associated {@link #trackId}s.
     * see {@link #SESSION_TRACK_CSV_WRITER}
     *
     * @param context see {@link #getSessionTrackFile(Context)}
     */
    private static void initSessionTrackCSVWriter(@NonNull Context context) {
        File sessionTrackFile = getSessionTrackFile(context);
        try {
            if (SESSION_TRACK_CSV_WRITER == null) {
                SESSION_TRACK_CSV_WRITER = new CSVWriter(new FileWriter(sessionTrackFile));
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to file " + sessionTrackFile);
            e.printStackTrace();
        }
    }

    /**
     * Flushes and closes the {@link #SESSION_TRACK_CSV_WRITER}. Should be only called when the evaluation
     * of all images is done or the app is destroyed.
     * see {@link #SESSION_TRACK_CSV_WRITER}
     */
    public static void closeSessionTrackCSVWriter() {
        try {
            SESSION_TRACK_CSV_WRITER.flush();
            SESSION_TRACK_CSV_WRITER.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs to the same file the name of a {@link VRImage}'s file, its associated {@link ImageGrade}
     * and the {@link #trackId} that will allow us to get info about the {@link #vrScene}'s camera
     * angles during its evaluation.
     *
     * @param imgName  the {@link VRImage}'s {@link File}'s name
     * @param imgGrade the {@link ImageGrade} given to the {@link VRImage}
     * @param trackId  see {@link #trackId}
     */
    private static void logGrade(@NonNull String imgName, @NonNull ImageGrade imgGrade, long trackId) {
        SESSION_TRACK_CSV_WRITER.writeNext(new String[]{
                imgName,
                Integer.toString(imgGrade.toInt()),
                Long.toString(trackId)
        });
        try {
            SESSION_TRACK_CSV_WRITER.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
