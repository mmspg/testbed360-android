package ch.epfl.mmspg.testbed360;

import android.support.annotation.NonNull;
import android.util.Log;

import org.rajawali3d.renderer.Renderer;

import java.io.IOException;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

/**
 * Last {@link VRScene} to be displayed on the app once a {@link ch.epfl.mmspg.testbed360.image.ImagesSession}
 * has been finished. Prompts the user with a text thanking him !
 * {@link VRScene#MODE_TRAINING}
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 09/12/2017
 */

public final class EndScene extends VRScene {
    private final static String TAG = "WelcomeScene";

    //TODO remove or set false this in production, only for debugging
    private final static boolean RENDER_AXIS = true;


    public EndScene(@NonNull Renderer renderer) {
        super(renderer, null, MODE_TRAINING);
        try {
            setSkybox(ImageUtils.loadCubicMap(renderer.getContext(), R.drawable.jvet_kiteflite_cubemap32_2250x1500_raw_q00));
        } catch (IOException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }
        selectionDot.setVisible(true);
    }

    @Override
    protected void initMenu(@NonNull final Renderer renderer) {
        menu = VRMenuFactory.buildEndMenu(renderer);
        menu.setVisible(true);
        addChild(menu);
    }
}
