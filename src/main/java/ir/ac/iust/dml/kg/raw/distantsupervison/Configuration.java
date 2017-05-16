package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Configuration {
    public static final int maximumNoOfVocabularyForBagOfWords = 4000;

    public static final int maximumNoOfInstancesForEachPredicate = 1000;
    public static final int noOfTrainExamples = 16000;
    public static final int noOfTestExamples = noOfTrainExamples/5;
    public static final int noOfTotalExamples = noOfTrainExamples+noOfTestExamples;

    public static final int numberOfPredicatesToLoad =
            (Integer) noOfTotalExamples/maximumNoOfInstancesForEachPredicate +1;

    public static final double confidenceThreshold = 0;

    public static final String extractorClient = "http://194.225.227.161:8094";
    public static final String ontologyClient = "http://194.225.227.161:8090";

    public static final int maxWindowSize = 5;

}
