package ch.epfl.mmspg.testbed360.ui;

import android.support.annotation.NonNull;

import com.google.vrtoolkit.cardboard.Eye;

import ch.epfl.mmspg.testbed360.VRViewRenderer;

/** This gives some basic methods that any VR UI components should have in order for the lifecycle of
 * the UI to work as intended.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 07/11/2017
 */

public interface VRUI {
    /**
     * Starts the recycling of the internal resources of the object, and the object
     * itself. It should try to free up resources/tag them as reusable, nullify references that should
     * be picked by garbage collector, and destroy children {@link org.rajawali3d.Object3D}.
     * When it returns, the UI object should'nt be used anymore.
     */
    public void recycle();


    /**
     * Called whenever there was a Cardboard trigger (usually when the user touches the screen with
     * the Cardboard viewer's button). Should propagate the signal if not yet consumed, or handle it
     * @return true if it was consumed, false otherwise
     */
    public boolean onCardboardTrigger();

    /**
     * Called by the {@link VRViewRenderer} at each draw, see {@link VRViewRenderer#onDrawEye(Eye)}.
     * @param vrViewRenderer the {@link VRViewRenderer} drawing.
     */
    public void onDrawing(@NonNull VRViewRenderer vrViewRenderer);

}
