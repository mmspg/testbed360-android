package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

import java.util.Stack;

public class VRViewRenderer extends VRRenderer {
    //TODO remove this in production, only for debugging
    private final static boolean ENABLE_TOASTS = true;
    private final static boolean RENDER_AXIS = true;

    private final static String TAG = "VRViewRenderer";
    final static int MODE_EQUIRECTANGULAR = 0;
    final static int MODE_CUBIC = 1;

    private int mode = MODE_EQUIRECTANGULAR;
    private Sphere sphere;

    private Vibrator vibrator;

    private RectangularPrism textPrism;
    private ATexture currentTextTexture;
    private Material textPrismMaterial;

    public VRViewRenderer(Context context) {
        super(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void initScene() {
        sphere = createPhotoSphereWithTexture(new Texture("photo", R.drawable.jvet_kiteflite_equirec_3000x1500_raw_q00));
        getCurrentScene().addChild(sphere);

        if(RENDER_AXIS) {
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.X, Color.RED));
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.Y, Color.GREEN));
            getCurrentScene().addChild(createLine(Vector3.ZERO, Vector3.Z, Color.BLUE));
        }

        try {
            getCurrentScene().setSkybox(R.drawable.jvet_kiteflite_cmp_3000x2250_raw_q00);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }

        textPrismMaterial = new Material();
        textPrismMaterial.setColorInfluence(0);

        textPrism = new RectangularPrism(10f, 5f, 0.4f);
        textPrism.setPosition(0, 0, -10);
        textPrism.setMaterial(textPrismMaterial);
        textPrism.setVisible(true);
        textPrism.rotate(Vector3.Axis.Y,180);
        textPrism.setTransparent(true);

        getCurrentScene().addChild(textPrism);

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(100);

        setText("Equirectangular");
    }

    private static Sphere createPhotoSphereWithTexture(ATexture texture) {

        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1); //otherwise image is inverted
        sphere.setMaterial(material);

        return sphere;
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
            setText("Cubic");
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
        setText("Equirectangular");
    }

    int getMode() {
        return mode;
    }

    public void setText(String text){
        if(textPrismMaterial != null && currentTextTexture != null){
            textPrismMaterial.removeTexture(currentTextTexture);
        }

        try {
            int color = Color.argb(55,55,55,55);
            currentTextTexture = new Texture("text", textAsBitmap(text, 50, Color.WHITE, color));
            textPrismMaterial.addTexture(currentTextTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap textAsBitmap(String text, float textSize, int color, int bgColor) {
        Bitmap mBtnBitmap = Bitmap.createBitmap(512, 256, Bitmap.Config.ARGB_8888);
        Canvas btnCanvas = new Canvas(mBtnBitmap);
        Paint btnPaint = new Paint();
        btnPaint.setColor(bgColor);
        btnPaint.setStyle(Paint.Style.FILL);
        btnCanvas.drawRect(0, 0, 512, 512, btnPaint);
        btnPaint.setColor(color);
        btnPaint.setTextSize(textSize);
        btnCanvas.drawText(text, 12, 40, btnPaint);
        return mBtnBitmap;
    }

    private static Line3D createLine(Vector3 p1, Vector3 p2, int color){
        Stack<Vector3> points = new Stack<>();
        points.add(p1);
        points.add(p2);
        points.add(new Vector3(0,0,-10));

        Line3D line = new Line3D(points,5f,color);
        Material material = new Material();
        material.setColor(color);
        line.setMaterial(material);
        return line;
    }
}
