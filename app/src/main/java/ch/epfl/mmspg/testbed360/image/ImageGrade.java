// Copyright (C) 2017 ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland
// Multimedia Signal Processing Group
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package ch.epfl.mmspg.testbed360.image;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.NoSuchElementException;

import ch.epfl.mmspg.testbed360.R;

/** This class represents a grade associated to an image. It includes helpful methods to get a {@link String}
 * representation for UI purposes, get an {@link ImageGrade} from a given int.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 29/10/2017
 */

public enum ImageGrade {
    EXCELLENT(R.string.grade_5,5),
    GOOD(R.string.grade_4,4),
    FAIR(R.string.grade_3,3),
    POOR(R.string.grade_2,2),
    BAD(R.string.grade_1,1),
    NONE(R.string.grade_none,-1);

    private final static String TAG = "ImageGrade";
    private int strId;
    private int grade;

    /**
     * Creates an {@link ImageGrade}
     * @param strId the resource id {@link String} to be used to represent this grade
     * @param grade the int value of this grade
     */
    ImageGrade(int strId, int grade){
        this.strId = strId;
        this.grade = grade;
    }

    /**
     * Returns the {@link String} representation of this {@link ImageGrade}
     * @param context the {@link Context} from which we load the {@link String}
     * @return a {@link String} corresponding to the resource with id {@link #strId}, or the {@link String}
     * value of {@link #grade} if this resource does not exist
     */
    @NonNull
    public String toString(@NonNull Context context){
        try{
            return context.getString(strId);
        }catch (Resources.NotFoundException e){
            Log.e(TAG,"String id invalid : "+strId+", returning "+grade);
            return Integer.toString(grade);
        }
    }

    public int toInt(){
        return grade;
    }

    /**
     * Helper method to get an {@link ImageGrade} from its grade value. Can be used when parsing data,
     * we can then only store {@link #grade} and retrieve the {@link ImageGrade} later.
     * @param grade the grade value of the {@link ImageGrade} we want
     * @return the {@link ImageGrade} if it exists, or throws a {@link NoSuchElementException} if it
     * does exists.
     */
    @NonNull
    public static ImageGrade fromGrade(int grade){
        for(ImageGrade imageGrade : values()){
            if(imageGrade.grade == grade){
                return imageGrade;
            }
        }
        throw new NoSuchElementException("There exists no "+TAG+" for grade "+grade);
    }
}
