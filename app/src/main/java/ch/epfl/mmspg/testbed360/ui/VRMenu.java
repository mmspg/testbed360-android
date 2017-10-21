package ch.epfl.mmspg.testbed360.ui;

import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.RectangularPrism;

import java.util.ArrayList;

import ch.epfl.mmspg.testbed360.VRViewRenderer;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 21/10/2017
 */

public class VRMenu extends RectangularPrism {
    private final static String TAG = "VRMenu";

    private final static float LOOKING_AT_ANGLE = 5;
    private final static float BUTTON_SPACING = 0.5f;

    private static int MENU_COUNTER = 0;

    private ArrayList<VRButton> buttons = new ArrayList<>();

    public VRMenu() {
        super(0, 21f, 2f);
        Material prismMaterial = new Material();
        prismMaterial.setColorInfluence(0);
        setTransparent(true);
        setMaterial(prismMaterial);
    }

    public void addButton(VRButton button) {
        if (button == null) {
            throw new IllegalArgumentException("VRButton cannot be null");
        }
        float nextYPos = computeNextButtonY();
        button.setParentMenu(this);
        buttons.add(button);
        addChild(button);

        button.moveUp(-nextYPos);
    }

    public void removeButton(VRButton button) {
        if (button == null) {
            throw new IllegalArgumentException("VRButton cannot be null");
        }
        buttons.remove(button);
        removeChild(button);
    }

    public VRButton getButton(int index) {
        if (index < 0 || buttons.size() <= index) {
            throw new IllegalArgumentException("Incorrect index : " + index);
        }
        return buttons.get(index);
    }

    public boolean onCardboardTrigger() {
        for (VRButton button : buttons) {
            if (button.onCardboardTrigger()) {
                return true;
            }
        }
        return false;
    }

    public void onDrawing(VRViewRenderer renderer) {
        //setRotY(renderer.getCurrentCamera().getRotY());

        boolean consumed = false;
        for (VRButton button : buttons) {
            if (consumed) {
                button.setHovered(false);
            } else {
                consumed = renderer.isLookingAtObject(button);
                button.setHovered(consumed);
            }
        }
    }

    private float computeNextButtonY() {
        float y = 0;
        for (VRButton button : buttons) {
            y += button.getHeight() + BUTTON_SPACING;
        }
        return y;
    }


}
