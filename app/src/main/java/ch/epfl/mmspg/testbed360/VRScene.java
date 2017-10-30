package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.io.IOException;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.ui.VRButton;
import ch.epfl.mmspg.testbed360.ui.VRMenu;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 30/10/2017
 */

public class VRScene extends Scene {
    private final static String TAG = "VRScene";

    public final static int MODE_TRAINING = 0;
    public final static int MODE_EVALUATION = 1;

    private VRImage vrImage;
    private Sphere sphere;
    private VRMenu menu;

    public VRScene(@NonNull Renderer renderer, @NonNull VRImage image) {
        super(renderer);
        this.vrImage = image;

        switch (image.getVrImageType()) {
            case CUBIC:
                initCube(renderer.getContext());
                break;
            case EQUIRECTANGULAR:
                initSphere(renderer.getContext());
                break;
            default:
                break;
        }
    }

    private void initSphere(@NonNull Context context) {
        Material material = new Material();
        material.setColor(0);

        try {
            Bitmap[] bitmaps = vrImage.getBitmap(context);
            if (bitmaps == null || bitmaps.length < 1 || bitmaps[0] == null) {
                throw new IOException("Error : no equirectangular bitmap for picture " + vrImage);
            }
            material.addTexture(new Texture("photo", bitmaps[0]));
        } catch (IOException | ATexture.TextureException e) {
            e.printStackTrace();
            //TODO display a vr message saying there was an issue loading image
        }

        sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1); //otherwise image is inverted
        sphere.setMaterial(material);

        addChild(sphere);
    }

    private void initCube(@NonNull Context context) {
        try {
            Bitmap[] bitmaps = vrImage.getBitmap(context);
            if (bitmaps == null || bitmaps.length < 6) {
                throw new IOException("Error : no equirectangular bitmap for picture " + vrImage);
            }
            setSkybox(bitmaps);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO display a vr message saying there was an issue loading image
        }
    }

    private void initMenu(Renderer renderer) {
        menu = new VRMenu(20);

        try {
            VRButton button = new VRButton(renderer.getContext(), vrImage.getVrImageType().toString(), 10f, 2f);
            menu.addButton(button);

            final VRButton fpsButton = new VRButton(renderer.getContext(),
                    "This is a test ! No action if pressed",
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
            menu.addButton(fpsButton);


        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        addChild(menu);

    }

    public void onCardboardTrigger() {
        Log.d(TAG, "Cardboard trigger");
        if (menu != null) {
            menu.onCardboardTrigger();
        }
    }
}
