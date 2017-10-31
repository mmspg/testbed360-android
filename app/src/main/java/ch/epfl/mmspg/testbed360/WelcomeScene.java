package ch.epfl.mmspg.testbed360;

import android.support.annotation.NonNull;
import android.util.Log;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.io.IOException;
import java.util.EmptyStackException;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.ui.VRButton;
import ch.epfl.mmspg.testbed360.ui.VRMenu;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 31/10/2017
 */

public final class WelcomeScene extends VRScene {
    private final static String TAG = "WelcomeScene";

    public WelcomeScene(@NonNull Renderer renderer) {
        super(renderer, null);

        try {
            setSkybox(ImageUtils.loadCubicMap(renderer.getContext(), R.drawable.jvet_kiteflite_cubemap32_2250x1500_raw_q00));
        } catch (IOException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }
    }

    @Override
    protected void initMenu(final Renderer renderer) {
        menu = VRMenuFactory.buildWelcomeMenu(renderer);
        menu.setVisible(true);
        addChild(menu);
    }
}
