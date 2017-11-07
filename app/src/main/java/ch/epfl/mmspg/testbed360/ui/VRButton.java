package ch.epfl.mmspg.testbed360.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.support.annotation.NonNull;
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

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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

public class VRButton extends RectangularPrism implements VRUI {
    private final static String TAG = "VRButton";

    private final static int BUTTON_BG_COLOR = Color.argb(45, 55, 55, 55);
    private final static int BUTTON_HOVER_BG_COLOR = Color.argb(160, 45, 45, 45);
    private final static int BUTTON_SELECTED_BG_COLOR = Color.argb(255, 35, 35, 35);

    private final static int VIBRATION_HOVER_MS = 20;
    private final static int VIBRATION_PRESS_MS = 50;

    private final static int CANVAS_WIDTH = 1024;
    private final static int CANVAS_HEIGHT = 256;

    private final static Set<SoftReference<Bitmap>> mReusableBitmaps = new HashSet<>();


    private static int BUTTON_COUNTER = 0;

    private SoftReference<Bitmap> bitmapTexture;
    private SoftReference<Texture> texture;

    private View layoutView;
    private TextView textView;
    private String buttonId;

    private float width;
    private float height;

    private String text;

    private boolean isHovered = false;
    private boolean isSelected = false;
    private boolean isSelectable = true;
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

        bitmapTexture = new SoftReference<Bitmap>(findUsableBitmap());
        texture = new SoftReference<Texture>(new Texture(buttonId, bitmapTexture.get()));
        Material prismMaterial = new Material();
        prismMaterial.setColorInfluence(0);

        prismMaterial.addTexture(texture.get());
        setMaterial(prismMaterial);

        setText(text);
        redraw();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private static Bitmap findUsableBitmap() {
        Bitmap bitmap = null;

        if (!mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();
                    if (null != item && item.isMutable()) {
                        bitmap = item;
                        // Remove from reusable set so it can't be used again.
                        iterator.remove();
                        break;
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        if (bitmap == null) {
            return Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
        }
        return bitmap;
    }

    /**
     * Updates the text of the button with a new text, triggering a redraw
     *
     * @param newText the new text to set
     */
    public void setText(String newText) {
        boolean needRedraw = this.text == null || !this.text.equals(newText);
        this.text = newText;
        textView.setText(text);

        if (needRedraw) {
            redraw();
        }
    }

    /**
     * Redraws the Bitmap linked to the button {@link #texture}. Can be used when changing text
     * or changing the backround color
     */
    private void redraw() {
        if (bitmapTexture.get().isMutable()) {
            Canvas buttonCanvas = new Canvas(bitmapTexture.get());
            buttonCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            layoutView.draw(buttonCanvas);
            TextureManager.getInstance().replaceTexture(texture.get());
        }
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
            if (!isSelected) {
                setBackground(BUTTON_HOVER_BG_COLOR);
            }
        } else if (this.isHovered && !isHovered) {
            if (!isSelected) {
                setBackground(BUTTON_BG_COLOR);
            }
        }
        this.isHovered = isHovered;
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

    /**
     * Updates the {@link #layoutView}'s background color if its not null, and triggers a {@link #redraw()}
     *
     * @param color the new background color, should be of type {@link Bitmap.Config#ARGB_8888}
     */
    private void setBackground(int color) {
        if (layoutView != null) {
            layoutView.setBackgroundColor(color);
            redraw();
        }
    }

    public void setSelected(boolean selected) {
        if (isSelectable) {
            isSelected = selected;
            setBackground(isSelected ? BUTTON_SELECTED_BG_COLOR : BUTTON_HOVER_BG_COLOR);
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
            return parentMenu.getPosition().clone().add(super.getPosition());
        }
        return super.getPosition();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
    }

    /**
     * Checks whether the button is being pressed (i.e. {@link #isHovered}, as we now there was a
     * trigger/touch done) and execute the {@link #onTriggerAction} associated to it.
     *
     * @return true if the event was consumed, false otherwise. This should be checked to not propagate
     * the trigger uselessly to other buttons
     */
    @Override
    public boolean onCardboardTrigger() {
        if (isHovered) {
            if (isSelectable) {
                vibrate(VIBRATION_PRESS_MS);

                setSelected(!isSelected);
                if (onTriggerAction != null) {
                    try {
                        onTriggerAction.call();
                    } catch (Exception e) {
                        Log.e(TAG, buttonId + ": error executing onTriggerAction");
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawing(@NonNull VRViewRenderer vrViewRenderer) {
        //nothing to do !
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recycle() {
        mReusableBitmaps.add(bitmapTexture);
        TextureManager.getInstance().removeTexture(texture.get());
        texture.clear();
        layoutView = null;
        textView = null;
    }
}
