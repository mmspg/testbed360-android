package ch.epfl.mmspg.testbed360.ui;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.util.EmptyStackException;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.VRViewActivity;

/**
 * This class' goalis to provide methods for building {@link VRMenu} that we use multiple times
 * through the app.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 31/10/2017
 */

public final class VRMenuFactory {
    //TODO remove or set false this in production, only for debugging
    private final static boolean RENDER_FPS = true;

    private VRMenuFactory() {
        //do nothing, this constructor is private to follow the Factory Pattern
    }

    public static VRMenu buildWelcomeMenu(final Renderer renderer) {
        VRMenu menu = new VRMenu(20);

        try {
            //TODO add the tutorial in this button, and store the string value in xml !
            VRButton welcomeButton = new VRButton(renderer.getContext(), "Welcome !", 10f, 2f);

            if (RENDER_FPS) {
                menu.addButton(buildFPSButton(renderer));
            }
            final VRButton startButton = new VRButton(renderer.getContext(),
                    "Start training!", //TODO put text in strings.xml
                    10f,
                    2f);
            startButton.setName("StartButton");
            startButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        renderer.switchScene(new VRScene(renderer, VRViewActivity.nextTraining()));
                    } catch (EmptyStackException e) {
                        startButton.setText("No new image"); //TODO put text in strings.xml
                    }
                    return null;
                }
            });
            menu.addButton(welcomeButton);
            menu.addButton(startButton);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }

    private static VRButton buildFPSButton(final Renderer renderer) throws ATexture.TextureException {
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
        return fpsButton;
    }
}
