package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.vr.renderer.VRRenderer;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.ui.VRButton;
import ch.epfl.mmspg.testbed360.ui.VRMenu;

public class VRViewRenderer extends VRRenderer {
    //TODO remove or set false this in production, only for debugging
    private final static boolean ENABLE_TOASTS = true;
    private final static boolean RENDER_AXIS = true;

    private final static String TAG = "VRViewRenderer";
    private final static int MODE_EQUIRECTANGULAR = 0;
    private final static int MODE_CUBIC = 1;

    private int mode = MODE_EQUIRECTANGULAR;
    private Sphere sphere;
    private VRMenu menu;

    private Vector3 mForwardVec = new Vector3(0);
    private Vector3 mHeadTranslation = new Vector3(0);

    public VRViewRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        initSphere();
        initSkyBox();
        initMenu();
        initAxis();

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(100);
    }

    private void initSphere() {

        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(new Texture("photo", R.drawable.jvet_kiteflite_equirec_3000x1500_raw_q00));
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1); //otherwise image is inverted
        sphere.setMaterial(material);

        getCurrentScene().addChild(sphere);
    }

    private void initAxis() {
        if (RENDER_AXIS) {
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.X, Color.RED));
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.Y, Color.GREEN));
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.Z, Color.BLUE));
        }
    }

    private void initSkyBox() {
        try {
            getCurrentScene().setSkybox(BitmapUtils.loadCubicMap(getContext(), R.drawable.jvet_kiteflite_cubemap32_2250x1500_raw_q00));
        } catch (IOException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }
    }

    private void initMenu() {
        menu = new VRMenu(20);

        try {
            VRButton button = new VRButton(getContext(), "Equirectangular", 10f, 2f);
            //button.setPosition(0, 0, -20);
            button.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    if (getMode() == VRViewRenderer.MODE_EQUIRECTANGULAR) {
                        setCubicMode();
                    } else if (getMode() == VRViewRenderer.MODE_CUBIC) {
                        setEquirectangularMode();
                    }
                    return null;
                }
            });
            menu.addButton(button);

            final VRButton fpsButton = new VRButton(getContext(),
                    "This is a test ! No action if pressed",
                    10f,
                    2f);
            fpsButton.setName("FPSButton");
            setFPSUpdateListener(new OnFPSUpdateListener() {
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

        getCurrentScene().addChild(menu);

    }

    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().updatePerspective(
                eye.getFov().getLeft(),
                eye.getFov().getRight(),
                eye.getFov().getBottom(),
                eye.getFov().getTop()
        );
        mCurrentEyeMatrix.setAll(eye.getEyeView());
        mCurrentEyeOrientation.fromMatrix(mCurrentEyeMatrix.inverse());

        //the call to .inverse() here fixes the inverted orientation of the view
        //see suggestion I made at https://github.com/Rajawali/Rajawali/issues/1935
        getCurrentCamera().setOrientation(mCurrentEyeOrientation);
        getCurrentCamera().setPosition(mCameraPosition);
        getCurrentCamera().getPosition().add(mCurrentEyeMatrix.getTranslation());
        menu.onDrawing(this);
        super.onRenderFrame(null);
    }

    @Override
    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        mHeadViewQuaternion.fromMatrix(mHeadViewMatrix);

        //here we override this method to remove this inversion as we already inverse the
        //camera orientation in onDrawEye
        //mHeadViewQuaternion.inverse();

        mForwardVec.setAll(0, 0, 1);
        mForwardVec.rotateBy(mHeadViewQuaternion);

        mHeadTranslation.setAll(mHeadViewMatrix.getTranslation());
        mHeadTranslation.subtract(target.getPosition());
        mHeadTranslation.normalize();

        return mHeadTranslation.angle(mForwardVec) < maxAngle;
    }


    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {

    }

    /**
     * This method must be redefined but is never called when the screen is touched. In fact, it's
     * because the detection is done in {@link VRViewActivity#onCardboardTrigger()}.
     */
    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    public void onCardboardTrigger() {
        Log.d(TAG, "Cardboard trigger");
        if (menu != null) {
            menu.onCardboardTrigger();
        }
    }

    /**
     * Changes the mode of projection to cubic. Basically just changes the
     * {@link VRViewRenderer#mode} value to {@link VRViewRenderer#MODE_CUBIC} and sets
     * the sphere as invisible
     */
    private void setCubicMode() {
        Log.i(TAG, "Changing mode to : CUBIC_MODE");

        mode = MODE_CUBIC;
        sphere.setVisible(false);
        menu.getButton(0).setText("Cubic");
    }

    /**
     * Changes the mode of projection to equirectangular. Basically just changes the
     * {@link VRViewRenderer#mode} value to {@link VRViewRenderer#MODE_EQUIRECTANGULAR} and sets
     * the sphere as visible
     */
    private void setEquirectangularMode() {
        Log.i(TAG, "Changing mode to : EQUIRECTANGULAR");

        mode = MODE_EQUIRECTANGULAR;
        sphere.setVisible(true);
        menu.getButton(0).setText("Equirectangular");
    }

    public int getMode() {
        return mode;
    }


    private static Line3D createLine(Vector3 p1, Vector3 p2, int color) {
        Stack<Vector3> points = new Stack<>();
        points.add(p1);
        points.add(p2);

        Line3D line = new Line3D(points, 2f, color);
        Material material = new Material();
        material.setColor(color);
        line.setMaterial(material);
        line.moveUp(-5);
        line.moveRight(-5);
        line.moveForward(-5);
        return line;
    }
}
