/*
 * Farsi Knowledge Graph Project
 *  Iran University of Science and Technology (Year 2017)
 *  Developed by Ensieh Hemmatan.
 */

package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import java.util.Comparator;
import java.util.Map;

public class HashmapValueComparator implements Comparator<Map.Entry> {
    @Override
    public int compare(Map.Entry x, Map.Entry y) {
        if ((Double)x.getValue() > (Double)y.getValue()) {
            return -1;
        }
        if ((Double)x.getValue() < (Double)y.getValue()) {
            return 1;
        }
        return 0;
    }

}
