package ch.epfl.mmspg.testbed360.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import ch.epfl.mmspg.testbed360.R;
import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.VRViewActivity;
import ch.epfl.mmspg.testbed360.tracking.TrackingTask;

/**
 * Represents a batch of {@link VRImage} that are going to be viewed by the user. When the app starts,
 * the list of {@link ImagesSession} available is displayed so that the user can pick one.
 * A session of {@link VRImage} is simply built by having a folder with an {@link int} value as name,
 * containing folders {@link #EVALUATION_DIR} and {@link #TRAINING_DIR}.
 * <p>
 * {@link ImagesSession} should only be loaded using {@link LoadTask}.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 24/11/2017
 */

public class ImagesSession {
    private final static String TAG = "ImagesSession";


    final static String EVALUATION_DIR = "evaluation";
    final static String TRAINING_DIR = "training";

    /**
     * Map that allows us to fetch back a session given its id
     */
    private final static SparseArray<ImagesSession> SESSIONS_MAP = new SparseArray<>();

    /**
     * Default data folder of the app, corresponds to Android/data/ch.epfl.mmsp.tesbed360/files. This
     * is where session files must be put.
     */
    private static File DATA_DIR;

    private int id;
    private Stack<VRImage> evaluationImages = new Stack<>();
    private Stack<VRImage> trainingImages = new Stack<>();
    private File sessionDir;
    private int sessionTrackCount;

    /**
     * Inits an {@link ImagesSession} instance with the given folder and id.
     *
     * @param id         the id of the session, usually parsed from its folder name
     * @param sessionDir folder containing the session images
     * @param context    {@link Context} used to load files
     */
    private ImagesSession(int id, File sessionDir, Context context) {
        this.sessionDir = sessionDir;
        //We init the training images here
        this.id = id;
        List<VRImage> vrImgs = ImageUtils.distinctShuffle(ImageUtils.loadVRImages(context, sessionDir, VRScene.MODE_TRAINING));
        Collections.reverse(vrImgs); // we reverse here as it will be inverted in the stack after
        trainingImages.addAll(vrImgs);

        //And we init the evaluation pictures
        vrImgs = ImageUtils.distinctShuffle(ImageUtils.loadVRImages(context, sessionDir, VRScene.MODE_EVALUATION));
        Collections.reverse(vrImgs); // we reverse here as it will be inverted in the stack after
        evaluationImages.addAll(vrImgs);

        vrImgs.clear();

        sessionTrackCount = computeSessionTrackCount();

        SESSIONS_MAP.put(id, this);
    }

    /**
     * @return the next not yet displayed training {@link VRImage}
     * @throws EmptyStackException in case there is no training {@link VRImage} not yet displayed !
     */
    @NonNull
    public VRImage nextTraining() throws EmptyStackException {
        return trainingImages.pop();
    }

    /**
     * @return the next not yet displayed evaluation {@link VRImage}
     * @throws EmptyStackException in case there is no evaluation {@link VRImage} not yet displayed !
     */
    @NonNull
    public VRImage nextEvaluation() throws EmptyStackException {
        return evaluationImages.pop();
    }

    /**
     * First attempts to init {@link #DATA_DIR} if it is empty, and then returns a {@link LoadTask}
     * that is used to detect {@link ImagesSession} ready to be used !
     *
     * @param context the app's {@link Context} to load files from
     * @return a {@link LoadTask} to fetch available {@link ImagesSession} on the current device
     */
    public static LoadTask getLoadingTask(@NonNull final Context context) {
        if (DATA_DIR == null) {
            DATA_DIR = context.getExternalFilesDir(null);
            if (DATA_DIR == null) {
                throw new IllegalStateException("Data dir folder is null");
            }

            if (!DATA_DIR.exists() && !DATA_DIR.mkdirs()) {
                throw new IllegalStateException("Could not create data dir folder : " + DATA_DIR);
            }
        }
        return new LoadTask();
    }

    /**
     * @return the id of the session, its folder name (must be an int parseable string).
     */
    public int getId() {
        return id;
    }

    /**
     * @param sessionId the id of the session we're interested in
     * @return the {@link ImagesSession} with {@link #id} {@param sessionId}, stored in the {@link #SESSIONS_MAP}.
     * Returns null if there is no session associated to this id.
     */
    @Nullable
    public static ImagesSession getFromId(int sessionId) {
        return SESSIONS_MAP.get(sessionId);
    }

    public File getSessionDir() {
        return sessionDir;
    }

    private int computeSessionTrackCount() {
        if (sessionDir == null || !sessionDir.exists()) {
            throw new IllegalStateException("Session dir does not exist : " + sessionDir);
        }
        File[] trackFiles = new File(sessionDir, TrackingTask.TRACKING_DIR).listFiles();
        if (trackFiles == null || trackFiles.length == 0) {
            return 0;
        }
        int count = 0;
        for (File f : trackFiles) {
            if (f.getName().matches("\\d+g")) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return Gets the count of already done tracking on this {@link ImagesSession}
     */
    public int getSessionTrackCount() {
        return sessionTrackCount;
    }

    /**
     * This class is to be used to seek existing {@link ImagesSession} on the device, all in a
     * background task.
     */
    public static class LoadTask extends AsyncTask<Activity, Void, List<ImagesSession>> {

        @Override
        protected List<ImagesSession> doInBackground(@NonNull Activity... activities) {
            if (activities[0] == null) {
                throw new IllegalArgumentException("Context was null");
            }
            SESSIONS_MAP.clear();
            File[] files = DATA_DIR.listFiles();
            List<ImagesSession> sessions = new ArrayList<>();
            if (files == null || files.length == 0) {
                return sessions;
            }

            for (File f : files) {
                if (f.isDirectory()) {
                    try {
                        int id = Integer.parseInt(f.getName());
                        sessions.add(new ImagesSession(id, f, activities[0]));
                    } catch (NumberFormatException ignored) {
                        //this folder is not named as wanted !
                    }
                }
            }
            Collections.sort(sessions, new Comparator<ImagesSession>() {
                @Override
                public int compare(ImagesSession o1, ImagesSession o2) {
                    if(o1 == null && o2!=null){
                        return 1;
                    }else if(o1 != null && o2==null){
                        return -1;
                    }else if (o1 == null){
                        return 0;
                    }
                    return Integer.compare(o1.getId(),o2.getId());
                }
            });
            return sessions;
        }
    }

    /**
     * Class used to represent an {@link ImagesSession} in the {@link ch.epfl.mmspg.testbed360.StartActivity}.
     */
    public static class Adapter extends ArrayAdapter<ImagesSession> {
        private final List<ImagesSession> sessions;
        private final LayoutInflater layoutInflater;

        public Adapter(@NonNull Context context, int resource, @NonNull List<ImagesSession> objects) {
            super(context, resource, objects);
            sessions = objects;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.session_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.titleView = (TextView) convertView.findViewById(R.id.session_item_title);
                viewHolder.descriptionView = (TextView) convertView.findViewById(R.id.session_item_description);
                viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.session_item_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final ImagesSession session = getItem(position);
            if (session != null) {
                viewHolder.titleView.setText(
                        getContext().getString(R.string.session_number,session.getId())
                );
                viewHolder.descriptionView.setText(getContext().getResources().getQuantityString(
                        R.plurals.sessions_count,
                        session.getSessionTrackCount(),//as quantity
                        session.getSessionTrackCount()//as %1$d placeholder for the string
                ));
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), VRViewActivity.class);
                        intent.putExtra(VRViewActivity.SESSION_ID_TAG, session.getId());
                        getContext().startActivity(intent);
                    }
                });
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return sessions.size();
        }

        @Override
        public ImagesSession getItem(int position) {
            return sessions.get(position);
        }

        /**
         * Quick class to be used in {@link Adapter#getView(int, View, ViewGroup)}
         */
        static class ViewHolder {
            private TextView titleView;
            private TextView descriptionView;
            private LinearLayout layout;
        }
    }
}
