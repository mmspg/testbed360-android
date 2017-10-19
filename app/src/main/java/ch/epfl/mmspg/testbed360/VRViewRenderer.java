package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation.RepeatMode;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class VRViewRenderer extends org.rajawali3d.vr.renderer.VRRenderer {
    private Sphere sphere;

    public VRViewRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo", R.drawable.jvet_kiteflite_equirec_3000x1500_raw_q00));

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
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        boolean isLookingAt = isLookingAtObject(sphere);
        if(isLookingAt) {
            sphere.setColor(Color.RED);
        } else {
            sphere.setColor(Color.YELLOW);
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {

    }

    @Override public void onTouchEvent(MotionEvent event) {

    }
}
