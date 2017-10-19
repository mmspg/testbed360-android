package ch.epfl.mmspg.testbed360;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

public class EquirectangularActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equirectangular);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.jvet_kiteflite_equirec_3000x1500_raw_q00);
        VrPanoramaView view = (VrPanoramaView) findViewById(R.id.pano_view);
        VrPanoramaView.Options options = new VrPanoramaView.Options();
        options.inputType = VrPanoramaView.Options.TYPE_MONO;
        view.setDisplayMode(VrPanoramaView.DisplayMode.FULLSCREEN_STEREO);
        view.loadImageFromBitmap(bm,options);
    }
}
