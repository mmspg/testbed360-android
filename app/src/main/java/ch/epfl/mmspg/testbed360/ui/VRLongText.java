package ch.epfl.mmspg.testbed360.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.materials.textures.ATexture;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 06/12/2017
 */

public class VRLongText extends VRButton implements VRUI {
    private final static int MAX_PAGE = 100;
    private int paddingBottom = 0;
    private boolean canScrollUp = false;
    private boolean canScrollDown = true;

    /**
     * Creates a {@link VRButton}
     *
     * @param context context to load the default button layout from
     * @param text    the initial text we want to set
     * @throws ATexture.TextureException in case there was an error binding the initial texture
     */
    public VRLongText(@NonNull Context context, @Nullable String text) throws ATexture.TextureException {
        super(context, text, true);
    }

    public void scrollDown() {
        paddingBottom += textView.getHeight();
        canScrollDown = true;
        canScrollUp = true;

        if (paddingBottom >= MAX_PAGE*textView.getHeight()) {
            paddingBottom = MAX_PAGE*textView.getHeight();
            canScrollDown = false;
        }
        textView.scrollTo(0,paddingBottom);
        //textView.setTranslationY(paddingBottom);
        redraw();
    }

    public void scrollUp() {
        paddingBottom -= textView.getHeight();
        canScrollUp = true;
        canScrollDown = true;

        if (paddingBottom < 0) {
            paddingBottom = 0;
            canScrollUp = false;
        }
        //textView.setTranslationY(paddingBottom);
        textView.scrollTo(0,paddingBottom);
        redraw();
    }

    public boolean canScrollUp() {
        return canScrollUp;
    }

    public boolean canScrollDown() {
        return canScrollDown;
    }
}
