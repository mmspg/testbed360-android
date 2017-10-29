package ch.epfl.mmspg.testbed360;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class including various methods used to decode and manipulate {@link Bitmap}s
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public final class BitmapUtils {
    private final static String TAG = "BitmapUtils";

    /**
     * Loads a region of a {@link Bitmap}
     * @param decoder the {@link BitmapRegionDecoder} to be used
     * @param regionLeft the ratio on the {@link Bitmap} corresponding to the left side of the region
     * @param regionTop the ratio on the {@link Bitmap} corresponding to the top side of the region
     * @param regionRight the ratio on the {@link Bitmap} corresponding to the right side of the region
     * @param regionBottom the ratio on the {@link Bitmap} corresponding to the bottom side of the region
     * @return a {@link Bitmap} containing the wanted region or null if the image data could not be
     *         decoded.
     */
    private static Bitmap loadBitmapRegion(
            BitmapRegionDecoder decoder,
            float regionLeft, float regionTop,
            float regionRight, float regionBottom){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int h = decoder.getHeight();
        int w = decoder.getWidth();
        Log.d(TAG, "loadBitmapRegion: image is " + w + "x" + h);

        Rect region = new Rect(
                Math.round(regionLeft * w), Math.round(regionTop * h),
                Math.round(regionRight * w), Math.round(regionBottom * h));
        Log.d(TAG, "loadBitmapRegion: decoding region : " + region);
        return decoder.decodeRegion(region, opt);
    }

    /**
     * Helper methods to load a cube map image into an array of {@link Bitmap}s. The order of faces
     * is : left, right, top, bottom, back, front
     * @param context {@link Context} of the app to load the resource
     * @param resourceId the id of the resource containing the cube map to decode
     * @return a {@link Bitmap} array of length, containing each faces of the cube, or containing null
     * for a face if there was an error loading the {@link Bitmap} ( see {@link #loadBitmapRegion(BitmapRegionDecoder, float, float, float, float)}
     * @throws IOException if the image format is not supported or can not be decoded. (see {@link BitmapRegionDecoder#newInstance(InputStream, boolean)}
     */
    public static Bitmap[] loadCubicMap(Context context, int resourceId) throws IOException {
        Bitmap[] bitmapCube = new Bitmap[6];
        InputStream is = context.getResources().openRawResource(resourceId);
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
        float hPadding = 1f / 3f;
        float vPadding = 1f / 2f;

        //left
        bitmapCube[0] = rotateBitmap(
                loadBitmapRegion(
                        decoder,
                        hPadding,
                        vPadding,
                        hPadding * 2,
                        vPadding * 2
                ), -90
        );
        //right
        bitmapCube[1] = loadBitmapRegion(
                decoder,
                hPadding,
                0f,
                hPadding * 2,
                vPadding
        );
        //top
        bitmapCube[2] = rotateBitmap(
                loadBitmapRegion(
                decoder,
                hPadding * 2,
                vPadding,
                hPadding * 3,
                vPadding * 2
                ),180
        );
        //bottom
        bitmapCube[3] = loadBitmapRegion(
                decoder,
                0f,
                vPadding,
                hPadding,
                vPadding * 2
        );
        //back
        bitmapCube[4] = loadBitmapRegion(
                decoder,
                hPadding * 2,
                0f,
                hPadding * 3,
                vPadding);
        //front
        bitmapCube[5] = loadBitmapRegion(
                decoder,
                0f,
                0f,
                hPadding,
                vPadding
        );

        is.close();
        decoder.recycle();

        return bitmapCube;

    }

    /**
     * Given a {@link Bitmap}, it returns a rotated new {@link Bitmap}
     * @param bitmap the {@link Bitmap} to rotate
     * @param angle the angle of rotation, in degrees
     * @return the bitmap rotated with angle given
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
