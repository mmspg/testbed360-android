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

package ch.epfl.mmspg.testbed360;

import android.support.annotation.NonNull;
import android.util.Log;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.Renderer;

import java.io.IOException;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

/**
 * Last {@link VRScene} to be displayed on the app once a {@link ch.epfl.mmspg.testbed360.image.ImagesSession}
 * has been finished. Prompts the user with a text thanking him !
 * {@link VRScene#MODE_TRAINING}
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 09/12/2017
 */

public final class EndScene extends VRScene {
    private final static String TAG = "EndScene";

    public EndScene(@NonNull Renderer renderer) {
        super(renderer, VRImage.Default.INSTANCE, MODE_TRAINING);
        selectionDot.setVisible(true);
    }

    @Override
    protected void initMenu(@NonNull final Renderer renderer) {
        menu = VRMenuFactory.buildEndMenu(renderer);
        menu.setVisible(true);
        addChild(menu);
    }
}
