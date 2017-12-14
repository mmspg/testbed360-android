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
