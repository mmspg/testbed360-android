package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 26/09/2017
 */

public class VRGLSurfaceView extends GLSurfaceView {

    private final VRGLRenderer mRenderer;

    public VRGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new VRGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}