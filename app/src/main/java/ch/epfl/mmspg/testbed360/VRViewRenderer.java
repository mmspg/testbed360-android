package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

import java.util.Stack;

import ch.epfl.mmspg.testbed360.ui.VRButton;

public class VRViewRenderer extends VRRenderer {
    //TODO remove this in production, only for debugging
    private final static boolean ENABLE_TOASTS = true;
    private final static boolean RENDER_AXIS = true;

    private final static int BUTTON_BG_COLOR = Color.argb(55, 55, 55, 55);
    private final static int BUTTON_HOVER_BG_COLOR = Color.argb(180, 45, 45, 45);


    private final static int CANVAS_WIDTH = 1024;
    private final static int CANVAS_HEIGHT = 512;

    private final static String TAG = "VRViewRenderer";
    final static int MODE_EQUIRECTANGULAR = 0;
    final static int MODE_CUBIC = 1;

    private int mode = MODE_EQUIRECTANGULAR;
    private Sphere sphere;
    private VRButton button;

    private Vibrator vibrator;



    public VRViewRenderer(Context context) {
        super(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void initScene() {
        initSphere();
        initSkyBox();
        initButton();
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
            getCurrentScene().setSkybox(R.drawable.jvet_kiteflite_cmp_3000x2250_raw_q00);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }
    }

    private void initButton() {
        try {
            button = new VRButton(getContext(),"Equirectangular",10f, 2f);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        button.setPosition(0, 0, -20);
        button.setVibrator(vibrator);

        getCurrentScene().addChild(button);

    }

    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().updatePerspective(
                eye.getFov().getLeft(),
                eye.getFov().getRight(),
                eye.getFov().getBottom(),
                eye.getFov().getTop());
        mCurrentEyeMatrix.setAll(eye.getEyeView());
        mCurrentEyeOrientation.fromMatrix(mCurrentEyeMatrix);

        //the call to .inverse() here fixes the inverted orientation of the view
        //see suggestion I made at https://github.com/Rajawali/Rajawali/issues/1935
        getCurrentCamera().setOrientation(mCurrentEyeOrientation.inverse());
        getCurrentCamera().setPosition(mCameraPosition);
        getCurrentCamera().getPosition().add(mCurrentEyeMatrix.getTranslation().inverse());
        button.onHover(isLookingAtObject(button));
        super.onRenderFrame(null);
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

    /**
     * Changes the mode of projection to cubic. Basically just changes the
     * {@link VRViewRenderer#mode} value to {@link VRViewRenderer#MODE_CUBIC} and sets
     * the sphere as invisible
     */
    void setCubicMode() {
        vibrator.vibrate(50);
        Log.i(TAG, "Changing mode to : CUBIC_MODE");

        if (ENABLE_TOASTS) {
            Toast.makeText(getContext(), "Cubic", Toast.LENGTH_SHORT).show();
        }

        mode = MODE_CUBIC;
        sphere.setVisible(false);

        try {
            getCurrentScene().updateSkybox(R.drawable.jvet_kiteflite_cmp_3000x2250_raw_q00);
            button.setText("Cubic");
        } catch (Exception e) {
            Log.e(TAG, "Error updating the skybox texture");
            e.printStackTrace();
        }
    }

    /**
     * Changes the mode of projection to equirectangular. Basically just changes the
     * {@link VRViewRenderer#mode} value to {@link VRViewRenderer#MODE_EQUIRECTANGULAR} and sets
     * the sphere as visible
     */
    void setEquirectangularMode() {
        vibrator.vibrate(50);
        Log.i(TAG, "Changing mode to : EQUIRECTANGULAR");

        if (ENABLE_TOASTS) {
            Toast.makeText(getContext(), "Equirectangular", Toast.LENGTH_SHORT).show();
        }

        mode = MODE_EQUIRECTANGULAR;
        sphere.setVisible(true);
        button.setText("Equirectangular");
    }

    int getMode() {
        return mode;
    }


    private static Line3D createLine(Vector3 p1, Vector3 p2, int color) {
        Stack<Vector3> points = new Stack<>();
        points.add(p1);
        points.add(p2);
        points.add(new Vector3(-5, -5, -10));

        Line3D line = new Line3D(points, 2f, color);
        Material material = new Material();
        material.setColor(color);
        line.setMaterial(material);
        return line;
    }
}
