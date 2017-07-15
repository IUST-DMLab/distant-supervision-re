package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Configuration {
    public static final int maximumNoOfVocabularyForBagOfWords = 4000;

    public static final int maximumNumberOfTrainExamples = 36000;
    public static final int maximumNoOfInstancesForEachPredicate = 500;

    public static int noOfTrainExamples;// = 33000;//21000;
    public static int noOfTestExamples;

    public static final int maximumNumberOfPredicatesToLoad = 72;
    //(Integer) noOfTotalExamples/maximumNoOfInstancesForEachPredicate +1;

    public static final double confidenceThreshold = 0.4;

    public static final String extractorClient = "http://194.225.227.161:8094";
    public static final String ontologyClient = "http://194.225.227.161:8090";
    public static final String exportURL = "http://dmls.iust.ac.ir:8100/rest/v1/raw/export";

    public static final int maxWindowSize = 5;

    public static class libLinearParams {
        public static final double costOfConstraintsViolation = 1.0;
        public static final double epsStoppingCriteria = 0.1;
        public static final String pathToDirectory = "C:\\Users\\hemmatan\\Desktop\\KG\\RE segmented-bow\\lastLogitModel\\";
    }

    public static class dl4jParams {
        public static final int batchSizeTraining = 33837;
        public static final String pathToDirectory = "C:\\Users\\hemmatan\\Desktop\\KG\\RE segmented-bow\\lastDeepModel\\";
    }

    public static final String trainingSetMode = Constants.trainingSetModes.LOAD_DATA_FROM_CSV;

}
