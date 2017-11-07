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
import android.view.Window;
import android.view.WindowManager;

import org.rajawali3d.vr.VRActivity;

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.image.VRImage;

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
    private VRViewRenderer mRenderer;
    private static Stack<VRImage> TRAINING_IMAGES = new Stack<>();
    private static Stack<VRImage> EVALUATION_IMAGES = new Stack<>();

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

        mRenderer = new VRViewRenderer(this);
        setRenderer(mRenderer);
    }

    @Override
    public void onStart() {
        super.onStart();
        setConvertTapIntoTrigger(true);

        try {
            //We init the training images here
            List<VRImage> vrImgs = ImageUtils.distinctShuffle(ImageUtils.loadVRImages(this, VRScene.MODE_TRAINING));
            Collections.reverse(vrImgs); // we reverse here as it will be inverted in the stack after
            TRAINING_IMAGES.addAll(vrImgs);

            //And we init the evaluation pictures
            vrImgs = ImageUtils.distinctShuffle(ImageUtils.loadVRImages(this, VRScene.MODE_EVALUATION));
            Collections.reverse(vrImgs); // we reverse here as it will be inverted in the stack after
            EVALUATION_IMAGES.addAll(vrImgs);
        } catch (IllegalStateException e) {
            //TODO display a message saying that there is no picture
            e.printStackTrace();
        }
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

    /**
     * @return the next not yet displayed training {@link VRImage}
     * @throws EmptyStackException in case there is no training {@link VRImage} not yet displayed !
     */
    public static VRImage nextTraining() throws EmptyStackException {
        return TRAINING_IMAGES.pop();
    }

    /**
     * @return the next not yet displayed evaluation {@link VRImage}
     * @throws EmptyStackException in case there is no evaluation {@link VRImage} not yet displayed !
     */
    public static VRImage nextEvaluation() throws EmptyStackException {
        return EVALUATION_IMAGES.pop();
    }
}