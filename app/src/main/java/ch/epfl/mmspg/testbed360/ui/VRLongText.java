// Copyright (C) 2017 ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland
// Multimedia Signal Processing Group
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

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
