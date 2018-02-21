// Copyright (C) 2017 ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland
// Multimedia Signal Processing Group
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

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