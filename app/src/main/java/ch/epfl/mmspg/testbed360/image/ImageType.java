package ch.epfl.mmspg.testbed360.image;

import android.support.annotation.NonNull;

import java.util.NoSuchElementException;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public enum ImageType {
    CUBIC("cubemap32"),
    EQUIRECTANGULAR("equirec");

    private String[] slugs;

    ImageType(@NonNull String...slugs){
        this.slugs = slugs;
    }

    @NonNull
    public static ImageType fromSlug(@NonNull String slug){
        for(ImageType imageType : values()){
            for(String s : imageType.slugs){
                if(s.equals(slug)){
                    return imageType;
                }
            }
        }
        throw new NoSuchElementException("Given slug does not match any ImageType : "+slug);
    }
}
