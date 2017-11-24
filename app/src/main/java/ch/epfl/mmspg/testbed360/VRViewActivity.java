/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.epfl.mmspg.testbed360;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import org.rajawali3d.vr.VRActivity;

import java.util.EmptyStackException;

import ch.epfl.mmspg.testbed360.image.ImagesSession;
import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.tracking.TrackingTask;

/**
 * Custom implementation of Rajawali's {@link VRActivity}. Provides necessary methods to control the
 * app's storyboard's flow.
 * It works as a controller or {@link VRImage} which loads them {@link #onStart()} and then provide
 * them through the app execution, provided that some are still available.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */
public class VRViewActivity extends VRActivity {
    private final static String TAG = "VRViewActivity";
    public final static String SESSION_ID_TAG = "sessionId";

    private VRViewRenderer mRenderer;
    private static ImagesSession SESSION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        int sessionId = getIntent().getIntExtra(SESSION_ID_TAG,-1);
        if(sessionId == -1){
            throw new IllegalStateException("No SESSION passed to "+TAG);
        }
        SESSION = ImagesSession.getFromId(sessionId);

        if(SESSION == null){
            throw new IllegalStateException("Session with id "+sessionId+" does not exist");
        }

        mRenderer = new VRViewRenderer(this);
        setRenderer(mRenderer);
    }

    @Override
    public void onStart() {
        super.onStart();
        setConvertTapIntoTrigger(true);
    }


    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        mRenderer.onCardboardTrigger();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        TrackingTask.closeSessionTrackCSVWriter();
        super.onDestroy();
    }

    /**
     * @return the next not yet displayed training {@link VRImage}
     * @throws EmptyStackException in case there is no training {@link VRImage} not yet displayed !
     */
    @NonNull
    public static VRImage nextTraining() throws EmptyStackException {
        return SESSION.nextTraining();
    }

    /**
     * @return the next not yet displayed evaluation {@link VRImage}
     * @throws EmptyStackException in case there is no evaluation {@link VRImage} not yet displayed !
     */
    @NonNull
    public static VRImage nextEvaluation() throws EmptyStackException {
        return SESSION.nextEvaluation();
    }

    public static ImagesSession getCurrentSession(){
        return SESSION;
    }
}