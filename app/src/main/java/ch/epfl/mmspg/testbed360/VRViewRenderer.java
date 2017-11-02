package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

import java.io.IOException;
import java.util.Stack;

public class VRViewRenderer extends VRRenderer {

    private final static String TAG = "VRViewRenderer";

    private Vector3 mForwardVec = new Vector3(0);
    private Vector3 mHeadTranslation = new Vector3(0);

    public VRViewRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        switchScene(new WelcomeScene(this));

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(100);
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

        if (getCurrentVRScene() != null) {
            getCurrentVRScene().onDrawing(this);
        }

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

    protected Matrix4 getMHeadViewMatrix(){
        return mHeadViewMatrix;
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
        if (getCurrentVRScene() != null) {
            getCurrentVRScene().onCardboardTrigger();
        }
    }

    @Nullable
    private VRScene getCurrentVRScene() {
        if (getCurrentScene() instanceof VRScene) {
            return (VRScene) getCurrentScene();
        }
        return null;
    }
}
