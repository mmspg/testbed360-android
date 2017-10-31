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
        menu = new VRMenu(20);

        try {
            VRButton welcomeButton = new VRButton(renderer.getContext(), "Welcome !", 10f, 2f);

            final VRButton fpsButton = new VRButton(renderer.getContext(),
                    "",
                    10f,
                    2f);
            fpsButton.setName("FPSButton");
            renderer.setFPSUpdateListener(new OnFPSUpdateListener() {
                @Override
                public void onFPSUpdate(double fps) {
                    //pretty sure we have to divide per two because this method was thought
                    //for non VR rendering, hence we render twice as much image, which would give
                    //us here ~120FPS which seems way too much!
                    //also rounding to display to the nearest .5, hence to avoid redrawing too often
                    // this button which would make the FPS go down ironically !
                    fpsButton.setText("FPS:" + Math.round(fps) / 2.0);
                }
            });
            final VRButton startButton = new VRButton(renderer.getContext(),
                    "Start !",
                    10f,
                    2f);
            startButton.setName("StartButton");
            startButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        renderer.switchScene(new VRScene(renderer, VRViewActivity.nextTraining()));
                    } catch (EmptyStackException e) {
                        startButton.setText("No new image");
                    }
                    return null;
                }
            });
            menu.addButton(welcomeButton);
            menu.addButton(fpsButton);
            menu.addButton(startButton);


        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        addChild(menu);
        menu.setVisible(true);
    }
}
