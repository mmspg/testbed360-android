package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

public class VRViewRenderer extends VRRenderer {
    private Sphere sphere;
    double speed = 0.05;
    double sphereRotation = 0;
    private float[] mHeadView;

    public VRViewRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        sphere = createPhotoSphereWithTexture(new Texture("photo", R.drawable.jvet_kiteflite_equirec_3000x1500_raw_q00));
        mHeadView = new float[16];

        getCurrentScene().addChild(sphere);

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(100);
        getCurrentCamera().resetCameraOrientation();
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

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
