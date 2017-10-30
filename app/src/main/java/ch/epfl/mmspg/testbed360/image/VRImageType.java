package ch.epfl.mmspg.testbed360.image;

import android.support.annotation.NonNull;

import java.util.NoSuchElementException;

/**
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public enum VRImageType {
    CUBIC("cubemap32"),
    EQUIRECTANGULAR("equirec");

    private String[] names;

    VRImageType(@NonNull String...names){
        this.names = names;
    }

    @NonNull
    public static VRImageType fromName(@NonNull String name){
        for(VRImageType VRImageType : values()){
            for(String s : VRImageType.names){
                if(s.equals(name)){
                    return VRImageType;
                }
            }
        }
        throw new NoSuchElementException("Given name does not match any VRImageType : "+name);
    }
}
