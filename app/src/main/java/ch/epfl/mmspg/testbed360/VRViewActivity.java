package ch.epfl.mmspg.testbed360;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VRViewActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrview);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new VRGLSurfaceView(this);
        setContentView(mGLView);

    }
}
