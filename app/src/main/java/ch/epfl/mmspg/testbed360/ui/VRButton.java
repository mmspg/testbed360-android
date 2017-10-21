package ch.epfl.mmspg.testbed360.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.RectangularPrism;

import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.R;
import ch.epfl.mmspg.testbed360.VRViewRenderer;

/**
 * This class is a helper class to represent UI buttons in the {@link VRViewRenderer}.
 * <p>
 * In order to represent them in 3D, we extend the {@link RectangularPrism} class, except that there
 * is no depth for every {@link VRButton}. This prism has a {@link Texture} assigned to it, namely
 * {@link #texture}.
 * To have a convenient way to build UI, it inflates a layout defined in the resources xml into
 * {@link #layoutView}, which  will define what our button should look like.
 * When we want to set text, it will basically draw the {@link #layoutView} into the
 * {@link #texture}'s {@link Bitmap} using a {@link Canvas}. See {@link #setText(String)}
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 20/10/2017
 */

public class VRButton extends RectangularPrism {
    private final static String TAG = "VRButton";

    private final static float LOOKING_AT_ANGLE = 5;

    private final static int BUTTON_BG_COLOR = Color.argb(55, 55, 55, 55);
    private final static int BUTTON_HOVER_BG_COLOR = Color.argb(180, 45, 45, 45);

    private final static int VIBRATION_HOVER_MS = 20;
    private final static int VIBRATION_PRESS_MS = 50;

    private final static int CANVAS_WIDTH = 1024;
    private final static int CANVAS_HEIGHT = 256;

    private static int BUTTON_COUNTER = 0;

    private Texture texture;
    private View layoutView;
    private TextView textView;
    private String buttonId;

    private float width;
    private float height;

    private String text;

    private boolean isHovered = false;
    private Vibrator vibrator;

    private VRMenu parentMenu;

    private Callable onTriggerAction;

    /**
     * Creates a {@link VRButton}
     *
     * @param context context to load the default button layout from
     * @param text    the initial text we want to set
     * @param width   the width (in OpenGL coordinates) of the button
     * @param height  the height (in OpenGL coordinates) of the button
     * @throws ATexture.TextureException in case there was an error binding the initial texture
     */
    public VRButton(Context context, String text, float width, float height) throws ATexture.TextureException {
        super(width, height, 0f);
        this.width = width;
        this.height = height;

        setVisible(true);
        setTransparent(true);

        buttonId = TAG + BUTTON_COUNTER++;
        Log.d(TAG, "Assigning id " + buttonId + " to button with text " + text);

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
        texture = new Texture(buttonId, bitmap);
        Material prismMaterial = new Material();
        prismMaterial.setColorInfluence(0);

        prismMaterial.addTexture(texture);
        setMaterial(prismMaterial);

        setText(text);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Updates the text of the button with a new text, triggering a redraw
     *
     * @param newText the new text to set
     */
    public void setText(String newText) {
        this.text = newText;
        textView.setText(text);
        redraw();
    }

    /**
     * Redraws the Bitmap linked to the button {@link #texture}. Can be used when changing text
     * or changing the backround color
     */
    private void redraw() {
        Canvas buttonCanvas = new Canvas(texture.getBitmap());
        buttonCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        layoutView.draw(buttonCanvas);
        TextureManager.getInstance().replaceTexture(texture);
    }

    /**
     * Changes the hovered state of the button with the new value, and triggers associated actions.
     *
     * @param isHovered of the button is considered hovered. Usually determined by a {@link VRMenu}
     */
    public void setHovered(boolean isHovered) {
        //here we check the previous status, so that we do not repaint bitmap uselessly
        if (!this.isHovered && isHovered) {
            vibrate(VIBRATION_HOVER_MS);
            layoutView.setBackgroundColor(BUTTON_HOVER_BG_COLOR);
            redraw();
        } else if (this.isHovered && !isHovered) {
            layoutView.setBackgroundColor(BUTTON_BG_COLOR);
            redraw();
        }
        this.isHovered = isHovered;
    }


    /**
     * Checks whether the button is being pressed (i.e. {@link #isHovered}, as we now there was a
     * trigger/touch done) and execute the {@link #onTriggerAction} associated to it.
     *
     * @return true if the event was consumed, false otherwise. This should be checked to not propagate
     * the trigger uselessly to other buttons
     */
    public boolean onCardboardTrigger() {
        if (isHovered) {
            vibrator.vibrate(VIBRATION_PRESS_MS);

            if (onTriggerAction != null) {
                try {
                    onTriggerAction.call();
                } catch (Exception e) {
                    Log.e(TAG, buttonId + ": error executing onTriggerAction");
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    public Callable getOnTriggerAction() {
        return onTriggerAction;
    }

    public void setOnTriggerAction(Callable onTriggerAction) {
        this.onTriggerAction = onTriggerAction;
    }

    private void vibrate(int ms) {
        if (vibrator != null) {
            vibrator.vibrate(ms);
        }
    }

    public VRMenu getParentMenu() {
        return parentMenu;
    }

    public void setParentMenu(VRMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    /**
     * We override this method as if this {@link VRButton} belongs to a {@link VRMenu}, its position
     * is relative to the {@link #parentMenu}'s position. Hence we need to return the addition of
     * both positions. This allows us to have {@link VRViewRenderer#isLookingAtObject(Object3D, float)}
     * working as expected.
     *
     * @return the absolute position of the {@link VRButton} in its world
     */
    @Override
    public Vector3 getPosition() {
        if (parentMenu != null) {
            return new Vector3(
                    parentMenu.getX() + super.getX(),
                    Math.cos(parentMenu.getRotY()) * (parentMenu.getY() + super.getY()),
                    parentMenu.getZ() + super.getZ()
            );
        }
        return super.getPosition();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
