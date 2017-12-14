package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

import java.io.IOException;

import ch.epfl.mmspg.testbed360.image.ImageGrade;
import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.image.VRImageType;
import ch.epfl.mmspg.testbed360.tracking.TrackingTask;
import ch.epfl.mmspg.testbed360.ui.VRMenu;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;
import ch.epfl.mmspg.testbed360.ui.VRUI;

/**
 * A {@link VRScene} corresponds to a stage of a storyboard where a {@link VRImage} is being displayed
 * to the user. At first, the {@link #menu} is hidden and is set visible when the user first triggers
 * the cardboard (see {@link #onCardboardTrigger()}).
 * A {@link VRScene} has two default modes, which will determine the behaviour of the {@link #menu} :
 * - {@link #MODE_TRAINING} when the given {@link VRImage} already has an {@link ImageGrade} associated
 * to it. The {@link #menu} will show the grade of the {@link VRImage}. (
 * see {@link VRMenuFactory#buildTrainingGradeMenu(Renderer, VRImage)}
 * - {@link #MODE_EVALUATION} where the user is required to set an {@link ImageGrade} in order to pass
 * to the next {@link VRScene}.
 * <p>
 * Also, depending on the given {@link VRImage}, an equirectangular projection (onto a {@link Sphere})
 * or a cubic projection (onto {@link Scene#mSkybox}) is used to display the image.
 * <p>
 * When we switch to an other {@link VRScene}, the previous {@link VRScene} should be recycled using
 * {@link #recycle()}, to ensure a reasonable RAM usage.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 30/10/2017
 */

public class VRScene extends Scene implements VRUI {
    private final static String TAG = "VRScene";

    /**
     * The two different mode with which a {@link VRScene} can work.
     * see {@link VRMenuFactory#buildTrainingGradeMenu(Renderer, VRImage)}
     * see {@link VRMenuFactory#buildEvaluationGradeMenu(Renderer, VRScene)}
     */
    public final static int MODE_TRAINING = 0;
    public final static int MODE_EVALUATION = 1;

    /**
     * Static shared {@link Texture} and {@link Material} for the {@link Sphere}. As we display a
     * {@link VRScene} at a time, we can thus reuse the same {@link Texture} to bind to the {@link Sphere}
     * and avoid an increased RAM usage.
     */
    private static Texture sphereTexture;
    private static Material sphereMaterial;

    /**
     * The {@link VRImage} that is to be displayed in this scene.
     */
    VRImage vrImage;

    /**
     * A {@link Sphere} used for the equirectangular projection
     */
    private Sphere sphere;

    /**
     * The default {@link VRMenu} of this scene, should contain all elements of the UI.
     */
    VRMenu menu;

    /**
     * Just a dot at the screen's centre to show which {@link ch.epfl.mmspg.testbed360.ui.VRButton}
     * the user is looking at.
     * Various {@link double[]} and {@link Matrix4} are also kept as field and used in
     * {@link #centerSelectionDot(VRViewRenderer)} to spare some computational costs.
     */
    Sphere selectionDot;
    private double[] newDotPos = new double[4];
    private double[] initDotPos = {0, -0.3, -3, 1.0f};
    private Matrix4 headViewMatrix = new Matrix4();


    private boolean isRecycled = false;

    private final int mode;

    private TrackingTask trackingTask;

    /**
     * Creates and initializes a {@link VRScene} with the given {@link VRImage} and the given {@link Renderer}
     * used to get a {@link Context} to load images (see {@link VRImage#getBitmap(Context)}.
     * Depending on the {@link VRImage#getVrImageType()}, we use equirectangular (using {@link #sphere})
     * or cubic (using {@link VRScene#mSkybox} projections.
     *
     * @param renderer the {@link Renderer} used to display this scene
     * @param image    the {@link VRImage} that is to be displayed in this scene
     */
    public VRScene(@NonNull Renderer renderer, @Nullable VRImage image, int mode) {
        super(renderer);
        this.vrImage = image;

        if (mode != MODE_EVALUATION && mode != MODE_TRAINING) {
            throw new IllegalArgumentException(
                    "Given mode does not exist : " + mode + ". Please use MODE_EVALUATION or MODE_TRAINiNG"
            );
        }
        this.mode = mode;

        if (image != null) {
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
            Log.i(TAG, "Loaded with image " + vrImage);
        }
        initMenu(renderer);
        initSelectionDot();

        if (mode == MODE_EVALUATION) {
            trackingTask = new TrackingTask(this, renderer.getContext());
            trackingTask.startTracking();
        }

    }

    /**
     * Inits the {@link Sphere} used for equirectangular projection. Basically creates it, checks
     * the {@link #sphereTexture} and {@link #sphereMaterial} and update them with the {@link VRImage}'s
     * {@link Bitmap}, then bind it to the newly created sphere.
     *
     * @param context {@link Context} to load the {@link Bitmap}s from
     */
    void initSphere(@NonNull Context context) {
        if (sphereMaterial == null) {
            sphereMaterial = new Material();
            sphereMaterial.setColor(0);
        }

        try {
            Bitmap[] bitmaps = vrImage.getBitmap(context);
            if (bitmaps == null || bitmaps.length < 1 || bitmaps[0] == null) {
                throw new IOException("Error : no equirectangular bitmap for picture " + vrImage);
            }
            if (sphereTexture == null) {
                sphereTexture = new Texture("photo", bitmaps[0]);
            } else {
                sphereTexture.setBitmap(bitmaps[0]);
            }
            sphereMaterial.addTexture(sphereTexture);
        } catch (IOException | ATexture.TextureException e) {
            e.printStackTrace();
            //TODO display a vr message saying there was an issue loading image
        }

        sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1); //otherwise image is inverted
        sphere.setMaterial(sphereMaterial);

        addChild(sphere);
    }

    /**
     * Inits the {@link VRScene#mSkybox} used for cubic projection. Basically gets the {@link VRImage}'s
     * {@link Bitmap}s, then bind them to the scene's skybox.
     *
     * @param context {@link Context} to load the {@link Bitmap}s from
     */
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

    /**
     * Initializes a dot at the screen's center, so that the user can better determine at which UI
     * element he/she's looking at.
     */
    private void initSelectionDot() {
        selectionDot = new Sphere(0.015f, 8, 4);
        Material material = new Material();
        material.setColor(Color.WHITE);
        selectionDot.setMaterial(material);

        selectionDot.setScaleX(-1); //otherwise image is inverted
        selectionDot.setMaterial(material);

        selectionDot.setPosition(initDotPos[0], initDotPos[1], initDotPos[2]);
        selectionDot.setVisible(false);
        addChild(selectionDot);
    }

    /**
     * Inits the scene's {@link VRMenu}, depending on whether the set {@link #mode}
     *
     * @param renderer the {@link Renderer} used to draw the {@link VRMenu}
     */
    protected void initMenu(@NonNull Renderer renderer) {
        switch (mode) {
            case MODE_TRAINING:
                menu = VRMenuFactory.buildTrainingGradeMenu(renderer, vrImage);
                menu.setVisible(false);
                addChild(menu);
                break;
            case MODE_EVALUATION:
                menu = VRMenuFactory.buildEvaluationGradeMenu(renderer, this);
                menu.setVisible(false);
                addChild(menu);
                break;
        }
    }

    /**
     * Centers the {@link #selectionDot} on the screen
     *
     * @param renderer the {@link VRViewRenderer} on which the {@link #selectionDot} is being drawn
     */
    private void centerSelectionDot(@NonNull VRViewRenderer renderer) {
        headViewMatrix.setAll(renderer.getMHeadViewMatrix());
        headViewMatrix = headViewMatrix.inverse();
        double[] headViewMatrixInv = new double[16];
        headViewMatrix.toArray(headViewMatrixInv);

        Matrix.multiplyMV(newDotPos, 0, headViewMatrixInv, 0, initDotPos, 0);
        selectionDot.setPosition(newDotPos[0], newDotPos[1], newDotPos[2]);

        selectionDot.setLookAt(getCamera().getPosition());
    }

    /**
     * If the {@link #menu} is not visible yet, shows it as long as the {@link #selectionDot}. Otherwise,
     * propagates the Cardboard trigger to the {@link #menu} so that it can determine which UI component
     * to trigger.
     */
    @Override
    public boolean onCardboardTrigger() {
        if (!isRecycled && menu != null) {
            if (menu.isVisible()) {
                menu.onCardboardTrigger();
            } else {
                menu.setVisible(true);
                selectionDot.setVisible(true);
            }
        }
        return true;
    }

    /**
     * Called for every {@link VRViewRenderer#onDrawEye(Eye)}. Executes drawing actions for the {@link #menu}
     * and centers the {@link #selectionDot}.
     *
     * @param vrViewRenderer the {@link VRViewRenderer} drawing.
     */
    @Override
    public void onDrawing(@NonNull VRViewRenderer vrViewRenderer) {
        if (!isRecycled) {
            if (menu != null && menu.isVisible()) {
                menu.onDrawing(vrViewRenderer);
            }
            if (selectionDot != null) {
                centerSelectionDot(vrViewRenderer);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recycle() {
        isRecycled = true;
        menu.recycle();

        if (vrImage == null || vrImage.getVrImageType().equals(VRImageType.CUBIC)) {
            if (mSkyboxTexture != null) {
                mSkyboxTexture.shouldRecycle(true);
                TextureManager.getInstance().removeTexture(mSkyboxTexture);
                mSkyboxTexture = null;
            }
        } else if (vrImage.getVrImageType().equals(VRImageType.EQUIRECTANGULAR)) {
            if (sphereTexture != null) {
                sphereTexture.shouldRecycle(true);
                sphereMaterial.removeTexture(sphereTexture);
                TextureManager.getInstance().removeTexture(sphereTexture);
                sphere = null;
            }
        }
        menu = null;
        newDotPos = null;
        initDotPos = null;
        headViewMatrix = null;
        destroyScene();
        System.gc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecycled() {
        return isRecycled;
    }

    /**
     * @return Gets the value of vrImage and returns vrImage
     */
    @Nullable
    public VRImage getVrImage() {
        return vrImage;
    }

    /**
     * Changes the g{@link ImageGrade} of the scene's {@link #vrImage}, and stops the associated
     * {@link #trackingTask} so that it can log the {@link ImageGrade}.
     *
     * @param grade the {@link ImageGrade} given by the user to the {@link #vrImage}
     */
    public void setGrade(@NonNull ImageGrade grade) {
        if (vrImage != null) {
            vrImage.setGrade(grade);
        }
        if (trackingTask != null) {
            trackingTask.stopTracking();
        }

    }

    public int getMode() {
        return mode;
    }
}
