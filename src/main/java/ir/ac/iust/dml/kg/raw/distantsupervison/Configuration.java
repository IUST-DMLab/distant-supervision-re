package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Configuration {
    public static final int maximumNoOfVocabularyForBagOfWords = 4000;

    public static final int maximumNoOfInstancesForEachPredicate = 1000;
    public static final int noOfTrainExamples = 21000;
    public static final int noOfTestExamples = noOfTrainExamples / 21;
    public static final int noOfTotalExamples = noOfTrainExamples+noOfTestExamples;

    public static final int numberOfPredicatesToLoad =
            (Integer) noOfTotalExamples/maximumNoOfInstancesForEachPredicate +1;

    public static final double confidenceThreshold = 0.4;

    public static final String extractorClient = "http://194.225.227.161:8094";
    public static final String ontologyClient = "http://194.225.227.161:8090";
    public static final String exportURL = "http://dmls.iust.ac.ir:8100/rest/v1/raw/export";

    public static final int maxWindowSize = 5;

    public static final boolean getPredicatesFromFile = true;

    public static class libLinearParams {
        public static final double costOfConstraintsViolation = 1.0;
        public static final double epsStoppingCriteria = 0.1;
    }

    public static final String trainingSetMode = Constants.trainingSetModes.LOAD_PREDICATES_FROM_FILE;

}
