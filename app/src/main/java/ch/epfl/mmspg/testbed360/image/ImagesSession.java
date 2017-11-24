package ch.epfl.mmspg.testbed360.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import ch.epfl.mmspg.testbed360.R;
import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.VRViewActivity;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 24/11/2017
 */

public class ImagesSession {
    private final static String TAG = "ImagesSession";
    private final static String EVALUATION_DIR = "evaluation";
    private final static String TRAINING_DIR = "training";

    private final static HashMap<Integer, ImagesSession> SESSIONS_MAP = new HashMap<>();
    private static File DATA_DIR;

    private int id;
    private Stack<VRImage> evaluationImages = new Stack<>();
    private Stack<VRImage> trainingImages = new Stack<>();
    private File sessionDir;

    private ImagesSession(int id, File sessionDir, Context context) {
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

        SESSIONS_MAP.put(id,this);
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

    public static LoadTask getLoadingTask(@NonNull final Context context){
        if (DATA_DIR == null) {
            DATA_DIR = context.getExternalFilesDir(null);
            if (DATA_DIR == null) {
                throw new IllegalStateException("Data dir folder is null");
            }

            if(!DATA_DIR.exists() && !DATA_DIR.mkdirs()){
                throw new IllegalStateException("Could not create data dir folder : "+ DATA_DIR);
            }
        }
        return new LoadTask();
    }

    public int getId() {
        return id;
    }

    @Nullable
    public static ImagesSession getFromId(int sessionId){
        return SESSIONS_MAP.get(sessionId);
    }

    public File getSessionDir() {
        return sessionDir;
    }

    public static class LoadTask extends AsyncTask<Activity, Void, List<ImagesSession>> {

        @Override
        protected List<ImagesSession> doInBackground(@NonNull Activity... activities) {
            if(activities[0] == null){
                throw new IllegalArgumentException("Context was null");
            }
            File[] files = DATA_DIR.listFiles();
            List<ImagesSession> sessions = new ArrayList<>();
            if(files == null || files.length == 0 ){
                ((TextView)activities[0].findViewById(R.id.noSessionText)).setVisibility(View.VISIBLE);
                hideLoadingBar(activities[0]);
                return sessions;
            }

            for(File f : files){
                if(f.isDirectory()){
                    try {
                        int id = Integer.parseInt(f.getName());
                        //TODO add to strings.xml
                        ((TextView)activities[0].findViewById(R.id.loadingProgressText)).setText("Randomizing session "+ id);
                        sessions.add(new ImagesSession(id,f,activities[0]));
                    }catch (NumberFormatException ignored){
                        //this folder is not named as wanted !
                    }
                }
            }
            if(sessions.isEmpty()){
                ((TextView)activities[0].findViewById(R.id.noSessionText)).setVisibility(View.VISIBLE);
            }
            hideLoadingBar(activities[0]);

            return sessions;
        }

        private void hideLoadingBar(@NonNull Activity activity){
            activity.findViewById(R.id.loadingProgressText).setVisibility(View.GONE);
            activity.findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        }
    }

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
            final TextView textView;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.session_list_item, null);
                textView = (TextView) convertView.findViewById(R.id.session_item_text);
                convertView.setTag(textView);
            } else {
                textView = (TextView) convertView.getTag();
            }

            final ImagesSession session = getItem(position);
            if (session != null) {
                textView.setText("Session n°" + session.getId());
                textView.setOnClickListener(new View.OnClickListener() {
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
        public int getCount(){
            return sessions.size();
        }

        @Override
        public ImagesSession getItem(int position){
            return sessions.get(position);
        }
    }
}
