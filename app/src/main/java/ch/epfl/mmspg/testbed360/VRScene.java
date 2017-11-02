package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.io.IOException;

import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.ui.VRButton;
import ch.epfl.mmspg.testbed360.ui.VRMenu;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

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
    protected VRMenu menu;

    protected Sphere selectionDot;
    double[] newDotPos = new double[4];
    double[] initDotPos = {0, 0, -3, 1.0f};
    double[] headViewMatrix_inv = new double[16];
    Matrix4 headViewMatrix = new Matrix4();

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
        }
        initMenu(renderer);
        initSelectionDot();
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

    protected void initMenu(Renderer renderer) {
        if (vrImage.getGrade() == null) {
            //evaluation
            //TODO implement evaluation menu
            menu = VRMenuFactory.buildTrainingGradeMenu(renderer, vrImage);
            menu.setVisible(false);
            addChild(menu);
        }else {
            menu = VRMenuFactory.buildTrainingGradeMenu(renderer, vrImage);
            menu.setVisible(false);
            addChild(menu);
        }

    }

    public void onCardboardTrigger() {
        if (menu != null) {
            if (menu.isVisible()) {
                menu.onCardboardTrigger();
            } else {
                menu.setVisible(true);
                selectionDot.setVisible(true);
            }
        }
    }

    public void onDrawing(@NonNull VRViewRenderer vrViewRenderer) {
        if (menu != null && menu.isVisible()) {
            menu.onDrawing(vrViewRenderer);
        }
        if (selectionDot != null) {
            centerSelectionDot(vrViewRenderer);
        }
    }

    private void centerSelectionDot(@NonNull VRViewRenderer renderer) {
        headViewMatrix.setAll(renderer.getMHeadViewMatrix());
        headViewMatrix = headViewMatrix.inverse();
        double[] headViewMatrixInv = new double[16];
        headViewMatrix.toArray(headViewMatrixInv);

        Matrix.multiplyMV(newDotPos, 0, headViewMatrixInv, 0, initDotPos, 0);
        selectionDot.setPosition(newDotPos[0], newDotPos[1], newDotPos[2]);

        selectionDot.setLookAt(getCamera().getPosition());
    }
}
