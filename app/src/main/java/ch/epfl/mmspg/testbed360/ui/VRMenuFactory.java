package ch.epfl.mmspg.testbed360.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.OnFPSUpdateListener;

import java.util.EmptyStackException;
import java.util.concurrent.Callable;

import ch.epfl.mmspg.testbed360.R;
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
    private final static float STANDARD_BUTTON_WIDTH = 10f;
    private final static float STANDARD_BUTTON_HEIGHT = 2f;

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
            //TODO add the tutorial in this button, and store the string value in xml !
            VRButton welcomeButton = new VRButton(renderer.getContext(), "Welcome !", 10f, 2f);
            welcomeButton.setSelectable(false);
            if (RENDER_FPS) {
                menu.addButton(buildFPSButton(renderer));
            }

            final VRButton startButton = new VRButton(renderer.getContext(),
                    renderer.getContext().getString(R.string.start_training),
                    STANDARD_BUTTON_WIDTH,
                    STANDARD_BUTTON_HEIGHT);
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

            final VRButton skipTrainingButton = new VRButton(renderer.getContext(),
                    renderer.getContext().getString(R.string.skip_training),
                    STANDARD_BUTTON_WIDTH,
                    STANDARD_BUTTON_HEIGHT);
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
            });
            menu.addButton(welcomeButton);
            menu.addButton(startButton);

            menu.addButton(skipTrainingButton);
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
     * @return the ready to use {@link VRButton}, which is no selectable. (see {@link VRButton#isSelectable}
     * @throws ATexture.TextureException if there was a texturing error while constructing the button
     */
    @NonNull
    private static VRButton buildFPSButton(@NonNull final Renderer renderer) throws ATexture.TextureException {
        final VRButton fpsButton = new VRButton(renderer.getContext(),
                "",
                STANDARD_BUTTON_WIDTH,
                STANDARD_BUTTON_HEIGHT);
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
                    final VRButton button = new VRButton(renderer.getContext(),
                            grade.toString(renderer.getContext()),
                            STANDARD_BUTTON_WIDTH,
                            STANDARD_BUTTON_HEIGHT
                    );
                    button.setSelectable(false);
                    if (img != null && img.getGrade().equals(grade)) {
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
                                    try {
                                        VRImage next = VRViewActivity.nextEvaluation();
                                        ((VRScene) renderer.getCurrentScene()).recycle();
                                        renderer.switchScene(new VRScene(renderer, next, VRScene.MODE_EVALUATION));
                                    } catch (EmptyStackException e2) {
                                        button.setText("No new image"); //TODO put text in strings.xml
                                    }
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
                    final VRButton button = new VRButton(renderer.getContext(),
                            grade.toString(renderer.getContext()),
                            STANDARD_BUTTON_WIDTH,
                            STANDARD_BUTTON_HEIGHT
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
                                button.setText(renderer.getContext().getString(R.string.no_new_image));
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
