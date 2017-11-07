package ch.epfl.mmspg.testbed360.ui;

/** A {@link Recyclable} object is an object of the UI that should be recycled after it has been used.
 * It must provide a {@link #recycle()} method that ensures that all resources used internally
 * ({@link android.graphics.Bitmap}, {@link org.rajawali3d.materials.textures.Texture} etc..) are
 * recycled and that the object itself is destroyed, so that it can be garbage collected properly
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 03/11/2017
 */

public interface Recyclable {

    /**
     * Starts the recycling of the internal resources of the {@link Recyclable} object, and the object
     * itself. It should try to free up resources/tag them as reusable, nullify references that should
     * be picked by garbage collector, and destroy children {@link org.rajawali3d.Object3D}.
     * When it returns, the {@link Recyclable} object should'nt be used anymore.
     */
    public void recycle();
}
