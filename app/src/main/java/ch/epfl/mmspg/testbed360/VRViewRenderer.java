// Copyright (C) 2017 ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland
// Multimedia Signal Processing Group
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.vr.renderer.VRRenderer;

/**
 * Custom implementation of Rajawali's {@link VRRenderer}. Some custom methods are necessary to have
 * our {@link VRScene} logic working as intended, but also to fix some issues that are not yet addressed
 * by the library's developers.
 * This component is to be used as a Android UI fullscreen component that handles alone the VR part
 * of this project (camera angle, lens distortion according to the used Cardboard viewer, etc...),
 * letting us only care about what we want to display !
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 19/10/2017
 */
public class VRViewRenderer extends VRRenderer {

    private final static String TAG = "VRViewRenderer";

    private Vector3 mForwardVec = new Vector3(0);
    private Vector3 mHeadTranslation = new Vector3(0);

    public VRViewRenderer(Context context) {
        super(context);
    }

    /**
     * Override the {@link VRRenderer#initScene()} method to start with a new {@link WelcomeScene},
     * and centers the {@link #getCurrentCamera()}.
     */
    @Override
    public void initScene() {
        switchScene(new WelcomeScene(this));

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(100);
    }

    /**
     * Overrides the {@link VRRenderer#onDrawEye(Eye)} to allow updating our VR UI (represented by
     * {@link ch.epfl.mmspg.testbed360.ui.VRMenu} and {@link ch.epfl.mmspg.testbed360.ui.VRButton} inside
     * our {@link VRScene}s; but this also addresses an issue with the gyroscopic controls being inverted.
     *
     * @param eye the eye currently drawn
     */
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

    /**
     * Does quite the same as {@link VRRenderer#isLookingAtObject(Object3D, float)}, but follows the
     * fix done in {@link #onDrawEye(Eye)} for the inverted camera issue so that the computation done
     * in this method is correct !
     */
    @Override
    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        mHeadViewQuaternion.fromMatrix(mHeadViewMatrix);

        //here we override this method to remove this inversion as we already inverse the
        //camera orientation in onDrawEye
        //mHeadViewQuaternion.inverse();

        mForwardVec.setAll(0, 0.1, 1);
        mForwardVec.rotateBy(mHeadViewQuaternion);

        mHeadTranslation.setAll(mHeadViewMatrix.getTranslation());
        mHeadTranslation.subtract(target.getPosition());
        mHeadTranslation.normalize();

        return mHeadTranslation.angle(mForwardVec) < maxAngle;
    }

    protected Matrix4 getMHeadViewMatrix() {
        return mHeadViewMatrix;
    }

    /**
     * This method must be redefined but is not useful in our implementation
     */
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
     * Propagates the Cardboard trigger to the current scene
     */
    public void onCardboardTrigger() {
        Log.d(TAG, "Cardboard trigger");
        if (getCurrentVRScene() != null) {
            getCurrentVRScene().onCardboardTrigger();
        }
    }

    /**
     * Fetches the current scene only if it's a {@link VRScene} (which is always the case but better
     * safe than sorry)
     *
     * @return the current {@link VRScene} or null if the {@link android.transition.Scene} in not an
     * instance of {@link VRScene}
     */
    @Nullable
    private VRScene getCurrentVRScene() {
        if (getCurrentScene() instanceof VRScene) {
            return (VRScene) getCurrentScene();
        }
        return null;
    }
}
