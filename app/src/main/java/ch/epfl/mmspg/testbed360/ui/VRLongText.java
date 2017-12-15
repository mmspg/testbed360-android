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
    private final static int MAX_PAGE = 1;
    private int scrollingAmount = 0;
    private boolean canScrollUp = false;
    private boolean canScrollDown = true;

    private boolean allTextRead = false;

    /**
     * Creates a {@link VRButton}
     *
     * @param context context to load the default button layout from
     * @param text    the initial text we want to set
     * @throws ATexture.TextureException in case there was an error binding the initial texture
     */
    public VRLongText(@NonNull Context context, @Nullable String text) throws ATexture.TextureException {
        super(context, text, true);
        setSelectable(false);
        setClickable(false);
        setHoverable(false);
        moveUp(-STANDARD_BUTTON_HEIGHT*2);
    }

    public void scrollDown() {
        scrollingAmount += textView.getHeight();
        canScrollDown = true;
        canScrollUp = true;

        if (scrollingAmount >= MAX_PAGE*textView.getHeight()) {
            scrollingAmount = MAX_PAGE*textView.getHeight();
            canScrollDown = false;
            allTextRead = true;
        }
        textView.scrollTo(0, scrollingAmount);
        redraw();
    }

    public void scrollUp() {
        scrollingAmount -= textView.getHeight();
        canScrollUp = true;
        canScrollDown = true;

        if (scrollingAmount <= 0) {
            scrollingAmount = 0;
            canScrollUp = false;
        }
        textView.scrollTo(0, scrollingAmount);
        redraw();
    }

    public boolean canScrollUp() {
        return canScrollUp;
    }

    public boolean canScrollDown() {
        return canScrollDown;
    }

    public boolean isAllTextRead() {
        return allTextRead;
    }
}
