package ch.epfl.mmspg.testbed360;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.renderer.Renderer;

import java.io.IOException;
import java.util.Stack;

import ch.epfl.mmspg.testbed360.image.ImageUtils;
import ch.epfl.mmspg.testbed360.ui.VRMenuFactory;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 31/10/2017
 */

public final class WelcomeScene extends VRScene {
    private final static String TAG = "WelcomeScene";

    //TODO remove or set false this in production, only for debugging
    private final static boolean RENDER_AXIS = true;


    public WelcomeScene(@NonNull Renderer renderer) {
        super(renderer, null);
        try {
            setSkybox(ImageUtils.loadCubicMap(renderer.getContext(), R.drawable.jvet_kiteflite_cubemap32_2250x1500_raw_q00));
        } catch (IOException e) {
            Log.e(TAG, "Error setting the skybox texture");
            e.printStackTrace();
        }
        initAxis();
        selectionDot.setVisible(true);
    }

    @Override
    protected void initMenu(final Renderer renderer) {
        menu = VRMenuFactory.buildWelcomeMenu(renderer);
        menu.setVisible(true);
        addChild(menu);
    }


    private void initAxis() {
        if (RENDER_AXIS) {
            addChild(createLine(Vector3.ZERO, Vector3.X, Color.RED));
            addChild(createLine(Vector3.ZERO, Vector3.Y, Color.GREEN));
            addChild(createLine(Vector3.ZERO, Vector3.Z, Color.BLUE));
        }
    }

    private static Line3D createLine(Vector3 p1, Vector3 p2, int color) {
        Stack<Vector3> points = new Stack<>();
        points.add(p1);
        points.add(p2);

        Line3D line = new Line3D(points, 2f, color);
        Material material = new Material();
        material.setColor(color);
        line.setMaterial(material);
        line.moveUp(-5);
        line.moveRight(-5);
        line.moveForward(-5);
        return line;
    }
}
