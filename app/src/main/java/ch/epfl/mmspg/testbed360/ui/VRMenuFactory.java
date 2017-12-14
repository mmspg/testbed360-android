package ch.epfl.mmspg.testbed360.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.util.EmptyStackException;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.EndScene;
import ch.epfl.mmspg.testbed360.R;
import ch.epfl.mmspg.testbed360.TrainingDoneScene;
import ch.epfl.mmspg.testbed360.VRScene;
import ch.epfl.mmspg.testbed360.VRViewActivity;
import ch.epfl.mmspg.testbed360.image.ImageGrade;
import ch.epfl.mmspg.testbed360.image.VRImage;

/**
 * This class' goal is to provide methods for building {@link VRMenu} that we use multiple times
 * through the app.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 31/10/2017
 */

public final class VRMenuFactory {

    //TODO remove or set false this in production, only for debugging
    private final static boolean RENDER_FPS = false;

    private VRMenuFactory() {
        //do nothing, this constructor is private to follow the Factory Pattern
    }

    /**
     * Builds the {@link VRMenu} displayed when the user starts the app. It explains what we wait from
     * him/her, and has a {@link VRButton} to start the training session
     *
     * @param renderer the {@link Renderer} used to switch between {@link org.rajawali3d.scene.Scene}
     * @return the initialized and ready to use {@link VRMenu}
     */
    @NonNull
    public static VRMenu buildWelcomeMenu(@NonNull final Renderer renderer) {
        VRMenu menu = new VRMenu();

        try {
            if (RENDER_FPS) {
                menu.addButton(buildFPSButton(renderer));
            }
            final VRLongText text = new VRLongText(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.welcome_long_text)
            );

            final VRButton startButton = new VRButton(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.start_training),
                    false
            );
            startButton.setName("StartButton");
            startButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        VRImage next = VRViewActivity.nextTraining();
                        ((VRScene) renderer.getCurrentScene()).recycle();
                        renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_TRAINING));
                    } catch (EmptyStackException e) {
                        startButton.setText(renderer.getContext().getString(R.string.no_new_image));
                    }
                    return null;
                }
            });
            startButton.setEnabled(false);

            final VRButton scrollUpButton = new VRButton(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.up_button),
                    false
            );
            final VRButton scrollDownButton = new VRButton(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.down_button),
                    false
            );

            scrollUpButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    text.scrollUp();
                    //TODO implement way to disable button
                    scrollUpButton.setEnabled(text.canScrollUp());
                    scrollDownButton.setEnabled(text.canScrollDown());
                    return null;
                }
            });
            scrollUpButton.setEnabled(false);

            scrollDownButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    text.scrollDown();
                    scrollUpButton.setEnabled(text.canScrollUp());
                    scrollDownButton.setEnabled(text.canScrollDown());
                    if(text.isAllTextRead()){
                        startButton.setEnabled(true);
                    }
                    return null;
                }
            });



            /*final VRButton skipTrainingButton = new VRButton(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.skip_training),
                    false
            );
            skipTrainingButton.setName("skipTrainingButton");
            skipTrainingButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        VRImage next = VRViewActivity.nextEvaluation();
                        ((VRScene) renderer.getCurrentScene()).recycle();
                        renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_EVALUATION));
                    } catch (EmptyStackException e) {
                        startButton.setText(renderer.getContext().getString(R.string.no_new_image));
                    }
                    return null;
                }
            });*/
            menu.addAllButtons(
                    scrollUpButton,
                    text,
                    scrollDownButton,
                    //skipTrainingButton,
                    startButton
            );
            menu.setY(5);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }

    /**
     * Builds the {@link VRMenu} displayed when the user has finished a {@link ch.epfl.mmspg.testbed360.image.ImagesSession}.
     *
     * @param renderer the {@link Renderer} used to switch between {@link org.rajawali3d.scene.Scene}
     * @return the initialized and ready to use {@link VRMenu}
     */
    @NonNull
    public static VRMenu buildEndMenu(@NonNull final Renderer renderer) {
        VRMenu menu = new VRMenu();

        try {

            final VRLongText text = new VRLongText(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.end_long_text)
            );
            menu.addAllButtons(
                    text
            );
            menu.setY(5);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }

    /**
     * Builds the {@link VRMenu} displayed when the user has finished the {@link VRScene#MODE_TRAINING}
     * of an {@link ch.epfl.mmspg.testbed360.image.ImagesSession}
     *
     * @param renderer the {@link Renderer} used to switch between {@link org.rajawali3d.scene.Scene}
     * @return the initialized and ready to use {@link VRMenu}
     */
    @NonNull
    public static VRMenu buildTrainingDoneMenu(@NonNull final Renderer renderer) {
        VRMenu menu = new VRMenu();

        try {

            final VRLongText text = new VRLongText(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.training_done_text)
            );
            final VRButton startButton = new VRButton(
                    renderer.getContext(),
                    renderer.getContext().getString(R.string.start),
                    false
            );
            startButton.setName("StartButton");
            startButton.setOnTriggerAction(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        VRImage next = VRViewActivity.nextEvaluation();
                        ((VRScene) renderer.getCurrentScene()).recycle();
                        renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_EVALUATION));
                    } catch (EmptyStackException e2) {
                        startButton.setText("No new image");
                    }
                    return null;
                }
            });
            menu.addAllButtons(
                    text,
                    startButton
            );
            menu.setY(5);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }

    /**
     * Build a {@link VRButton} that displays the fPS in real time of the {@link Renderer}. As
     * the {@link VRButton#redraw()} method is quite costly, we round to the nearest 0.5 so that
     * we don't loose some FPS while displaying them...
     *
     * @param renderer the {@link Renderer} on which we monitor FPS
     * @return the ready to use {@link VRButton}, which is no selectable. (see {@link VRButton#isClickable}
     * @throws ATexture.TextureException if there was a texturing error while constructing the button
     */
    @NonNull
    private static VRButton buildFPSButton(@NonNull final Renderer renderer) throws ATexture.TextureException {
        final VRButton fpsButton = new VRButton(renderer.getContext(),"",false);
        fpsButton.setName("FPSButton");
        renderer.setFPSUpdateListener(new OnFPSUpdateListener() {
            @Override
            public void onFPSUpdate(double fps) {
                //pretty sure we have to divide per two because this method was thought
                //for non VR rendering, hence we render twice as much image, which would give
                //us here ~120FPS which seems way too much!
                fpsButton.setText("FPS:" + Math.round(fps) / 2.0);
            }
        });
        fpsButton.setSelectable(false);
        return fpsButton;
    }

    /**
     * Builds a {@link VRMenu} corresponding to a {@link VRImage} from the training session. It directly
     * displays the grade of the given {@link VRImage} in a {@link VRButton}, which when clicked on
     * will chain call the next {@link VRScene}
     *
     * @param renderer the {@link Renderer} used to switch between {@link org.rajawali3d.scene.Scene}
     * @return the initialized and ready to use {@link VRMenu}
     */
    @NonNull
    public static VRMenu buildTrainingGradeMenu(@NonNull final Renderer renderer, @Nullable VRImage img) {
        VRMenu menu = new VRMenu();
        menu.setY(4);

        try {
            for (ImageGrade grade : ImageGrade.values()) {
                if (!grade.equals(ImageGrade.NONE)) {
                    final VRButton button = new VRButton(
                            renderer.getContext(),
                            grade.toString(renderer.getContext()),
                            false
                    );
                    button.setClickable(false);
                    if (img != null && img.getGrade().equals(grade)) {
                        button.setClickable(true);
                        button.setSelectable(true);
                        button.setSelected(true);
                        button.setOnTriggerAction(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                try {
                                    VRImage next = VRViewActivity.nextTraining();
                                    ((VRScene) renderer.getCurrentScene()).recycle();
                                    renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_TRAINING));
                                } catch (EmptyStackException e) {
                                    ((VRScene) renderer.getCurrentScene()).recycle();
                                    renderer.switchScene(new TrainingDoneScene(renderer));
                                }
                                return null;
                            }
                        });
                    }
                    menu.addButton(button);
                }
            }
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }

    /**
     * Builds a {@link VRMenu} corresponding to a {@link VRImage} from the evaluation session.
     * Setting a grade triggers the next {@link VRScene}
     *
     * @param renderer the {@link Renderer} used to switch between {@link org.rajawali3d.scene.Scene}
     * @return the initialized and ready to use {@link VRMenu}
     */
    @NonNull
    public static VRMenu buildEvaluationGradeMenu(@NonNull final Renderer renderer, @NonNull final VRScene scene) {
        VRMenu menu = new VRMenu();
        menu.setY(4);

        try {
            for (final ImageGrade grade : ImageGrade.values()) {
                if (!grade.equals(ImageGrade.NONE)) {
                    final VRButton button = new VRButton(
                            renderer.getContext(),
                            grade.toString(renderer.getContext()),
                            false
                    );
                    button.setOnTriggerAction(new Callable() {
                        @Override
                        public Object call() throws Exception {
                            try {
                                scene.setGrade(grade);
                                VRImage next = VRViewActivity.nextEvaluation();
                                ((VRScene) renderer.getCurrentScene()).recycle();
                                renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_EVALUATION));
                            } catch (EmptyStackException e) {
                                ((VRScene) renderer.getCurrentScene()).recycle();
                                renderer.switchScene(new EndScene(renderer));
                            }
                            return null;
                        }
                    });
                    menu.addButton(button);
                }
            }
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        return menu;
    }
}
