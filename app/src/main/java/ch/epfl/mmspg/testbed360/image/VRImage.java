package ch.epfl.mmspg.testbed360.image;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A {@link VRImage} is a 360° image that will be assessed in this app. It is defined by a {@link File}
 * that links to the image, but also by various properties such as codec, quality, grade etc...
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public final class VRImage {
    private final static String TAG = "VRImage";

    private final static int UNKNOWN_QUALITY = -1;
    private final static int NAME_PATTERN_GROUP_COUNT = 7;
    private final static String GRADE_PREFIX = "grade";
    private final static String QUALITY_PREFIX = "q";
    private final static Pattern NAME_PATTERN = Pattern.compile("(.*)_(.*)_(.*)_(\\d{1,6})x(\\d{1,6})_(.*)_(.*)\\.(png|jpg|jpeg)");

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
    private VRImageType vrImgType;

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
        Log.i(TAG,"Loaded img: "+this);
    }

    /**
     * Attempts to match the given name against the {@link #NAME_PATTERN} to extract some properties
     * like the author name, the image resolution, a predefined {@link ImageGrade} or simply
     * the {@link VRImageType}
     * @param name the file name of the image
     */
    private void initFromName(@NonNull String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);


        if (!matcher.matches() || matcher.groupCount() < NAME_PATTERN_GROUP_COUNT) {
            throw new IllegalArgumentException("Given name does not match pattern, name=" + name);
        }

        author = matcher.group(1);
        title = matcher.group(2);
        vrImgType = VRImageType.fromSlug(matcher.group(3));
        width = Integer.parseInt(matcher.group(4));
        height = Integer.parseInt(matcher.group(5));
        codec = matcher.group(6);

        String gradeOrQuality = matcher.group(7);
        if (gradeOrQuality.startsWith(GRADE_PREFIX)) {
            grade = ImageGrade.fromGrade(Integer.parseInt(gradeOrQuality.substring(GRADE_PREFIX.length())));
        } else if (gradeOrQuality.startsWith(QUALITY_PREFIX)) {
            quality = Integer.parseInt(gradeOrQuality.substring(QUALITY_PREFIX.length()));
        }
    }

    @Override
    public String toString(){
        return "title=" + title +
                ", author=" + author +
                ", vrType=" + vrImgType +
                ", dim=" + width + 'x' + height +
                ", codec=" + codec +
                ", quality=" + quality +
                (grade!= null ? ", grade=" + grade.toInt() : "")
                ;
    }


}
