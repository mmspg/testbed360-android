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

import java.util.EmptyStackException;
import java.util.Stack;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.image.VRImage;

public class VRViewActivity extends VRActivity {
    private final static String TAG = "VRViewActivity";
    private VRViewRenderer mRenderer;
    private static Stack<VRImage> TRAINING_IMAGES = new Stack<>();

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
            TRAINING_IMAGES.addAll(ImageUtils.distinctShuffle(ImageUtils.loadVRImages(this, VRScene.MODE_TRAINING)));
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

    public static VRImage nextTraining() throws EmptyStackException {
        return TRAINING_IMAGES.pop();
    }
}