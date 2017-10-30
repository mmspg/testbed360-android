package ch.epfl.mmspg.testbed360.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link VRImage} is a 360° image that will be assessed in this app. It is defined by a {@link File}
 * that links to the image, but also by various properties such as codec, quality, grade etc...
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public final class VRImage {
    private final static String TAG = "VRImage";

    private final static int UNKNOWN_QUALITY = -1;
    private final static int MIN_PATTERN_GROUP_COUNT = 6;
    private final static String GRADE_PREFIX = "grade";
    private final static String QUALITY_PREFIX = "q";
    private final static Pattern NAME_PATTERN = Pattern.compile("(.*)_(.*)_(.*)_(\\d{1,5})x(\\d{1,5})_(?:(.*)_)?(.*)\\.(?:png|jpg|jpeg)");

    /**
     * {@link File} containing this {@link VRImage}
     */
    private File file;
    /**
     * A {@link String} representing the name of the author
     */
    private String author;

    /**
     * A {@link String} representing the title of the picture
     */
    private String title;

    /**
     * An {@link VRImageType} representing the type of the image
     */
    private VRImageType vrImageType;

    /**
     * A {@link String} representing the name of the author
     */
    private ImageGrade grade;
    /**
     * A {@link String} representing the codec used for this picture
     */
    private String codec;
    /**
     * Width of this picture
     */
    private int width;
    /**
     * Height of this picture
     */
    private int height;
    /**
     * Quality of this picture
     */
    private int quality = UNKNOWN_QUALITY;

    /**
     * Creates a new {@link VRImage} based on the given {@link File}
     *
     * @param file the file used to initialize the {@link VRImage} properties
     * @throws IllegalStateException if it was not possible to reade the given file
     */
    public VRImage(@NonNull File file) throws IllegalStateException {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exists : " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalStateException("Denied to read file : " + file.getAbsolutePath());
        }
        this.file = file;
        initFromName(file.getName());
        Log.i(TAG, "Loaded img: " + this);
    }

    /**
     * Attempts to match the given name against the {@link #NAME_PATTERN} to extract some properties
     * like the author name, the image resolution, a predefined {@link ImageGrade} or simply
     * the {@link VRImageType}
     *
     * @param name the file name of the image
     */
    private void initFromName(@NonNull String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);


        if (!matcher.matches() || matcher.groupCount() < MIN_PATTERN_GROUP_COUNT) {
            throw new IllegalArgumentException("Given name does not match pattern, name=" + name);
        }

        author = matcher.group(1);
        title = matcher.group(2);
        vrImageType = VRImageType.fromName(matcher.group(3));
        width = Integer.parseInt(matcher.group(4));
        height = Integer.parseInt(matcher.group(5));

        int gradeOrQualIndex = 6;
        if (matcher.groupCount() == MIN_PATTERN_GROUP_COUNT + 1) {
            //in this case we have a matching codec, thus the grade/quality group index must
            //be incremented
            codec = matcher.group(6);
            gradeOrQualIndex++;
        }
        String gradeOrQuality = matcher.group(gradeOrQualIndex);
        if (gradeOrQuality.startsWith(GRADE_PREFIX)) {
            grade = ImageGrade.fromGrade(Integer.parseInt(gradeOrQuality.substring(GRADE_PREFIX.length())));
        } else if (gradeOrQuality.startsWith(QUALITY_PREFIX)) {
            quality = Integer.parseInt(gradeOrQuality.substring(QUALITY_PREFIX.length()));
        }
    }

    @Nullable
    public Bitmap[] getBitmap(@NonNull Context context) throws IOException {
        switch (vrImageType) {
            case CUBIC:
                return ImageUtils.loadCubicMap(context, this);
            case EQUIRECTANGULAR:
                return new Bitmap[]{BitmapFactory.decodeFile(file.getAbsolutePath(),new BitmapFactory.Options())};
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "title=" + title +
                ", author=" + author +
                ", vrType=" + vrImageType +
                ", dim=" + width + 'x' + height +
                ", codec=" + codec +
                ", quality=" + quality +
                (grade != null ? ", grade=" + grade.toInt() : "")
                ;
    }


    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * Builds and returns a slug. A slug is useful to know if two {@link VRImage}s represents
     * the same thing (i.e. image taken at the same spot, same time) even if they might differ of
     * {@link #vrImageType} or any other field. We consider here that having the same {@link #author}
     * and {@link #title} for two pictures means they represents the same thing.
     * see {@link ImageUtils#distinctShuffle(List)}
     *
     * @return a {@link String} containing the {@link #author} and {@link #title} concatenated.
     */
    public String getSlug() {
        return author + ":" + title;
    }

    public VRImageType getVrImageType() {
        return vrImageType;
    }

    public ImageGrade getGrade() {
        return grade;
    }

    public File getFile() {
        return file;
    }
}
