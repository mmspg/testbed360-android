package ch.epfl.mmspg.testbed360.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.RectangularPrism;

import ch.epfl.mmspg.testbed360.R;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 20/10/2017
 */

public class VRButton extends RectangularPrism {
    private final static String TAG = "VRButton";

    private final static int BUTTON_BG_COLOR = Color.argb(55, 55, 55, 55);
    private final static int BUTTON_HOVER_BG_COLOR = Color.argb(180, 45, 45, 45);

    private final static int VIBRATION_HOVER_MS = 20;
    private final static int VIBRATION_PRESS_MS = 100;

    private final static int CANVAS_WIDTH = 1024;
    private final static int CANVAS_HEIGHT = 256;

    private static int TEXTURE_COUNTER = 0;

    private Texture currentBMPTexture;
    private View layoutView;
    private TextView textView;

    private String text;

    private boolean isHovered = false;
    private Vibrator vibrator;

    public VRButton(Context context, String text, float width, float height) throws ATexture.TextureException {
        super(width, height, 0f);

        setVisible(true);
        rotate(Vector3.Axis.Y, 180);
        setTransparent(true);


        layoutView = LayoutInflater
                .from(context)
                .inflate(R.layout.vr_button_layout, new LinearLayout(context), false);
        layoutView.setBackgroundColor(BUTTON_BG_COLOR);
        layoutView.layout(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        textView = (TextView) layoutView.findViewById(R.id.vr_button_text);
        textView.setTextSize(24);
        textView.setText(text);
        textView.layout(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        Bitmap bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
        currentBMPTexture = new Texture("button" + TEXTURE_COUNTER++, bitmap);
        Material prismMaterial = new Material();
        prismMaterial.setColorInfluence(0);

        prismMaterial.addTexture(currentBMPTexture);
        setMaterial(prismMaterial);

        setText(text);

    }

    public void setText(String newText) {
        this.text = newText;
        textView.setText(text);
        redraw();
    }

    private void redraw(){
        Canvas buttonCanvas = new Canvas(currentBMPTexture.getBitmap());
        buttonCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        layoutView.draw(buttonCanvas);
        TextureManager.getInstance().replaceTexture(currentBMPTexture);
    }

    public void onHover(boolean isHovered) {

        //here we check the previous status, so that we do not repaint bitmap uselessly
        if (!this.isHovered && isHovered) {
            layoutView.setBackgroundColor(BUTTON_HOVER_BG_COLOR);
            setText(text); //triggers a repaint
            if(vibrator != null){
                vibrator.vibrate(VIBRATION_HOVER_MS);
            }
        } else if (this.isHovered && !isHovered) {
            layoutView.setBackgroundColor(BUTTON_BG_COLOR);
            setText(text); //triggers a repaint
            if(vibrator != null){
                vibrator.vibrate(VIBRATION_HOVER_MS);
            }
        }
        this.isHovered = isHovered;

        //TODO create a field containing an action to execute here
    }

    public void onPressed() {
        //TODO create a field containing an action to execute here
    }

    public void setVibrator(Vibrator vibrator){
        this.vibrator = vibrator;
    }
}
