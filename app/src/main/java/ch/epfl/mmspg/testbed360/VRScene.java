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
import ch.epfl.mmspg.testbed360.ui.Recyclable;
import ch.epfl.mmspg.testbed360.ui.VRMenu;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

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
 *
 * Also, depending on the given {@link VRImage}, an equirectangular projection (onto a {@link Sphere})
 * or a cubic projection (onto {@link Scene#mSkybox}) is used to display the image.
 * <p>
 * When we switch to an other {@link VRScene}, the previous {@link VRScene} should be recycled using
 * {@link #recycle()}, to ensure a reasonable RAM usage.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 30/10/2017
 */

public class VRScene extends Scene implements Recyclable {
    private final static String TAG = "VRScene";

    /**
     * The two different mode with which a {@link VRScene} can work.
     * see {@link VRMenuFactory#buildTrainingGradeMenu(Renderer, VRImage)}
     * see {@link VRMenuFactory#buildEvaluationGradeMenu(Renderer, VRImage)}
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
    private VRImage vrImage;

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
    private double[] initDotPos = {0, 0, -3, 1.0f};
    private Matrix4 headViewMatrix = new Matrix4();


    private boolean isRecycled = false;

    /**
     * Creates and initializes a {@link VRScene} with the given {@link VRImage} and the given {@link Renderer}
     * used to get a {@link Context} to load images (see {@link VRImage#getBitmap(Context)}.
     * Depending on the {@link VRImage#getVrImageType()}, we use equirectangular (using {@link #sphere})
     * or cubic (using {@link VRScene#mSkybox} projections.
     * @param renderer the {@link Renderer} used to display this scene
     * @param image the {@link VRImage} that is to be displayed in this scene
     */
    public VRScene(@NonNull Renderer renderer, @Nullable VRImage image) {
        super(renderer);
        this.vrImage = image;

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
    }

    /**
     * Inits the {@link Sphere} used for equirectangular projection. Basically creates it, checks
     * the {@link #sphereTexture} and {@link #sphereMaterial} and update them with the {@link VRImage}'s
     * {@link Bitmap}, then bind it to the newly created sphere.
     * @param context {@link Context} to load the {@link Bitmap}s from
     */
    private void initSphere(@NonNull Context context) {
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
     * Inits the scene's {@link VRMenu}, depending on whether there already is a grade for the given
     * {@link VRImage} (in which case we are in {@link #MODE_TRAINING}), or not ({@link #MODE_EVALUATION}).
     * @param renderer the {@link Renderer} used to draw the {@link VRMenu}
     */
    protected void initMenu(Renderer renderer) {
        if (vrImage.getGrade().equals(ImageGrade.NONE)) {
            //evaluation
            menu = VRMenuFactory.buildEvaluationGradeMenu(renderer, vrImage);
            menu.setVisible(false);
            addChild(menu);
        } else {
            menu = VRMenuFactory.buildTrainingGradeMenu(renderer, vrImage);
            menu.setVisible(false);
            addChild(menu);
        }

    }

    /**
     * If the {@link #menu} is not visible yet, shows it as long as the {@link #selectionDot}. Otherwise,
     * propagates the Cardboard trigger to the {@link #menu} so that it can determine which UI component
     * to trigger.
     */
    public void onCardboardTrigger() {
        if (!isRecycled && menu != null) {
            if (menu.isVisible()) {
                menu.onCardboardTrigger();
            } else {
                menu.setVisible(true);
                selectionDot.setVisible(true);
            }
        }
    }

    /**
     * Called for every {@link VRViewRenderer#onDrawEye(Eye)}. Executes drawing actions for the {@link #menu}
     * and centers the {@link #selectionDot}.
     * @param vrViewRenderer the {@link VRViewRenderer} drawing.
     */
    public void onDrawing(@NonNull VRViewRenderer vrViewRenderer) {
        if(!isRecycled) {
            if (menu != null && menu.isVisible()) {
                menu.onDrawing(vrViewRenderer);
            }
            if (selectionDot != null) {
                centerSelectionDot(vrViewRenderer);
            }
        }
    }

    /**
     * Centers the {@link #selectionDot} on the screen
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
     * {@inheritDoc Recyclable}
     */
    @Override
    public void recycle() {
        isRecycled = true;
        menu.recycle();

        if(vrImage == null || vrImage.getVrImageType().equals(VRImageType.CUBIC)){
            if (mSkyboxTexture != null) {
                mSkyboxTexture.shouldRecycle(true);
                TextureManager.getInstance().removeTexture(mSkyboxTexture);
                mSkyboxTexture = null;
            }
        } else if (vrImage.getVrImageType().equals(VRImageType.EQUIRECTANGULAR)){
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
}
