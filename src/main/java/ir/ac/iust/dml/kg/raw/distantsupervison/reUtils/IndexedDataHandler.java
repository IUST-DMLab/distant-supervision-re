package ir.ac.iust.dml.kg.raw.distantsupervison.reUtils;

import java.util.HashMap;

/**
 * Created by hemmatan on 7/1/2017.
 */
public class IndexedDataHandler {

    public static void insertToIndexedData(String newData, HashMap<String, Double> dataIndices,
                                           HashMap<Double, String> dataInvertedIndices, Double numberOfData,
                                           HashMap<String, Double> dataCounts){
        if (!dataIndices.containsKey(newData)) {
            dataIndices.put(newData, ++numberOfData);
            dataInvertedIndices.put(numberOfData, newData);
            dataCounts.put(newData, 1.0);
        }
        else
                dataCounts.put(newData,
                dataCounts.get(newData)+1);
        }
}
