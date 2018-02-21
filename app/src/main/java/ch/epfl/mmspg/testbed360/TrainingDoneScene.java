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

import org.rajawali3d.renderer.Renderer;

import java.io.IOException;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.image.VRImage;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

/**
 * {@link VRScene} to be displayed when the {@link VRScene#MODE_TRAINING} is finished. Is a transition
 * to the {@link VRScene#MODE_EVALUATION}.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 09/12/2017
 */

public final class TrainingDoneScene extends VRScene {
    private final static String TAG = "TrainingDoneScene";

    public TrainingDoneScene(@NonNull Renderer renderer) {
        super(renderer, VRImage.Default.INSTANCE, MODE_TRAINING);
        selectionDot.setVisible(true);
    }

    @Override
    protected void initMenu(@NonNull final Renderer renderer) {
        menu = VRMenuFactory.buildTrainingDoneMenu(renderer);
        menu.setVisible(true);
        addChild(menu);
    }
}
