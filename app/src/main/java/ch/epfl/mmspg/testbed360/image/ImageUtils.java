package ch.epfl.mmspg.testbed360.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static ch.epfl.mmspg.testbed360.VRScene.MODE_EVALUATION;
import static ch.epfl.mmspg.testbed360.VRScene.MODE_TRAINING;

/**
 * Helper class including various methods used to decode and manipulate {@link Bitmap}s, but also
 * extracts some information from {@link java.io.File} images.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public final class ImageUtils {
    private final static String TAG = "ImageUtils";

    private final static String EVALUATION_DIR = "evaluation";
    private final static String TRAINING_DIR = "training";

    private static Bitmap[] cubeBitmaps = new Bitmap[6];
    private static Bitmap[] sphereBitmap = new Bitmap[1];

    /**
     * Loads a region of a {@link Bitmap}
     *
     * @param decoder        the {@link BitmapRegionDecoder} to be used
     * @param reusableBitmap a {@link Bitmap} we want to reuse
     * @param regionLeft     the ratio on the {@link Bitmap} corresponding to the left side of the region
     * @param regionTop      the ratio on the {@link Bitmap} corresponding to the top side of the region
     * @param regionRight    the ratio on the {@link Bitmap} corresponding to the right side of the region
     * @param regionBottom   the ratio on the {@link Bitmap} corresponding to the bottom side of the region
     * @return a {@link Bitmap} containing the wanted region or null if the image data could not be
     * decoded.
     */
    @Nullable
    private static Bitmap loadBitmapRegion(
            @NonNull BitmapRegionDecoder decoder,
            @Nullable Bitmap reusableBitmap,
            float regionLeft, float regionTop,
            float regionRight, float regionBottom) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inBitmap = reusableBitmap;
        opt.inMutable = true;
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
     * See {@link #loadCubicMap(InputStream)}
     *
     * @param context    {@link Context} of the app to load the resource
     * @param resourceId the id of the resource containing the cube map to decode
     * @return a {@link Bitmap} array of length 6, containing each faces of the cube, or containing null
     * for a face if there was an error loading the {@link Bitmap} ( see {@link #loadBitmapRegion(BitmapRegionDecoder, Bitmap, float, float, float, float)}
     * @throws IOException if the image format is not supported or can not be decoded. (see {@link BitmapRegionDecoder#newInstance(InputStream, boolean)}
     */
    @NonNull
    public static Bitmap[] loadCubicMap(@NonNull Context context, int resourceId) throws IOException {
        return loadCubicMap(context.getResources().openRawResource(resourceId));
    }

    /**
     * See {@link #loadCubicMap(InputStream)}
     *
     * @param image the image containing the {@link VRImage#file} to read from
     * @return a {@link Bitmap} array of length 6, containing each faces of the cube, or containing null
     * for a face if there was an error loading the {@link Bitmap} ( see {@link #loadBitmapRegion(BitmapRegionDecoder, Bitmap, float, float, float, float)}
     * @throws IOException if the image format is not supported or can not be decoded. (see {@link BitmapRegionDecoder#newInstance(InputStream, boolean)}
     */
    @NonNull
    public static Bitmap[] loadCubicMap(@NonNull VRImage image) throws IOException {
        return loadCubicMap(new FileInputStream(image.getFile()));
    }

    /**
     * Helper methods to load a cube map image into an array of {@link Bitmap}s. The order of faces
     * is : left, right, top, bottom, back, front
     *
     * @param stream the stream to read the image from. This methods closes it when done.
     * @return a {@link Bitmap} array of length 6, containing each faces of the cube, or containing null
     * for a face if there was an error loading the {@link Bitmap} ( see {@link #loadBitmapRegion(BitmapRegionDecoder, Bitmap, float, float, float, float)}
     * @throws IOException if the image format is not supported or can not be decoded. (see {@link BitmapRegionDecoder#newInstance(InputStream, boolean)}
     */
    @NonNull
    public static Bitmap[] loadCubicMap(@NonNull InputStream stream) throws IOException {
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(stream, false);
        float hPadding = 1f / 3f;
        float vPadding = 1f / 2f;

        //left
        cubeBitmaps[0] = rotateBitmap(
                loadBitmapRegion(
                        decoder,
                        cubeBitmaps[0],
                        hPadding,
                        vPadding,
                        hPadding * 2,
                        vPadding * 2
                ), -90
        );
        //right
        cubeBitmaps[1] = loadBitmapRegion(
                decoder,
                cubeBitmaps[1],
                hPadding,
                0f,
                hPadding * 2,
                vPadding
        );
        //top
        cubeBitmaps[2] = rotateBitmap(
                loadBitmapRegion(
                        decoder,
                        cubeBitmaps[2],
                        hPadding * 2,
                        vPadding,
                        hPadding * 3,
                        vPadding * 2
                ), 180
        );
        //bottom
        cubeBitmaps[3] = loadBitmapRegion(
                decoder,
                cubeBitmaps[3],
                0f,
                vPadding,
                hPadding,
                vPadding * 2
        );
        //back
        cubeBitmaps[4] = loadBitmapRegion(
                decoder,
                cubeBitmaps[4],
                hPadding * 2,
                0f,
                hPadding * 3,
                vPadding);
        //front
        cubeBitmaps[5] = loadBitmapRegion(
                decoder,
                cubeBitmaps[5],
                0f,
                0f,
                hPadding,
                vPadding
        );

        stream.close();
        decoder.recycle();

        return cubeBitmaps;

    }

    /**
     * Given a {@link Bitmap}, it returns a rotated new {@link Bitmap}
     *
     * @param bitmap the {@link Bitmap} to rotate
     * @param angle  the angle of rotation, in degrees
     * @return the bitmap rotated with angle given
     */
    @Nullable
    public static Bitmap rotateBitmap(@Nullable Bitmap bitmap, float angle) {
        if(bitmap == null){
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //TODO check memory usage of the following method !
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Reads from external storage all {@link VRImage}s associated to the given mode. Also inits the
     * dir if it does not exists, along with an "init" file that is here only to make Android media
     * scanner discover this folder, so that it can be used in USB MTP (see issue https://issuetracker.google.com/issues/37071807)
     *
     * @param context {@link Context} to be used to load {@link VRImage}s
     * @param mode    the mode wanted, corresponds to {@link ch.epfl.mmspg.testbed360.VRScene#MODE_EVALUATION}
     *                or {@link ch.epfl.mmspg.testbed360.VRScene#MODE_TRAINING}
     * @return a {@link List} containing all {@link VRImage}s for the given {@param mode}
     * @throws IllegalStateException if the directory does not contain any image, or if there was a
     *                               permission while trying to read an image (see {@link VRImage#VRImage(File)}
     */
    @NonNull
    public static List<VRImage> loadVRImages(@NonNull Context context, @NonNull File sessionDir, int mode) throws IllegalStateException {
        File imgDir;
        switch (mode) {
            case MODE_TRAINING:
                imgDir = new File(sessionDir, TRAINING_DIR);
                break;
            case MODE_EVALUATION:
                imgDir = new File(sessionDir, EVALUATION_DIR);
                break;
            default:
                throw new IllegalArgumentException("Unknown mode to load images: " + mode);
        }
        imgDir.mkdirs();

        File init = new File(imgDir, "init"); //Getting a file within the dir.
        Log.d(TAG, "Writing to " + init);
        try (FileOutputStream out = new FileOutputStream(init)) {
            init.createNewFile();
            out.write("0".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //workaround to fix an issue with folder not appearing on USB MTP connection. Basically triggers
        //a rescan of files. See more about this issue here https://issuetracker.google.com/issues/37071807
        MediaScannerConnection.scanFile(context, new String[]{init.getAbsolutePath()}, null, null);

        File[] imgFiles = imgDir.listFiles();
        if (imgFiles.length == 0) {
            throw new IllegalStateException("Dir does not contain images : " + imgDir);
        }

        ArrayList<VRImage> vrImages = new ArrayList<>();

        for (File imgFile : imgFiles) {
            try {
                Log.d(TAG, "Reading file " + imgFile.getName() + "...");
                vrImages.add(new VRImage(imgFile));
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "File name " + imgFile.getName() + " does not match");
            }
        }
        return vrImages;
    }

    @Deprecated //although this gives correct output, this has a way too big complexity !
    public static void bruteForceShuffle(@NonNull List<VRImage> toShuffle) {
        boolean distinct = false;
        long start = System.currentTimeMillis();

        int shuffleCount = 0;
        while (!distinct) {
            shuffleCount++;
            Collections.shuffle(toShuffle);
            VRImage prev = null;

            distinct = true;
            for (VRImage img : toShuffle) {
                if (prev != null && prev.getSlug().equals(img.getSlug())) {
                    distinct = false;
                    break;
                }
                prev = img;
            }
        }
        Log.d(TAG, shuffleCount + " shuffles, done in " + (System.currentTimeMillis() - start) + "ms");
        Log.d(TAG, "Order is :");

        for (VRImage img : toShuffle) {
            Log.d(TAG, "\t" + img.getSlug());
        }
    }

    /**
     * Constructs a new {@link List} with elements from {@param toShuffle} shuffled with the best effort
     * to ensure that there is no two consecutive {@link VRImage}s with the same slug (see {@link VRImage#getSlug()}.
     * This method is a best effort implementation; there exists cases where the shuffle contract can
     * not be respected at all (simply all {@link VRImage}s from {@param toShuffle} have the same slug)
     * or way too much time costly to be interesting. So it might perform a trade-off so that the computation
     * does not infinite loop (in impossible cases) or does not take too much time.
     * An ideal case is to have for any slug no more than 2x {@link VRImage}s associated than other slugs
     * (see {@link #initShuffledSlugsList(List, HashMap, String, ArrayList)} to understand why x2). This
     * case is also an edge limit for the logic of this app : having too much images of the same slug
     * might alter the tester's opinion on quality of those images.
     *
     * @param toShuffle a {@link List} containing all {@link VRImage} loaded for this session and that
     *                  should be shuffled as expressed in the previous paragraph
     * @return a shuffled {@link List} of {@link VRImage}s, with a best effort to follow the
     * aforementioned shuffle contract.
     */
    @NonNull
    public static List<VRImage> distinctShuffle(@NonNull List<VRImage> toShuffle) {
        long start = System.currentTimeMillis();

        /*First we build a HashMap mapping all existing slugs (see {@link VRImage#getSlug()} for details)
        in this list to shuffle to HashSet<VRImage> regrouping all images that have the same slug.
         */
        HashMap<String, HashSet<VRImage>> groupedVRImages = new HashMap<>();

        for (VRImage img : toShuffle) {
            if (!groupedVRImages.containsKey(img.getSlug())) {
                groupedVRImages.put(img.getSlug(), new HashSet<VRImage>());
            }
            groupedVRImages.get(img.getSlug()).add(img);
        }

        for (String slug : groupedVRImages.keySet()) {
            Log.d(TAG, "slug " + slug + " has " + groupedVRImages.get(slug).size() + " VRImages");
        }

        //the result ArrayList
        ArrayList<VRImage> shuffled = new ArrayList<>();

        /* Now for the shuffling part :
        For the sake of simplicity, we will organize our shuffling per "rounds". Basically in a
        round we do #nb_slugs iterations and pick a remaining slug (i.e. not already chosen for this
        round). Then we just pick a VRImage that has not been selected yet.
        We keep track of the previous chosen slug, so that between two rounds we do not pick the same
        slug for the previous round's last slug and the current round's first slug.
        In case we have more VRImages mapped to a slug than others, we could infinite loop. The trade off
        here is to accept to pick an image at the end if there is always the same slug remaining.
        This case may happen when a slug has more VRImages associated to it than other slugs.
         */
        ArrayList<String> possibleSlugs = new ArrayList<>();

        int shuffleCount = initShuffledSlugsList(possibleSlugs, groupedVRImages, null, shuffled);

        String selectedSlug = null;
        while (shuffled.size() < toShuffle.size()) {
            if (possibleSlugs.size() > 0) {
                String prevSlug = selectedSlug;
                selectedSlug = possibleSlugs.get(0);
                if (prevSlug == null || !prevSlug.equals(selectedSlug)) {
                    /*here we remove at the index instead of removing the slug because we might have
                    twice the same slug in possibleSlugs
                     */
                    possibleSlugs.remove(0);
                    boolean noImg = true;
                    for (VRImage img : groupedVRImages.get(selectedSlug)) {
                        if (!shuffled.contains(img)) {
                            noImg = false;
                            Log.d(TAG, "\t\tChosen: " + img);
                            shuffled.add(img);
                            break;
                        }
                    }
                    /*if we arrive here, then it means that there is no picture not already added
                    that has this slug. Hence we remove it fully, to avoid useless iterations after
                     */
                    if (noImg) {
                        possibleSlugs.remove(selectedSlug);
                        selectedSlug = prevSlug; //as we didn't really select a slug
                    }
                } else if (possibleSlugs.contains(prevSlug) && possibleSlugs.size() == 1) {
                    /*here we are in the case were the remaining slugs are only of the same type
                    so we pick it anyway next time, breaking the contract but not infinite looping
                    which is a fair trade off. This case is rare when there is a nearly equal amount
                    of VRImages for each slug
                     */
                    selectedSlug = null;
                }
            } else {
                //we'll pick a slug next time
                shuffleCount += initShuffledSlugsList(possibleSlugs, groupedVRImages, selectedSlug, shuffled);
            }

        }

        Log.d(TAG, shuffleCount + " shuffles, done in " + (System.currentTimeMillis() - start) + "ms");
        return shuffled;
    }

    /**
     * Private helper method associated to {@link #distinctShuffle(List)}. This method is to be used
     * for any new round, in order to re-init the possible slugs list and shuffle it so that we can
     * pop the first element every time and be sure that our shuffle contract is followed.
     * It modifies the given {@param toShuffle} list to first add it all possible slugs.
     * A slug is considered "possible" if it exists (i.e. is a key in {@param groupedVRImages} and if
     * there remains some images associated to this slug that has not yet been added to the {@params addedImgs}
     * list.
     * Then we re-add slugs that would have more remaining non-added images associated to it than other
     * slugs, so that we can reach an equal number of {@link VRImage} remaining to add per slug.
     * We check if we have only one slug (possibly duplicated) in the generated possible list; and
     * return if it's the case. This case can arise only if there is more than twice VRImages count
     * associated to a slug than any other slug, which wouldn't make sense to have in this app; but
     * better catch this case anyway !
     * Finally we shuffle this list until we reach a state were there is no consecutive slug and the
     * first slug is not the same as {@param previousLastingSlug}. This part will never infinite loop
     * as there is at max twice the same slug.
     * And we have our possible slugs to pick from for the next round.
     *
     * @param toShuffle           the {@link List} in which will be added slugs
     * @param groupedVRImages     the {@link HashMap<String,HashSet<VRImage>>} mapping all slugs to a
     *                            {@link HashSet<VRImage>} of {@link VRImage}s that have the same slug
     * @param previousLastingSlug the last slug of the possible slugs of the previous round, or null
     *                            if we are at round 0. see {@link #distinctShuffle(List)}
     * @param addedImgs           a {@link List} containing all {@link VRImage}s already selected and ordered
     *                            with respect to the contract. see {@link #distinctShuffle(List)}
     * @return the amount of slug shuffles
     */
    private static int initShuffledSlugsList(@NonNull List<String> toShuffle,
                                             @NonNull HashMap<String, HashSet<VRImage>> groupedVRImages,
                                             @Nullable String previousLastingSlug,
                                             @NonNull ArrayList<VRImage> addedImgs) {
        int lowestCount = Integer.MAX_VALUE;
        String[] slugArray = new String[groupedVRImages.keySet().size()];
        int[] alreadyAddedPerSlug = new int[slugArray.length];
        int[] totalImgPerSlug = new int[slugArray.length];

        int index = 0;
        for (String slug : groupedVRImages.keySet()) {
            int count = 0;
            for (VRImage img : groupedVRImages.get(slug)) {
                if (!addedImgs.contains(img)) {
                    count++;
                }
                totalImgPerSlug[index]++;
            }
            slugArray[index] = slug;
            alreadyAddedPerSlug[index] = count;
            if (lowestCount > count) {
                lowestCount = count;
            }
            index++;
        }

        toShuffle.clear();
        toShuffle.addAll(groupedVRImages.keySet());

        for (int i = 0; i < alreadyAddedPerSlug.length; i++) {
            if (totalImgPerSlug[i] - alreadyAddedPerSlug[i] > 0) {
                //this means there remains some images from this slug that has not been added yet!
                toShuffle.add(slugArray[i]);
            }
            if (alreadyAddedPerSlug[i] > lowestCount) {
                /*if there is more images mapped to this slug than for other slugs, we add this slug
                twice si that we can decrement the number of remaining non added images faster for this
                 slug and reach a stability in number of remaining non added images between all slugs
                 */
                toShuffle.add(slugArray[i]);
            }
        }

        /*We check the case were all possible slugs are just the same. In this case we're out of luck
        and we have not other choice than to return consecutive slugs. This case may arise only if
        a given slug has more than twice the amount of VRImage associated to it than any other slugs.
        This case should not arise in any case as it wouldn't make much sense to "flood" the tester
        of images with so much of one image !
         */
        boolean allSimilar = true;
        String p = null;
        for (String s : toShuffle) {
            allSimilar = allSimilar && (p == null || p.equals(s));
            p = s;
        }
        if (allSimilar) {
            return 0;
        }

        /* Now we shuffle our possible slugs. We iterate until we reach a state were there is no
        consecutive slug and the  first slug is not the same as {@param previousLastingSlug}.
        This will never infinite loop as there is at max twice the same slug. */
        boolean distinct = false;
        int shuffleCount = 0;
        while (!distinct) {
            shuffleCount++;
            Collections.shuffle(toShuffle);
            String prev = previousLastingSlug; //the last slug from previous round, given in args here
            distinct = true;
            for (String s : toShuffle) {
                if (prev != null && prev.equals(s)) {
                    distinct = false;
                    break;
                }
                prev = s;
            }
        }
        Log.d(TAG, "\tNew Round ! (" + shuffleCount + " slug shuffles)");
        return shuffleCount;
    }

    /**
     * Loads the Equirectangular image of the given {@link VRImage}.
     *
     * @param image the {@link VRImage} to load the {@link Bitmap} from
     * @return a {@link Bitmap} array containing only the equirectangular {@link Bitmap}
     */
    @NonNull
    public static Bitmap[] loadSphereBitmap(@Nullable VRImage image) {
        if(image == null){
            return new Bitmap[6];
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inBitmap = sphereBitmap[0];
        opt.inMutable = true;
        return new Bitmap[]{BitmapFactory.decodeFile(image.getFile().getAbsolutePath(), opt)};
    }
}
